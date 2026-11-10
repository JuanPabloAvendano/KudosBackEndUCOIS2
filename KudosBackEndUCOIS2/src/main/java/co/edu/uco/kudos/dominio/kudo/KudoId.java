package co.edu.uco.kudos.dominio.kudo;

import java.util.Objects;
import java.util.UUID;

/** Identificador del Kudo (UUID v7, ordenable en el tiempo). */
public record KudoId(UUID valor) {

    public KudoId {
        Objects.requireNonNull(valor, "El id del Kudo es obligatorio");
    }
}
