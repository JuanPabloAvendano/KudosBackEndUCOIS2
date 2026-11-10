package co.edu.uco.kudos.servicios.output.ports.serviciosexternos;

import co.edu.uco.kudos.servicios.output.dto.EventoKudoPublicadoDTO;
import java.util.List;

/** Message Broker (Event Grid, ADR-007). Si falla, lanza y el relevo reintenta en la siguiente vuelta. */
public interface PublicadorDeEventos {

    void publicar(List<EventoKudoPublicadoDTO> eventos);
}
