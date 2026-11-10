package co.edu.uco.kudos.servicios.input.ports;

import co.edu.uco.kudos.servicios.input.dto.request.IdentidadRequest;
import co.edu.uco.kudos.servicios.input.dto.response.KudoResponse;
import co.edu.uco.kudos.servicios.input.dto.response.PaginaResponse;

/** Consultar el feed de Kudos, del más reciente al más antiguo. */
public interface ConsultarFeedInputPort {

    PaginaResponse<KudoResponse> consultar(IdentidadRequest identidad, String cursor, Integer tamano);
}
