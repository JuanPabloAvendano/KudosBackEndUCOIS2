package co.edu.uco.kudos.transversal;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;

/** Pruebas de las utilidades transversales: UUID v7 y página. */
class TransversalTest {

    @Test
    void elUuidV7EsVersionSieteYGuardaElInstante() {
        Instant instante = Instant.parse("2026-11-10T15:00:00.123Z");
        UUID id = UuidV7.generar(instante);

        assertThat(id.version()).isEqualTo(7);
        assertThat(id.variant()).isEqualTo(2);
        assertThat(id.getMostSignificantBits() >>> 16).isEqualTo(instante.toEpochMilli());
    }

    @Test
    void laPaginaMapeaSusElementosYConservaElCursor() {
        Pagina<Integer> pagina = new Pagina<>(List.of(1, 2), "siguiente");

        assertThat(pagina.mapear(n -> n * 10).elementos()).containsExactly(10, 20);
        assertThat(pagina.siguiente()).contains("siguiente");
        assertThat(new Pagina<>(List.of(), null).siguiente()).isEmpty();
    }
}
