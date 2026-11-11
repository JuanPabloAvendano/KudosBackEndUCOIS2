package co.edu.uco.kudos.servicios.input.ports;

import co.edu.uco.kudos.servicios.input.dto.request.IdentidadRequest;
import co.edu.uco.kudos.servicios.input.dto.response.ConexionPushResponse;

/** Obtener la conexión push de quien llama. */
public interface ObtenerConexionPushInputPort {

    ConexionPushResponse conectar(IdentidadRequest identidad);
}
