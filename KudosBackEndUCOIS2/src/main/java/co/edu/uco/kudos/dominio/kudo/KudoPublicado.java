package co.edu.uco.kudos.dominio.kudo;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * Evento de dominio: se publicó un Kudo. Lleva lo necesario para notificar sin consultar al back (ADR-007); su
 * {@code eventoId} es la clave de idempotencia del Notification Gateway (ADR-009).
 */
public record KudoPublicado(UUID eventoId, KudoId kudoId, String autor, Destinatario destinatario, String categoria,
        Instant publicadoEn, UUID correlationId) {

    public KudoPublicado {
        Objects.requireNonNull(eventoId, "El id del evento es obligatorio");
        Objects.requireNonNull(kudoId, "El id del Kudo es obligatorio");
        Objects.requireNonNull(autor, "El autor es obligatorio");
        Objects.requireNonNull(destinatario, "El destinatario es obligatorio");
        Objects.requireNonNull(categoria, "La categoría es obligatoria");
        Objects.requireNonNull(publicadoEn, "La fecha es obligatoria");
        Objects.requireNonNull(correlationId, "El correlationId es obligatorio");
    }

    public static KudoPublicado de(UUID eventoId, Kudo kudo, String nombreAutor, Destinatario destinatario,
            String nombreCategoria, UUID correlationId) {
        return new KudoPublicado(eventoId, kudo.id(), nombreAutor, destinatario, nombreCategoria, kudo.fecha(),
                correlationId);
    }
}
