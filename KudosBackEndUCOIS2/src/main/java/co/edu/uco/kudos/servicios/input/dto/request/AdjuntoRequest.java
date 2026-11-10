package co.edu.uco.kudos.servicios.input.dto.request;

import java.io.InputStream;
import java.util.function.Supplier;

/**
 * Archivo recibido con el Kudo. El contenido se abre bajo demanda para no cargar el archivo completo en memoria.
 *
 * @param tipoDeclarado tipo que dice el cliente; solo informativo, se valida el contenido real
 */
public record AdjuntoRequest(String tipoDeclarado, long tamano, Supplier<InputStream> contenido) {
}
