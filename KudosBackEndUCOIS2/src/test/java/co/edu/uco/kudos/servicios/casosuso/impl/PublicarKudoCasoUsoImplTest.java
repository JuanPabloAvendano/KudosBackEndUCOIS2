package co.edu.uco.kudos.servicios.casosuso.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import co.edu.uco.kudos.dobles.Dobles;
import co.edu.uco.kudos.dominio.categoria.Categoria;
import co.edu.uco.kudos.dominio.categoria.CategoriaId;
import co.edu.uco.kudos.dominio.kudo.Kudo;
import co.edu.uco.kudos.dominio.kudo.TipoDeArchivo;
import co.edu.uco.kudos.dominio.reglas.CodigoRegla;
import co.edu.uco.kudos.dominio.usuario.Usuario;
import co.edu.uco.kudos.dominio.usuario.UsuarioId;
import co.edu.uco.kudos.servicios.casosuso.ArchivoNuevo;
import co.edu.uco.kudos.servicios.casosuso.SolicitudDePublicacion;
import co.edu.uco.kudos.servicios.output.adapter.impl.EventoKudoPublicadoOutputAdapterImpl;
import co.edu.uco.kudos.servicios.output.dto.EventoKudoPublicadoDTO;
import co.edu.uco.kudos.servicios.output.dto.RegistroAuditoriaDTO;
import java.io.ByteArrayInputStream;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/** Pruebas de Publicar Kudo con dobles en cada puerto: transacción, adjunto, efectos posteriores y reglas. */
class PublicarKudoCasoUsoImplTest {

    private final Usuario ana = Dobles.usuario("Ana");
    private final Usuario luis = Dobles.usuario("Luis");
    private final UUID correlacion = UUID.randomUUID();

    private Dobles.Usuarios usuarios;
    private Dobles.Kudos kudos;
    private Dobles.Outbox outbox;
    private Dobles.Almacen almacen;
    private Dobles.Cache cache;
    private Dobles.RegistroDeAuditoria auditoria;
    private Dobles.UnidadInmediata unidad;
    private PublicarKudoCasoUsoImpl casoUso;

    @BeforeEach
    void preparar() {
        usuarios = new Dobles.Usuarios().con(ana, luis);
        Dobles.Categorias categorias = new Dobles.Categorias().con(
                new Categoria(new CategoriaId(1), "Liderazgo", true),
                new Categoria(new CategoriaId(2), "Retirada", false));
        kudos = new Dobles.Kudos();
        outbox = new Dobles.Outbox();
        almacen = new Dobles.Almacen();
        cache = new Dobles.Cache();
        auditoria = new Dobles.RegistroDeAuditoria();
        unidad = new Dobles.UnidadInmediata();
        Dobles.IdsSecuenciales ids = new Dobles.IdsSecuenciales();
        Dobles.RelojFijo reloj = new Dobles.RelojFijo();
        PreparacionDelKudo preparacion = new PreparacionDelKudo(usuarios, categorias, ids, reloj,
                new Dobles.Parametros(), new EventoKudoPublicadoOutputAdapterImpl());
        casoUso = new PublicarKudoCasoUsoImpl(preparacion, almacen, kudos, outbox, unidad,
                new EfectosPosteriores(auditoria, cache, ids, reloj));
    }

    private SolicitudDePublicacion solicitud(UsuarioId destinatario, int categoria, ArchivoNuevo archivo) {
        return new SolicitudDePublicacion(ana.id(), destinatario, new CategoriaId(categoria), "¡Gracias!", archivo,
                correlacion);
    }

    private static ArchivoNuevo png() {
        return new ArchivoNuevo(Dobles.PNG.length, () -> new ByteArrayInputStream(Dobles.PNG));
    }

    @Test
    void guardaElKudoYSuEventoEnUnaSolaTransaccion() {
        Kudo kudo = casoUso.ejecutar(solicitud(luis.id(), 1, null));

        assertThat(unidad.transacciones).isEqualTo(1);
        assertThat(kudos.filas).containsExactly(kudo);
        assertThat(kudo.fecha()).isEqualTo(Dobles.AHORA);
        EventoKudoPublicadoDTO evento = outbox.pendientes.getFirst();
        assertThat(evento.kudoId()).isEqualTo(kudo.id().valor());
        assertThat(evento.autor()).isEqualTo("Ana");
        assertThat(evento.categoria()).isEqualTo("Liderazgo");
        assertThat(evento.destinatario().correo()).isEqualTo("luis@uco.edu.co");
        assertThat(evento.destinatario().telefono()).isEqualTo("+573000000000");
        assertThat(evento.correlationId()).isEqualTo(correlacion);
        assertThat(evento.eventoId()).isNotEqualTo(kudo.id().valor());
    }

