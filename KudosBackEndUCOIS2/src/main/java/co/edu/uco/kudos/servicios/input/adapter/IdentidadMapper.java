package co.edu.uco.kudos.servicios.input.adapter;

import co.edu.uco.kudos.dominio.usuario.Rol;
import co.edu.uco.kudos.dominio.usuario.Usuario;
import co.edu.uco.kudos.dominio.usuario.UsuarioId;
import co.edu.uco.kudos.servicios.input.dto.request.IdentidadRequest;

/** Traduce la identidad del token al usuario del dominio. */
final class IdentidadMapper {

    private IdentidadMapper() {
    }

    static Usuario aUsuario(IdentidadRequest identidad) {
        Rol rol = Rol.desde(identidad.rol())
                .orElseThrow(() -> new IllegalArgumentException("El token no trae un rol institucional válido"));
        return Usuario.desdeIdentidad(new UsuarioId(identidad.id()), identidad.nombre(), identidad.correo(),
                identidad.telefono(), rol);
    }
}
