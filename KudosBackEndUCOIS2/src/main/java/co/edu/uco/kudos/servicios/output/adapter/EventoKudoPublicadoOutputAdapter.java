package co.edu.uco.kudos.servicios.output.adapter;

import co.edu.uco.kudos.dominio.kudo.KudoPublicado;
import co.edu.uco.kudos.servicios.output.dto.EventoKudoPublicadoDTO;

/** Traductor del evento de dominio {@link KudoPublicado} al contrato con el Notification Gateway. */
public interface EventoKudoPublicadoOutputAdapter {

    EventoKudoPublicadoDTO aDto(KudoPublicado evento);
}
