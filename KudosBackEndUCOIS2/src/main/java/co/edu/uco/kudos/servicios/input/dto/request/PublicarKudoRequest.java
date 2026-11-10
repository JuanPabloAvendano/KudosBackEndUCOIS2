package co.edu.uco.kudos.servicios.input.dto.request;

import java.util.Optional;
import java.util.UUID;

/** Lo que manda el cliente para publicar un Kudo. */
public record PublicarKudoRequest(UUID destinatarioId, Integer categoriaId, String contenido, AdjuntoRequest adjunto,
        UUID correlationId) {

    public Optional<AdjuntoRequest> adjuntoOpcional() {
        return Optional.ofNullable(adjunto);
    }
}
