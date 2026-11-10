package co.edu.uco.kudos.infraestructura.output.repositorios.estructurados;

import co.edu.uco.kudos.dominio.categoria.Categoria;
import co.edu.uco.kudos.dominio.categoria.CategoriaId;
import co.edu.uco.kudos.infraestructura.output.repositorios.estructurados.jpa.CategoriaJPARepositorio;
import co.edu.uco.kudos.infraestructura.output.repositorios.estructurados.mapper.CategoriaEntidadMapper;
import co.edu.uco.kudos.servicios.output.ports.repositorios.entidades.CategoriaRepositorio;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Component;

/** Categorías en PostgreSQL. */
@Component
public class CategoriaRepositorioAdapter implements CategoriaRepositorio {

    private final CategoriaJPARepositorio jpa;
    private final CategoriaEntidadMapper mapper;

    public CategoriaRepositorioAdapter(CategoriaJPARepositorio jpa, CategoriaEntidadMapper mapper) {
        this.jpa = jpa;
        this.mapper = mapper;
    }

    @Override
    public Optional<Categoria> porId(CategoriaId id) {
        return jpa.findById(id.valor()).map(mapper::aDominio);
    }

    @Override
    public List<Categoria> activas() {
        return jpa.findByActivaTrueOrderByNombre().stream().map(mapper::aDominio).toList();
    }

}
