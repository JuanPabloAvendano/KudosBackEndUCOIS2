package co.edu.uco.kudos.servicios.input.adapter;

import co.edu.uco.kudos.servicios.casosuso.ConsultarCategoriasCasoUso;
import co.edu.uco.kudos.servicios.input.dto.response.CategoriaResponse;
import co.edu.uco.kudos.servicios.input.ports.ConsultarCategoriasInputPort;
import java.util.List;

/** Entrega las categorías activas como DTO de respuesta. */
public final class ConsultarCategoriasInputAdapter implements ConsultarCategoriasInputPort {

    private final ConsultarCategoriasCasoUso consultarCategorias;

    public ConsultarCategoriasInputAdapter(ConsultarCategoriasCasoUso consultarCategorias) {
        this.consultarCategorias = consultarCategorias;
    }

    @Override
    public List<CategoriaResponse> consultar() {
        return consultarCategorias.ejecutar().stream()
                .map(categoria -> new CategoriaResponse(categoria.id().valor(), categoria.nombre()))
                .toList();
    }
}
