package co.edu.uco.kudos.infraestructura.output.repositorios.estructurados.mapper;

import co.edu.uco.kudos.dominio.usuario.Usuario;
import co.edu.uco.kudos.infraestructura.output.repositorios.estructurados.jpa.entidades.UsuarioEntidadJPA;

/** Traductor entre el Usuario del dominio y su fila JPA. */
public interface UsuarioEntidadMapper {

    Usuario aDominio(UsuarioEntidadJPA entidad);
}
