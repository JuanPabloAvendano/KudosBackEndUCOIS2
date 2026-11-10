package co.edu.uco.kudos.servicios.input.ports;

import co.edu.uco.kudos.servicios.input.dto.request.IdentidadRequest;
import co.edu.uco.kudos.servicios.input.dto.request.PublicarKudoRequest;
import co.edu.uco.kudos.servicios.input.dto.response.PublicarKudoResponse;

/** Publicar un Kudo. */
public interface PublicarKudoInputPort {

    PublicarKudoResponse publicar(IdentidadRequest identidad, PublicarKudoRequest request);
}
