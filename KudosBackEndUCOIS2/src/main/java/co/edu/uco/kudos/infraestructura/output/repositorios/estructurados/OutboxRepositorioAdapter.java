package co.edu.uco.kudos.infraestructura.output.repositorios.estructurados;

import co.edu.uco.kudos.infraestructura.output.repositorios.estructurados.jpa.OutboxJPARepositorio;
import co.edu.uco.kudos.infraestructura.output.repositorios.estructurados.jpa.entidades.OutboxEventoEntidadJPA;
import co.edu.uco.kudos.servicios.output.dto.EventoKudoPublicadoDTO;
import co.edu.uco.kudos.servicios.output.ports.repositorios.entidades.OutboxRepositorio;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Component;
import tools.jackson.databind.json.JsonMapper;

/** Outbox en PostgreSQL; el evento se guarda como JSON en {@code payload} (jsonb). */
@Component
public class OutboxRepositorioAdapter implements OutboxRepositorio {

    private final OutboxJPARepositorio jpa;
    private final JsonMapper json;

    public OutboxRepositorioAdapter(OutboxJPARepositorio jpa, JsonMapper json) {
        this.jpa = jpa;
        this.json = json;
    }

    @Override
    public void agregar(EventoKudoPublicadoDTO evento) {
        jpa.save(new OutboxEventoEntidadJPA(evento.eventoId(), EventoKudoPublicadoDTO.TIPO,
                json.writeValueAsString(evento), evento.publicadoEn()));
    }

    @Override
    public List<EventoKudoPublicadoDTO> tomarPendientes(int lote) {
        return jpa.tomarPendientes(lote).stream()
                .map(fila -> json.readValue(fila.getPayload(), EventoKudoPublicadoDTO.class))
                .toList();
    }

    @Override
    public void marcarEnviados(List<UUID> eventoIds, Instant enviadoEn) {
        jpa.marcarEnviados(eventoIds, enviadoEn);
    }
}
