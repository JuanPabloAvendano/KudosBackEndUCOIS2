package co.edu.uco.kudos.dominio.usuario;

import java.util.Objects;
import java.util.UUID;

/** Identificador del usuario: el {@code oid} que emite el Identity Provider (no se genera aquí). */
public record UsuarioId(UUID valor) {

    public UsuarioId {
        Objects.requireNonNull(valor, "El id del usuario es obligatorio");
    }

    public static UsuarioId de(String valor) {
        return new UsuarioId(UUID.fromString(valor));
    }
}
