package co.edu.uco.kudos.servicios.input.adapter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import co.edu.uco.kudos.dominio.categoria.Categoria;
import co.edu.uco.kudos.dominio.categoria.CategoriaId;
import co.edu.uco.kudos.dominio.kudo.Adjunto;
import co.edu.uco.kudos.dominio.kudo.AdjuntoId;
import co.edu.uco.kudos.dominio.kudo.CursorDelFeed;
import co.edu.uco.kudos.dominio.kudo.Kudo;
import co.edu.uco.kudos.dominio.kudo.KudoId;
import co.edu.uco.kudos.dominio.kudo.TipoDeArchivo;
import co.edu.uco.kudos.dominio.usuario.Rol;
import co.edu.uco.kudos.dominio.usuario.Usuario;
import co.edu.uco.kudos.dominio.usuario.UsuarioId;
import co.edu.uco.kudos.servicios.casosuso.KudoConEnlaces;
import co.edu.uco.kudos.servicios.casosuso.SolicitudDePublicacion;
import co.edu.uco.kudos.servicios.input.dto.request.AdjuntoRequest;
import co.edu.uco.kudos.servicios.input.dto.request.IdentidadRequest;
import co.edu.uco.kudos.servicios.input.dto.request.PublicarKudoRequest;
import co.edu.uco.kudos.servicios.input.dto.response.ConexionPushResponse;
import co.edu.uco.kudos.servicios.input.dto.response.KudoResponse;
import co.edu.uco.kudos.servicios.input.dto.response.PaginaResponse;
import co.edu.uco.kudos.servicios.input.dto.response.PublicarKudoResponse;
import co.edu.uco.kudos.servicios.input.dto.response.UsuarioResponse;
import co.edu.uco.kudos.transversal.Pagina;
import java.io.ByteArrayInputStream;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.Test;

/** Pruebas de los adaptadores de entrada: traducción de DTO, cursor y validaciones. */
class InputAdaptersTest {

    private static final IdentidadRequest ANA = new IdentidadRequest(UUID.randomUUID(), "Ana", "ana@uco.edu.co",
            null, "docente");
    private static final Instant FECHA = Instant.parse("2026-11-10T15:00:00Z");

    private final List<Usuario> registrados = new ArrayList<>();

    private Usuario registrar(Usuario usuario) {
        registrados.add(usuario);
        return usuario;
    }

    @Test
    void publicarRegistraAlAutorYTraduceLaSolicitud() {
        AtomicReference<SolicitudDePublicacion> recibida = new AtomicReference<>();
        PublicarKudoInputAdapter adapter = new PublicarKudoInputAdapter(this::registrar, solicitud -> {
            recibida.set(solicitud);
            return Kudo.reconstruir(new KudoId(new UUID(0, 1)), solicitud.autor(), solicitud.destinatario(),
                    solicitud.categoria(), solicitud.contenido(), FECHA, List.of());
        });
        UUID luis = UUID.randomUUID();
        AdjuntoRequest adjunto = new AdjuntoRequest("image/png", 3, () -> new ByteArrayInputStream(new byte[3]));

        PublicarKudoResponse respuesta = adapter.publicar(ANA,
                new PublicarKudoRequest(luis, 4, "Gracias", adjunto, UUID.randomUUID()));

        assertThat(respuesta).isEqualTo(new PublicarKudoResponse(new UUID(0, 1), FECHA));
        assertThat(registrados).singleElement().satisfies(u -> assertThat(u.rol()).isEqualTo(Rol.DOCENTE));
        assertThat(recibida.get().destinatario()).isEqualTo(new UsuarioId(luis));
        assertThat(recibida.get().categoria()).isEqualTo(new CategoriaId(4));
        assertThat(recibida.get().archivoOpcional()).hasValueSatisfying(a -> assertThat(a.tamano()).isEqualTo(3));
    }

