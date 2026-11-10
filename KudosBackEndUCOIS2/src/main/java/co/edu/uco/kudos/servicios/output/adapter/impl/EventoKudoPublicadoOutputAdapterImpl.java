package co.edu.uco.kudos.servicios.output.adapter.impl;

import co.edu.uco.kudos.dominio.kudo.Destinatario;
import co.edu.uco.kudos.dominio.kudo.KudoPublicado;
import co.edu.uco.kudos.servicios.output.adapter.EventoKudoPublicadoOutputAdapter;
import co.edu.uco.kudos.servicios.output.dto.DestinatarioDTO;
import co.edu.uco.kudos.servicios.output.dto.EventoKudoPublicadoDTO;

/** Copia los datos del evento de dominio, campo por campo, a tipos simples listos para JSON (CloudEvents). */
public final class EventoKudoPublicadoOutputAdapterImpl implements EventoKudoPublicadoOutputAdapter {

    @Override
    public EventoKudoPublicadoDTO aDto(KudoPublicado evento) {
        Destinatario destinatario = evento.destinatario();
        return new EventoKudoPublicadoDTO(evento.eventoId(), evento.kudoId().valor(), evento.autor(),
                new DestinatarioDTO(destinatario.id().valor(), destinatario.nombre(), destinatario.correo(),
                        destinatario.telefono()),
                evento.categoria(), evento.publicadoEn(), evento.correlationId());
    }
}
