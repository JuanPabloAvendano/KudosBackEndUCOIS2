package co.edu.uco.kudos.servicios.input.dto.response;

import java.util.UUID;

/** Adjunto del feed con su enlace temporal de solo lectura. */
public record AdjuntoResponse(UUID id, String tipo, long tamano, String enlace) {
}