    @Test
    void publicarSinDestinatarioOConUnRolDesconocidoEsUnaSolicitudInvalida() {
        PublicarKudoInputAdapter adapter = new PublicarKudoInputAdapter(this::registrar, solicitud -> null);
        IdentidadRequest invitado = new IdentidadRequest(UUID.randomUUID(), "X", "x@uco.edu.co", null, "invitado");
        PublicarKudoRequest sinDestinatario = new PublicarKudoRequest(null, 1, "Hola", null, null);
        PublicarKudoRequest completa = new PublicarKudoRequest(UUID.randomUUID(), 1, "Hola", null, null);

        assertThatThrownBy(() -> adapter.publicar(ANA, sinDestinatario)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> adapter.publicar(invitado, completa)).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void elFeedTraduceCursorYAdjuntosConSuEnlace() {
        Adjunto adjunto = new Adjunto(new AdjuntoId(new UUID(5, 5)), "kudos/a/b", TipoDeArchivo.PNG, 7);
        Kudo kudo = Kudo.reconstruir(new KudoId(new UUID(0, 2)), new UsuarioId(new UUID(1, 1)),
                new UsuarioId(new UUID(2, 2)), new CategoriaId(3), "Hola", FECHA, List.of(adjunto));
        AtomicReference<CursorDelFeed> cursorRecibido = new AtomicReference<>();
        ConsultarFeedInputAdapter adapter = new ConsultarFeedInputAdapter(this::registrar, (cursor, tamano) -> {
            cursorRecibido.set(cursor);
            return new Pagina<>(List.of(new KudoConEnlaces(kudo, Map.of(adjunto.id(), "https://enlace"))), "sig");
        });
        String cursor = CursorDelFeed.despuesDe(kudo).codificar();

        PaginaResponse<KudoResponse> pagina = adapter.consultar(ANA, cursor, 5);

        assertThat(cursorRecibido.get()).isEqualTo(CursorDelFeed.despuesDe(kudo));
        assertThat(pagina.siguienteCursor()).isEqualTo("sig");
        KudoResponse respuesta = pagina.elementos().getFirst();
        assertThat(respuesta.categoriaId()).isEqualTo(3);
        assertThat(respuesta.adjuntos()).singleElement().satisfies(a -> {
            assertThat(a.enlace()).isEqualTo("https://enlace");
            assertThat(a.tipo()).isEqualTo("image/png");
        });
        assertThat(adapter.consultar(ANA, " ", null).elementos()).hasSize(1);
        assertThat(cursorRecibido.get()).isNull();
    }

    @Test
    void usuariosRegistraAQuienConsultaYSoloEntregaIdYNombre() {
        Usuario luis = Usuario.desdeIdentidad(new UsuarioId(UUID.randomUUID()), "Luis", "luis@uco.edu.co",
                "+57300", Rol.ESTUDIANTE);
        ConsultarUsuariosInputAdapter adapter = new ConsultarUsuariosInputAdapter(this::registrar, () -> List.of(luis));

        List<UsuarioResponse> respuesta = adapter.consultar(ANA);

        assertThat(respuesta).containsExactly(new UsuarioResponse(luis.id().valor(), "Luis"));
        assertThat(registrados).singleElement().satisfies(u -> assertThat(u.id().valor()).isEqualTo(ANA.id()));
    }

    @Test
    void pushRegistraAQuienSeConectaYLeDaSuUrl() {
        AtomicReference<UsuarioId> pedido = new AtomicReference<>();
        ObtenerConexionPushInputAdapter adapter = new ObtenerConexionPushInputAdapter(this::registrar, usuario -> {
            pedido.set(usuario);
            return "wss://push/ana";
        });

        ConexionPushResponse respuesta = adapter.conectar(ANA);

        assertThat(respuesta.url()).isEqualTo("wss://push/ana");
        assertThat(pedido.get()).isEqualTo(new UsuarioId(ANA.id()));
        assertThat(registrados).hasSize(1);
    }

    @Test
    void categoriasYRelevoDelegan() {
        ConsultarCategoriasInputAdapter categorias = new ConsultarCategoriasInputAdapter(
                () -> List.of(new Categoria(new CategoriaId(1), "Liderazgo", true)));
        PublicarEventosPendientesInputAdapter relevo = new PublicarEventosPendientesInputAdapter(() -> 4);

        assertThat(categorias.consultar()).singleElement()
                .satisfies(c -> assertThat(c.nombre()).isEqualTo("Liderazgo"));
        assertThat(relevo.publicarPendientes()).isEqualTo(4);
    }
}
