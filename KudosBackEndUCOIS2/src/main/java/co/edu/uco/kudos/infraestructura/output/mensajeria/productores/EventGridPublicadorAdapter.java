package co.edu.uco.kudos.infraestructura.output.mensajeria.productores;

import co.edu.uco.kudos.servicios.output.dto.EventoKudoPublicadoDTO;
import co.edu.uco.kudos.servicios.output.ports.serviciosexternos.PublicadorDeEventos;
import com.azure.core.models.CloudEvent;
import com.azure.core.models.CloudEventDataFormat;
import com.azure.core.util.BinaryData;
import com.azure.messaging.eventgrid.EventGridPublisherClient;
import java.time.ZoneOffset;
import java.util.List;
import tools.jackson.databind.json.JsonMapper;

/**
 * Publica {@code KudoPublicado} en el tópico de Event Grid con esquema CloudEvents 1.0 (ADR-007). El id del CloudEvent
 * es el id del evento: con él el Notification Gateway descarta duplicados (ADR-009).
 */
public class EventGridPublicadorAdapter implements PublicadorDeEventos {

    private final EventGridPublisherClient<CloudEvent> cliente;
    private final String fuente;
    private final JsonMapper json;

    public EventGridPublicadorAdapter(EventGridPublisherClient<CloudEvent> cliente, String fuente, JsonMapper json) {
        this.cliente = cliente;
        this.fuente = fuente;
        this.json = json;
    }

    @Override
    public void publicar(List<EventoKudoPublicadoDTO> eventos) {
        cliente.sendEvents(eventos.stream().map(this::aCloudEvent).toList());
    }

    CloudEvent aCloudEvent(EventoKudoPublicadoDTO evento) {
        BinaryData datos = BinaryData.fromString(json.writeValueAsString(evento));
        return new CloudEvent(fuente, EventoKudoPublicadoDTO.TIPO, datos, CloudEventDataFormat.JSON,
                "application/json")
                .setId(evento.eventoId().toString())
                .setSubject("kudos/" + evento.kudoId())
                .setTime(evento.publicadoEn().atOffset(ZoneOffset.UTC));
    }
}
