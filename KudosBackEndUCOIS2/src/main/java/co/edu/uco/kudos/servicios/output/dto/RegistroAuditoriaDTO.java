package co.edu.uco.kudos.servicios.output.dto;

import java.time.Instant;
import java.util.UUID;

/** Una entrada del Registro de auditoría (ADR-012): quién hizo qué, sobre qué recurso y con qué correlationId. */
public record RegistroAuditoriaDTO(UUID id, String accion, UUID actorId, String recurso, UUID correlationId,
        Instant ocurridoEn) {

    public static final String KUDO_GUARDADO = "KUDO_GUARDADO";
    public static final String EVENTO_PUBLICADO = "EVENTO_PUBLICADO";
}
