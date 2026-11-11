package co.edu.uco.kudos.servicios.input.ports;

import co.edu.uco.kudos.servicios.input.dto.request.IdentidadRequest;
import co.edu.uco.kudos.servicios.input.dto.response.UsuarioResponse;
import java.util.List;

/** Consultar las personas registradas, para elegir a quién reconocer y mostrar nombres en el feed. */
public interface ConsultarUsuariosInputPort {

    List<UsuarioResponse> consultar(IdentidadRequest identidad);
}
