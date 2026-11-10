package co.edu.uco.kudos.infraestructura.input.controladores;

import co.edu.uco.kudos.servicios.input.dto.response.CategoriaResponse;
import co.edu.uco.kudos.servicios.input.ports.ConsultarCategoriasInputPort;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Categorías activas para el formulario de publicar. */
@RestController
@RequestMapping("/api/categorias")
public class CategoriaControlador {

    private final ConsultarCategoriasInputPort consultarCategorias;

    public CategoriaControlador(ConsultarCategoriasInputPort consultarCategorias) {
        this.consultarCategorias = consultarCategorias;
    }

    @GetMapping
    public List<CategoriaResponse> activas() {
        return consultarCategorias.consultar();
    }
}
