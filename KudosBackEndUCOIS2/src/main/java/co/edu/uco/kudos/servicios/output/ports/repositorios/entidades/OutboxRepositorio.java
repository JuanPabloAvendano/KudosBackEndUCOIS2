package co.edu.uco.kudos.servicios.output.ports.repositorios.entidades;

import co.edu.uco.kudos.servicios.output.dto.EventoKudoPublicadoDTO;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

/** Outbox transaccional (ADR-010): el evento se guarda en la misma transacción que el Kudo. */
public interface OutboxRepositorio {

    void agregar(EventoKudoPublicadoDTO evento);

    /** Toma y bloquea hasta {@code lote} eventos sin enviar ({@code FOR UPDATE SKIP LOCKED}); exige transacción. */
    List<EventoKudoPublicadoDTO> tomarPendientes(int lote);

    void marcarEnviados(List<UUID> eventoIds, Instant enviadoEn);
}
