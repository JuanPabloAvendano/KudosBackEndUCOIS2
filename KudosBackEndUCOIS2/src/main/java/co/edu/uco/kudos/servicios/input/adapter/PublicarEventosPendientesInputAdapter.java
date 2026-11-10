package co.edu.uco.kudos.servicios.input.adapter;

import co.edu.uco.kudos.servicios.casosuso.PublicarEventosPendientesCasoUso;
import co.edu.uco.kudos.servicios.input.ports.PublicarEventosPendientesInputPort;

/** Puerta de entrada del relevo del outbox hacia su caso de uso. */
public final class PublicarEventosPendientesInputAdapter implements PublicarEventosPendientesInputPort {

    private final PublicarEventosPendientesCasoUso publicarPendientes;

    public PublicarEventosPendientesInputAdapter(PublicarEventosPendientesCasoUso publicarPendientes) {
        this.publicarPendientes = publicarPendientes;
    }

    @Override
    public int publicarPendientes() {
        return publicarPendientes.ejecutar();
    }
}
