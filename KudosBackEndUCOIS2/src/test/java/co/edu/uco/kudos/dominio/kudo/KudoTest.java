package co.edu.uco.kudos.dominio.kudo;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import co.edu.uco.kudos.dominio.categoria.CategoriaId;
import co.edu.uco.kudos.dominio.reglas.CodigoRegla;
import co.edu.uco.kudos.dominio.reglas.ReglaIncumplida;
import co.edu.uco.kudos.dominio.usuario.UsuarioId;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;

/** Pruebas de las reglas de Kudo: mensaje obligatorio y sin autorreconocimiento. */
class KudoTest {

    private static final UsuarioId ANA = new UsuarioId(UUID.randomUUID());
    private static final UsuarioId LUIS = new UsuarioId(UUID.randomUUID());
    private static final KudoId ID = new KudoId(UUID.randomUUID());
    private static final Instant FECHA = Instant.parse("2026-11-10T15:00:00Z");

    @Test
    void publicaUnKudoValidoYRecortaElMensaje() {
        Kudo kudo = Kudo.publicar(ID, ANA, LUIS, new CategoriaId(1), "  ¡Gracias por tu ayuda!  ", FECHA, List.of());

        assertThat(kudo.contenido()).isEqualTo("¡Gracias por tu ayuda!");
        assertThat(kudo.autor()).isEqualTo(ANA);
        assertThat(kudo.destinatario()).isEqualTo(LUIS);
        assertThat(kudo.categoria().valor()).isEqualTo(1);
        assertThat(kudo.fecha()).isEqualTo(FECHA);
        assertThat(kudo.id()).isEqualTo(ID);
        assertThat(kudo.adjuntos()).isEmpty();
    }

    @Test
    void rechazaUnMensajeVacio() {
        assertThatThrownBy(() -> Kudo.publicar(ID, ANA, LUIS, new CategoriaId(1), "   ", FECHA, List.of()))
                .isInstanceOf(ReglaIncumplida.class)
                .extracting("codigo").isEqualTo(CodigoRegla.CONTENIDO_VACIO);
    }

    @Test
    void rechazaUnMensajeNulo() {
        assertThatThrownBy(() -> Kudo.publicar(ID, ANA, LUIS, new CategoriaId(1), null, FECHA, List.of()))
                .extracting("codigo").isEqualTo(CodigoRegla.CONTENIDO_VACIO);
    }

    @Test
    void nadiePuedeDarseUnKudoASiMismo() {
        assertThatThrownBy(() -> Kudo.publicar(ID, ANA, ANA, new CategoriaId(1), "Yo", FECHA, List.of()))
                .extracting("codigo").isEqualTo(CodigoRegla.AUTORRECONOCIMIENTO);
    }

    @Test
    void reconstruirNoVuelveAValidarYCopiaLosAdjuntos() {
        Adjunto adjunto = new Adjunto(new AdjuntoId(UUID.randomUUID()), "kudos/x/y", TipoDeArchivo.PDF, 10);
        Kudo kudo = Kudo.reconstruir(ID, ANA, ANA, new CategoriaId(2), "", FECHA, List.of(adjunto));

        assertThat(kudo.adjuntos()).containsExactly(adjunto);
        assertThatThrownBy(() -> kudo.adjuntos().add(adjunto)).isInstanceOf(UnsupportedOperationException.class);
    }
}
