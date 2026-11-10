package co.edu.uco.kudos.servicios.output.ports.repositorios.entidades;

import co.edu.uco.kudos.dominio.usuario.Usuario;
import co.edu.uco.kudos.dominio.usuario.UsuarioId;
import java.util.Optional;

/** Usuarios registrados desde el token. */
public interface UsuarioRepositorio {

    Optional<Usuario> porId(UsuarioId id);

    /** Inserta o actualiza. */
    void guardar(Usuario usuario);
}
