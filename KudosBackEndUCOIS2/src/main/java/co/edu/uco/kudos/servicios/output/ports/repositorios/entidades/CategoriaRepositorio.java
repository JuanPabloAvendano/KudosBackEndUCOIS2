package co.edu.uco.kudos.servicios.output.ports.repositorios.entidades;

import co.edu.uco.kudos.dominio.categoria.Categoria;
import co.edu.uco.kudos.dominio.categoria.CategoriaId;
import java.util.List;
import java.util.Optional;

/** Categorías. */
public interface CategoriaRepositorio {

    Optional<Categoria> porId(CategoriaId id);

    List<Categoria> activas();
}
