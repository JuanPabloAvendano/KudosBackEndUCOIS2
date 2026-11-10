package co.edu.uco.kudos.dominio.kudo;

import java.util.Arrays;
import java.util.Optional;

/**
 * Tipos de adjunto reconocidos por su contenido real (magic bytes), no por la extensión ni por lo que diga el
 * cliente (QA-10). Cuáles se permiten lo decide el Parameter Catalog.
 */
public enum TipoDeArchivo {
    PNG("image/png", new int[] {0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A}),
    JPEG("image/jpeg", new int[] {0xFF, 0xD8, 0xFF}),
    GIF("image/gif", new int[] {0x47, 0x49, 0x46, 0x38}),
    PDF("application/pdf", new int[] {0x25, 0x50, 0x44, 0x46, 0x2D});

    /** Bytes que hacen falta para reconocer cualquier tipo. */
    public static final int BYTES_DE_CABECERA = 8;

    private final String mime;
    private final int[] firma;

    TipoDeArchivo(String mime, int[] firma) {
        this.mime = mime;
        this.firma = firma;
    }

    public String mime() {
        return mime;
    }

    /** Reconoce el tipo por los primeros bytes del archivo. */
    public static Optional<TipoDeArchivo> detectar(byte[] cabecera) {
        if (cabecera == null) {
            return Optional.empty();
        }
        return Arrays.stream(values()).filter(tipo -> tipo.coincide(cabecera)).findFirst();
    }

    public static Optional<TipoDeArchivo> porMime(String mime) {
        return Arrays.stream(values()).filter(tipo -> tipo.mime.equalsIgnoreCase(mime)).findFirst();
    }

    private boolean coincide(byte[] cabecera) {
        if (cabecera.length < firma.length) {
            return false;
        }
        for (int i = 0; i < firma.length; i++) {
            if ((cabecera[i] & 0xFF) != firma[i]) {
                return false;
            }
        }
        return true;
    }
}
