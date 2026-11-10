package co.edu.uco.kudos.servicios.input.ports;

/** Relevo del outbox: publica en el Message Broker los eventos aún no enviados (ADR-010). */
public interface PublicarEventosPendientesInputPort {

    /** @return cuántos eventos se publicaron */
    int publicarPendientes();
}
