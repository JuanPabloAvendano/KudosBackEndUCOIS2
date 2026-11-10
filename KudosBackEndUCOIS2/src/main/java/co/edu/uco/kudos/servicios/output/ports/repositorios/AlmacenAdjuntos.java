package co.edu.uco.kudos.servicios.output.ports.repositorios;

import java.io.InputStream;
import java.time.Duration;

/** Almacén de archivos adjuntos. */
public interface AlmacenAdjuntos {

    void guardar(String clave, String tipoMime, long tamano, InputStream contenido);

    /** Enlace de solo lectura que vence en {@code vigencia}. */
    String enlaceTemporal(String clave, Duration vigencia);

    void eliminar(String clave);
}
