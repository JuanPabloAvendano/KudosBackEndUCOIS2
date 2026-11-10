package co.edu.uco.kudos.servicios.input.dto.response;

import java.time.Instant;
import java.util.UUID;

/** Respuesta del 201: el Kudo quedó guardado; las notificaciones salen después. */
public record PublicarKudoResponse(UUID id, Instant fecha) {
}