    @Test
    void despuesDelCommitAuditaYLimpiaLaCacheDelFeed() {
        Kudo kudo = casoUso.ejecutar(solicitud(luis.id(), 1, null));

        assertThat(cache.invalidaciones).isEqualTo(1);
        RegistroAuditoriaDTO registro = auditoria.registros.getFirst();
        assertThat(registro.accion()).isEqualTo(RegistroAuditoriaDTO.KUDO_GUARDADO);
        assertThat(registro.actorId()).isEqualTo(ana.id().valor());
        assertThat(registro.recurso()).isEqualTo("kudo/" + kudo.id().valor());
        assertThat(registro.correlationId()).isEqualTo(correlacion);
    }

    @Test
    void subeElAdjuntoAntesDeLaTransaccionConNombreDelServidor() {
        Kudo kudo = casoUso.ejecutar(solicitud(luis.id(), 1, png()));

        assertThat(kudo.adjuntos()).singleElement().satisfies(adjunto -> {
            assertThat(adjunto.tipo()).isEqualTo(TipoDeArchivo.PNG);
            assertThat(almacen.archivos.get(adjunto.claveArchivo())).isEqualTo(Dobles.PNG);
        });
    }

    @Test
    void siLaTransaccionFallaBorraElAdjuntoHuerfanoYPropagaElError() {
        unidad.fallarAlConfirmar = true;

        assertThatThrownBy(() -> casoUso.ejecutar(solicitud(luis.id(), 1, png())))
                .isInstanceOf(IllegalStateException.class);
        assertThat(almacen.eliminados).hasSize(1);
        assertThat(almacen.archivos).isEmpty();
        assertThat(auditoria.registros).isEmpty();
    }

    @Test
    void siFallanLaAuditoriaYLaCacheElKudoQuedaPublicado() {
        auditoria.caida = true;
        cache.caida = true;

        Kudo kudo = casoUso.ejecutar(solicitud(luis.id(), 1, null));

        assertThat(kudos.filas).containsExactly(kudo);
    }

    @Test
    void rechazaUnDestinatarioQueNuncaEntro() {
        UsuarioId desconocido = new UsuarioId(UUID.randomUUID());

        assertThatThrownBy(() -> casoUso.ejecutar(solicitud(desconocido, 1, null)))
                .extracting("codigo").isEqualTo(CodigoRegla.DESTINATARIO_NO_EXISTE);
        assertThat(unidad.transacciones).isZero();
    }

    @Test
    void rechazaCategoriasInexistentesOInactivas() {
        assertThatThrownBy(() -> casoUso.ejecutar(solicitud(luis.id(), 99, null)))
                .extracting("codigo").isEqualTo(CodigoRegla.CATEGORIA_NO_EXISTE);
        assertThatThrownBy(() -> casoUso.ejecutar(solicitud(luis.id(), 2, null)))
                .extracting("codigo").isEqualTo(CodigoRegla.CATEGORIA_INACTIVA);
    }

    @Test
    void unAdjuntoFalsoNoSeSubeNiSeGuarda() {
        byte[] exe = {0x4D, 0x5A, 0, 0, 0, 0, 0, 0};
        ArchivoNuevo falso = new ArchivoNuevo(exe.length, () -> new ByteArrayInputStream(exe));

        assertThatThrownBy(() -> casoUso.ejecutar(solicitud(luis.id(), 1, falso)))
                .extracting("codigo").isEqualTo(CodigoRegla.TIPO_DE_ADJUNTO_NO_PERMITIDO);
        assertThat(almacen.archivos).isEmpty();
        assertThat(kudos.filas).isEmpty();
    }

    @Test
    void elAutorDebeEstarRegistrado() {
        usuarios.filas.remove(ana.id());

        assertThatThrownBy(() -> casoUso.ejecutar(solicitud(luis.id(), 1, null)))
                .isInstanceOf(IllegalStateException.class);
    }
}
