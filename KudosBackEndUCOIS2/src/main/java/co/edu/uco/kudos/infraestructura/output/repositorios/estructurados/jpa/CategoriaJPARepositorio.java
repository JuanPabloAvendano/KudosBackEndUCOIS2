package co.edu.uco.kudos.infraestructura.output.repositorios.estructurados.jpa;

import co.edu.uco.kudos.infraestructura.output.repositorios.estructurados.jpa.entidades.CategoriaEntidadJPA;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

/** Consultas de Spring Data sobre la tabla {@code categoria}. */
public interface CategoriaJPARepositorio extends JpaRepository<CategoriaEntidadJPA, Integer> {

    List<CategoriaEntidadJPA> findByActivaTrueOrderByNombre();
}
