package co.edu.uco.kudos.servicios.casosuso.impl;

import co.edu.uco.kudos.servicios.casosuso.PublicarEventosPendientesCasoUso;
import co.edu.uco.kudos.servicios.output.dto.EventoKudoPublicadoDTO;
import co.edu.uco.kudos.servicios.output.ports.repositorios.entidades.OutboxRepositorio;
import co.edu.uco.kudos.servicios.output.ports.repositorios.UnidadDeTrabajo;
import co.edu.uco.kudos.servicios.output.ports.serviciosexternos.CatalogoDeParametros;
import co.edu.uco.kudos.servicios.output.ports.serviciosexternos.PublicadorDeEventos;
import co.edu.uco.kudos.servicios.output.ports.serviciosexternos.Reloj;
import java.util.List;

/**
 * Relevo del outbox (ADR-010): en una transacción por lote toma los eventos sin enviar con {@code FOR UPDATE SKIP
 * LOCKED} (varias réplicas no se pisan), los publica y los marca enviados. Si publicar falla, la transacción se
 * deshace y el lote sale en la siguiente vuelta: entrega al menos una vez (el gateway descarta duplicados, ADR-009).
 */
public final class PublicarEventosPendientesCasoUsoImpl implements PublicarEventosPendientesCasoUso {

    private final OutboxRepositorio outbox;
    private final PublicadorDeEventos publicador;
    private final UnidadDeTrabajo unidadDeTrabajo;
    private final CatalogoDeParametros parametros;
    private final Reloj reloj;
    private final EfectosPosteriores efectos;

    public PublicarEventosPendientesCasoUsoImpl(OutboxRepositorio outbox, PublicadorDeEventos publicador,
            UnidadDeTrabajo unidadDeTrabajo, CatalogoDeParametros parametros, Reloj reloj,
            EfectosPosteriores efectos) {
        this.outbox = outbox;
        this.publicador = publicador;
        this.unidadDeTrabajo = unidadDeTrabajo;
        this.parametros = parametros;
        this.reloj = reloj;
        this.efectos = efectos;
    }

    @Override
    public int ejecutar() {
        List<EventoKudoPublicadoDTO> publicados = unidadDeTrabajo.ejecutar(() -> {
            List<EventoKudoPublicadoDTO> pendientes = outbox.tomarPendientes(parametros.loteDelRelevo());
            if (!pendientes.isEmpty()) {
                publicador.publicar(pendientes);
                outbox.marcarEnviados(pendientes.stream().map(EventoKudoPublicadoDTO::eventoId).toList(),
                        reloj.ahora());
            }
            return pendientes;
        });
        publicados.forEach(efectos::eventoPublicado);
        return publicados.size();
    }
}
