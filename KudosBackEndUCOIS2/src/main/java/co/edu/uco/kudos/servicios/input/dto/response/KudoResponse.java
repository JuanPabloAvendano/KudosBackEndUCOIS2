package co.edu.uco.kudos.servicios.input.dto.response;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/** Un Kudo del feed. */
public record KudoResponse(UUID id, UUID autorId, UUID destinatarioId, int categoriaId, String contenido,
        Instant fecha, List<AdjuntoResponse> adjuntos) {

    public KudoResponse {
        adjuntos = List.copyOf(adjuntos);
    }
}
