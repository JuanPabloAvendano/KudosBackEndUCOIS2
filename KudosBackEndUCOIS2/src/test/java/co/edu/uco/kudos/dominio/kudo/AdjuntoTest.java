package co.edu.uco.kudos.dominio.kudo;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import co.edu.uco.kudos.dominio.reglas.CodigoRegla;
import java.util.EnumSet;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.Test;

/** Pruebas del adjunto: nombre en el servidor, tipo real por magic bytes, tamaño y archivos vacíos. */
class AdjuntoTest {

    private static final KudoId KUDO = new KudoId(UUID.fromString("0190a000-0000-7000-8000-000000000001"));
    private static final AdjuntoId ID = new AdjuntoId(UUID.fromString("0190a000-0000-7000-8000-000000000002"));
    private static final byte[] PNG = {(byte) 0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A};
    private static final byte[] EXE = {0x4D, 0x5A, (byte) 0x90, 0, 3, 0, 0, 0};
    private static final Set<TipoDeArchivo> IMAGENES = EnumSet.of(TipoDeArchivo.PNG, TipoDeArchivo.JPEG);

    @Test
    void nombraElArchivoEnElServidorYReconoceElTipoReal() {
        Adjunto adjunto = Adjunto.nuevo(KUDO, ID, PNG, 500, 1000, IMAGENES);

        assertThat(adjunto.claveArchivo()).isEqualTo("kudos/" + KUDO.valor() + "/" + ID.valor());
        assertThat(adjunto.tipo()).isEqualTo(TipoDeArchivo.PNG);
        assertThat(adjunto.tamano()).isEqualTo(500);
    }

    @Test
    void rechazaUnEjecutableRenombrado() {
        assertThatThrownBy(() -> Adjunto.nuevo(KUDO, ID, EXE, 500, 1000, IMAGENES))
                .extracting("codigo").isEqualTo(CodigoRegla.TIPO_DE_ADJUNTO_NO_PERMITIDO);
    }

    @Test
    void rechazaUnTipoRealQueNoEstaPermitido() {
        byte[] pdf = {0x25, 0x50, 0x44, 0x46, 0x2D, 0x31, 0x2E, 0x37};
        assertThatThrownBy(() -> Adjunto.nuevo(KUDO, ID, pdf, 500, 1000, IMAGENES))
                .extracting("codigo").isEqualTo(CodigoRegla.TIPO_DE_ADJUNTO_NO_PERMITIDO);
    }

    @Test
    void rechazaUnArchivoMayorAlLimite() {
        assertThatThrownBy(() -> Adjunto.nuevo(KUDO, ID, PNG, 1001, 1000, IMAGENES))
                .extracting("codigo").isEqualTo(CodigoRegla.ADJUNTO_MUY_GRANDE);
    }

    @Test
    void rechazaUnArchivoVacio() {
        assertThatThrownBy(() -> Adjunto.nuevo(KUDO, ID, new byte[0], 0, 1000, IMAGENES))
                .extracting("codigo").isEqualTo(CodigoRegla.ADJUNTO_VACIO);
    }

    @Test
    void exigeUnaClaveDeArchivo() {
        assertThatThrownBy(() -> new Adjunto(ID, " ", TipoDeArchivo.PNG, 1))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
