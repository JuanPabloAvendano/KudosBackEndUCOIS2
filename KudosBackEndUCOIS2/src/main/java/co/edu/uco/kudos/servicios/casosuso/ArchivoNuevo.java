package co.edu.uco.kudos.servicios.casosuso;

import java.io.InputStream;
import java.util.function.Supplier;

/** Archivo que acompaña un Kudo nuevo; {@code contenido} abre un flujo nuevo en cada llamada. */
public record ArchivoNuevo(long tamano, Supplier<InputStream> contenido) {
}
