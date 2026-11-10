package co.edu.uco.kudos.servicios.output.dto;

import java.time.Instant;
import java.util.UUID;

/**
 * Contenido ({@code data}) del evento {@code uco.kudos.KudoPublicado.v1} en CloudEvents 1.0 (diseño detallado §4).
 * Se guarda tal cual en el outbox y se publica tal cual en el Message Broker.
 */
public record EventoKudoPublicadoDTO(UUID eventoId, UUID kudoId, String autor, DestinatarioDTO destinatario,
        String categoria, Instant publicadoEn, UUID correlationId) {

    public static final String TIPO = "uco.kudos.KudoPublicado.v1";
}
