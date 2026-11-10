package co.edu.uco.kudos.dominio.kudo;

import java.util.Objects;
import java.util.UUID;

/** Identificador del adjunto. */
public record AdjuntoId(UUID valor) {

    public AdjuntoId {
        Objects.requireNonNull(valor, "El id del adjunto es obligatorio");
    }
}
