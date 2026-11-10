package co.edu.uco.kudos.servicios.input.ports;

import co.edu.uco.kudos.servicios.input.dto.response.CategoriaResponse;
import java.util.List;

/** Consultar las categorías activas. */
public interface ConsultarCategoriasInputPort {

    List<CategoriaResponse> consultar();
}
