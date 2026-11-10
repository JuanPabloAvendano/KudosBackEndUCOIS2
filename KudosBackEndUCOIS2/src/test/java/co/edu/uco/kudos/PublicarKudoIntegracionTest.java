package co.edu.uco.kudos;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import co.edu.uco.kudos.dobles.Dobles;
import co.edu.uco.kudos.servicios.input.ports.PublicarEventosPendientesInputPort;
import co.edu.uco.kudos.servicios.output.dto.EventoKudoPublicadoDTO;
import co.edu.uco.kudos.servicios.output.dto.RegistroAuditoriaDTO;
import co.edu.uco.kudos.servicios.output.ports.repositorios.AlmacenAdjuntos;
import co.edu.uco.kudos.servicios.output.ports.repositorios.Auditoria;
import co.edu.uco.kudos.servicios.output.ports.serviciosexternos.PublicadorDeEventos;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.AbstractMockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMultipartHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.RequestPostProcessor;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;
import tools.jackson.databind.json.JsonMapper;

/**
 * Publicar Kudo de punta a punta contra PostgreSQL y Redis reales: entrada HTTP (gateway + JWT), transacción con
 * outbox, auditoría, invalidación de caché, feed y relevo. Blob, Table y Event Grid no tienen emulador completo: se
 * reemplazan por dobles en memoria (sus adaptadores tienen pruebas propias).
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("prueba")
@Testcontainers
class PublicarKudoIntegracionTest {

    private static final String GATEWAY = "X-Kudos-Gateway";
    private static final String SECRETO = "secreto-de-prueba";

    @Container
    @ServiceConnection
    static final PostgreSQLContainer POSTGRES = new PostgreSQLContainer("postgres:18-alpine");

    @Container
    @ServiceConnection(name = "redis")
    static final GenericContainer<?> REDIS = new GenericContainer<>("redis:8-alpine").withExposedPorts(6379);

    @TestConfiguration
    static class DoblesDeAzure {

        @Bean
        @Primary
        Dobles.Almacen almacenDePrueba() {
            return new Dobles.Almacen();
        }

        @Bean
        @Primary
        Dobles.RegistroDeAuditoria auditoriaDePrueba() {
            return new Dobles.RegistroDeAuditoria();
        }

        @Bean
        @Primary
        Dobles.Publicador publicadorDePrueba() {
            return new Dobles.Publicador();
        }
    }

    @Autowired
    private MockMvc mvc;
    @Autowired
    private JdbcTemplate jdbc;
    @Autowired
    private JsonMapper json;
    @Autowired
    private PublicarEventosPendientesInputPort relevo;
    @Autowired
    private AlmacenAdjuntos almacen;
    @Autowired
    private Auditoria auditoria;
    @Autowired
    private PublicadorDeEventos publicador;

    private final UUID ana = UUID.randomUUID();
    private final UUID luis = UUID.randomUUID();

    @BeforeEach
    void registrarALuis() throws Exception {
        mvc.perform(conGateway(get("/api/kudos")).with(token(luis, "Luis"))).andExpect(status().isOk());
    }

    private static <B extends AbstractMockHttpServletRequestBuilder<B>> B conGateway(B peticion) {
        return peticion.header(GATEWAY, SECRETO);
    }

    private static RequestPostProcessor token(UUID id, String nombre) {
        return jwt().jwt(j -> j.claim("oid", id.toString()).claim("name", nombre)
                .claim("emails", List.of(nombre.toLowerCase() + "@uco.edu.co")).claim("roles", List.of("ESTUDIANTE")));
    }

    private MockMultipartHttpServletRequestBuilder publicar(UUID destinatario, String contenido) {
        return conGateway(multipart("/api/kudos").with(csrf())
                .param("destinatarioId", destinatario.toString())
                .param("categoriaId", "1")
                .param("contenido", contenido))
                .with(token(ana, "Ana"));
    }

    @Test
    void publicaConAdjuntoYElRelevoEntregaElEventoConElMismoCorrelationId() throws Exception {
        UUID correlacion = UUID.randomUUID();
        MockMultipartFile foto = new MockMultipartFile("adjunto", "foto.exe", "application/octet-stream", Dobles.PNG);

        String cuerpo = mvc.perform(conGateway(multipart("/api/kudos").with(csrf()).file(foto)
                        .param("destinatarioId", luis.toString()).param("categoriaId", "1")
                        .param("contenido", "¡Gracias por la tutoría!"))
                        .header("X-Correlation-Id", correlacion.toString())
                        .with(token(ana, "Ana")))
                .andExpect(status().isCreated())
                .andExpect(header().string("X-Correlation-Id", correlacion.toString()))
                .andReturn().getResponse().getContentAsString();
        UUID kudoId = UUID.fromString(json.readTree(cuerpo).get("id").asString());

        assertThat(jdbc.queryForObject("select count(*) from adjunto where kudo_id = ?", Integer.class, kudoId))
                .isEqualTo(1);
        assertThat(((Dobles.Almacen) almacen).archivos).anySatisfy((clave, bytes) -> {
            assertThat(clave).startsWith("kudos/" + kudoId + "/");
            assertThat(bytes).isEqualTo(Dobles.PNG);
        });
        assertThat(((Dobles.RegistroDeAuditoria) auditoria).registros)
                .anyMatch(r -> r.accion().equals(RegistroAuditoriaDTO.KUDO_GUARDADO)
                        && r.correlationId().equals(correlacion));

        assertThat(relevo.publicarPendientes()).isPositive();

        EventoKudoPublicadoDTO evento = ((Dobles.Publicador) publicador).publicados.stream()
                .filter(e -> e.kudoId().equals(kudoId)).findFirst().orElseThrow();
        assertThat(evento.correlationId()).isEqualTo(correlacion);
        assertThat(evento.destinatario().correo()).isEqualTo("luis@uco.edu.co");
        assertThat(jdbc.queryForObject("select enviado_en is not null from outbox_evento where id = ?",
                Boolean.class, evento.eventoId())).isTrue();
    }

    @Test
    void elFeedMuestraElKudoNuevoAunqueLaPrimeraPaginaEstuvieraEnCache() throws Exception {
        String unico = "Kudo " + UUID.randomUUID();
        mvc.perform(conGateway(get("/api/kudos")).with(token(ana, "Ana"))).andExpect(status().isOk());

        mvc.perform(publicar(luis, unico)).andExpect(status().isCreated());

        mvc.perform(conGateway(get("/api/kudos").param("tamano", "10")).with(token(ana, "Ana")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.elementos[0].contenido").value(unico))
                .andExpect(jsonPath("$.elementos[0].destinatarioId").value(luis.toString()));
    }

    @Test
    void elFeedPaginaConCursorSinRepetirKudos() throws Exception {
        for (int i = 0; i < 3; i++) {
            mvc.perform(publicar(luis, "Paginado " + i)).andExpect(status().isCreated());
        }

        String primera = mvc.perform(conGateway(get("/api/kudos").param("tamano", "2")).with(token(ana, "Ana")))
                .andExpect(jsonPath("$.elementos.length()").value(2))
                .andReturn().getResponse().getContentAsString();
        String cursor = json.readTree(primera).get("siguienteCursor").asString();
        String idUltimo = json.readTree(primera).get("elementos").get(1).get("id").asString();

        mvc.perform(conGateway(get("/api/kudos").param("tamano", "2").param("cursor", cursor))
                        .with(token(ana, "Ana")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.elementos[0].id").value(org.hamcrest.Matchers.not(idUltimo)));
    }

    @Test
    void rechazaReglasDelNegocioCon422YSuCodigo() throws Exception {
        mvc.perform(publicar(UUID.randomUUID(), "Para alguien que nunca entró"))
                .andExpect(status().isUnprocessableContent())
                .andExpect(jsonPath("$.codigo").value("DESTINATARIO_NO_EXISTE"));
        mvc.perform(publicar(ana, "Para mí"))
                .andExpect(status().isUnprocessableContent())
                .andExpect(jsonPath("$.codigo").value("AUTORRECONOCIMIENTO"));

        MockMultipartFile falso = new MockMultipartFile("adjunto", "foto.png", "image/png", new byte[] {0x4D, 0x5A, 0});
        mvc.perform(conGateway(multipart("/api/kudos").with(csrf()).file(falso).param("destinatarioId", luis.toString())
                        .param("categoriaId", "1").param("contenido", "Hola")).with(token(ana, "Ana")))
                .andExpect(status().isUnprocessableContent())
                .andExpect(jsonPath("$.codigo").value("TIPO_DE_ADJUNTO_NO_PERMITIDO"));
        mvc.perform(conGateway(get("/api/kudos").param("cursor", "manipulado")).with(token(ana, "Ana")))
                .andExpect(status().isUnprocessableContent())
                .andExpect(jsonPath("$.codigo").value("CURSOR_INVALIDO"));
    }

    @Test
    void unaSolicitudMalFormadaEs400() throws Exception {
        mvc.perform(conGateway(multipart("/api/kudos").with(csrf()).param("destinatarioId", "no-es-uuid")
                        .param("categoriaId", "1").param("contenido", "Hola")).with(token(ana, "Ana")))
                .andExpect(status().isBadRequest());
        mvc.perform(conGateway(multipart("/api/kudos").with(csrf()).param("categoriaId", "1").param("contenido", "Hola"))
                        .with(token(ana, "Ana")))
                .andExpect(status().isBadRequest());
    }

    @Test
    void sinCabeceraDelGatewayEs403SinTokenEs401YSinRolEs403() throws Exception {
        mvc.perform(get("/api/kudos").with(token(ana, "Ana"))).andExpect(status().isForbidden())
                .andExpect(jsonPath("$.title").value("Acceso denegado"));
        mvc.perform(get("/api/kudos").header(GATEWAY, "secreto-falso").with(token(ana, "Ana")))
                .andExpect(status().isForbidden());
        mvc.perform(conGateway(get("/api/kudos"))).andExpect(status().isUnauthorized());
        mvc.perform(conGateway(get("/api/kudos")).with(jwt().jwt(j -> j.claim("oid", ana.toString()))))
                .andExpect(status().isForbidden());
    }

    @Test
    void laSaludEsPublicaYLasCategoriasSalenDeFlyway() throws Exception {
        mvc.perform(get("/actuator/health")).andExpect(status().isOk()).andExpect(jsonPath("$.status").value("UP"));
        mvc.perform(conGateway(get("/api/categorias")).with(token(ana, "Ana")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(5));
    }

    @Test
    void unCorrelationIdMalFormadoSeReemplaza() throws Exception {
        String devuelto = mvc.perform(conGateway(get("/api/categorias")).header("X-Correlation-Id", "x")
                        .with(token(ana, "Ana")))
                .andReturn().getResponse().getHeader("X-Correlation-Id");

        assertThat(UUID.fromString(devuelto)).isNotNull();
    }
}
