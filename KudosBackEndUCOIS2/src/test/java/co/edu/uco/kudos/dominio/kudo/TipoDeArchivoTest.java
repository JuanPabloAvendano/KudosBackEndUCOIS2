package co.edu.uco.kudos.dominio.kudo;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

/** Pruebas del reconocimiento de tipos de archivo por su firma. */
class TipoDeArchivoTest {

    @Test
    void reconoceCadaTipoPorSuFirma() {
        assertThat(TipoDeArchivo.detectar(new byte[] {(byte) 0xFF, (byte) 0xD8, (byte) 0xFF, 0})).contains(TipoDeArchivo.JPEG);
        assertThat(TipoDeArchivo.detectar("GIF89a".getBytes())).contains(TipoDeArchivo.GIF);
        assertThat(TipoDeArchivo.detectar("%PDF-1.7".getBytes())).contains(TipoDeArchivo.PDF);
    }

    @Test
    void noReconoceCabecerasCortasNulasODesconocidas() {
        assertThat(TipoDeArchivo.detectar(new byte[] {(byte) 0x89})).isEmpty();
        assertThat(TipoDeArchivo.detectar(null)).isEmpty();
        assertThat(TipoDeArchivo.detectar("hola mundo".getBytes())).isEmpty();
    }

    @Test
    void buscaPorTipoMime() {
        assertThat(TipoDeArchivo.porMime("IMAGE/PNG")).contains(TipoDeArchivo.PNG);
        assertThat(TipoDeArchivo.porMime("text/plain")).isEmpty();
        assertThat(TipoDeArchivo.PDF.mime()).isEqualTo("application/pdf");
    }
}
