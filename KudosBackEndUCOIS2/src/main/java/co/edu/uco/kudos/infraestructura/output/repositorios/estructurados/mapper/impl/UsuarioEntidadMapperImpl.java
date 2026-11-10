package co.edu.uco.kudos.infraestructura.output.repositorios.estructurados.mapper.impl;

import co.edu.uco.kudos.dominio.usuario.Rol;
import co.edu.uco.kudos.dominio.usuario.Usuario;
import co.edu.uco.kudos.dominio.usuario.UsuarioId;
import co.edu.uco.kudos.infraestructura.output.repositorios.estructurados.jpa.entidades.UsuarioEntidadJPA;
import co.edu.uco.kudos.infraestructura.output.repositorios.estructurados.mapper.UsuarioEntidadMapper;
import org.springframework.stereotype.Component;

/** Reconstruye el Usuario del dominio desde su fila; un rol desconocido en la base de datos es un error de datos. */
@Component
public class UsuarioEntidadMapperImpl implements UsuarioEntidadMapper {

    @Override
    public Usuario aDominio(UsuarioEntidadJPA entidad) {
        Rol rol = Rol.desde(entidad.getRol())
                .orElseThrow(() -> new IllegalStateException("Rol desconocido en la base de datos: " + entidad.getRol()));
        return Usuario.desdeIdentidad(new UsuarioId(entidad.getId()), entidad.getNombre(), entidad.getCorreo(),
                entidad.getTelefono(), rol);
    }
}
