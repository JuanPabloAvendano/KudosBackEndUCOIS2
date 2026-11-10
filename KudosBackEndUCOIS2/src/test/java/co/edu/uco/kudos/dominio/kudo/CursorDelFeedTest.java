package co.edu.uco.kudos.dominio.kudo;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import co.edu.uco.kudos.dominio.reglas.CodigoRegla;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.Test;

/** Pruebas del cursor del feed: ida y vuelta sin perder la posición y rechazo de cursores manipulados. */
class CursorDelFeedTest {

    @Test
    void codificaYDecodificaSinPerderLaPosicion() {
        CursorDelFeed cursor = new CursorDelFeed(Instant.parse("2026-11-10T15:00:00.123Z"),
                new KudoId(UUID.randomUUID()));

        assertThat(CursorDelFeed.decodificar(cursor.codificar())).isEqualTo(cursor);
    }

    @Test
    void rechazaUnCursorManipulado() {
        assertThatThrownBy(() -> CursorDelFeed.decodificar("no-es-un-cursor"))
                .extracting("codigo").isEqualTo(CodigoRegla.CURSOR_INVALIDO);
    }
}
