package co.edu.uco.kudos.dominio.kudo;

import co.edu.uco.kudos.dominio.usuario.Usuario;
import co.edu.uco.kudos.dominio.usuario.UsuarioId;
import java.util.Objects;
import java.util.Optional;

/** Objeto de valor con los datos de contacto del destinatario que viajan en el evento (copia, no referencia). */
public record Destinatario(UsuarioId id, String nombre, String correo, String telefono) {

    public Destinatario {
        Objects.requireNonNull(id, "El id es obligatorio");
        Objects.requireNonNull(nombre, "El nombre es obligatorio");
        Objects.requireNonNull(correo, "El correo es obligatorio");
    }

    public static Destinatario de(Usuario usuario) {
        return new Destinatario(usuario.id(), usuario.nombre(), usuario.correo(), usuario.telefono().orElse(null));
    }

    public Optional<String> telefonoOpcional() {
        return Optional.ofNullable(telefono);
    }
}
