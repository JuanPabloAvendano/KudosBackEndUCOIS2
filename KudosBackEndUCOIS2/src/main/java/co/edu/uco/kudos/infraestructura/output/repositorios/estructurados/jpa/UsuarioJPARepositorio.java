package co.edu.uco.kudos.infraestructura.output.repositorios.estructurados.jpa;

import co.edu.uco.kudos.infraestructura.output.repositorios.estructurados.jpa.entidades.UsuarioEntidadJPA;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

/** Consultas de Spring Data sobre la tabla {@code usuario}. */
public interface UsuarioJPARepositorio extends JpaRepository<UsuarioEntidadJPA, UUID> {
}
