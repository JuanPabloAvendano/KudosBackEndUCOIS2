package co.edu.uco.kudos.infraestructura.input.programados;

import co.edu.uco.kudos.servicios.input.ports.PublicarEventosPendientesInputPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBooleanProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Dispara el relevo del outbox cada {@code kudos.relevo.intervalo} (ADR-010). Es una entrada al sistema, como un
 * controlador, pero la llama el reloj en vez de una petición HTTP.
 */
@Component
@ConditionalOnBooleanProperty("kudos.relevo.habilitado")
public class RelevoOutbox {

    private static final Logger LOG = LoggerFactory.getLogger(RelevoOutbox.class);

    private final PublicarEventosPendientesInputPort publicarPendientes;

    public RelevoOutbox(PublicarEventosPendientesInputPort publicarPendientes) {
        this.publicarPendientes = publicarPendientes;
    }

    @Scheduled(fixedDelayString = "${kudos.relevo.intervalo}")
    public void relevar() {
        try {
            int publicados = publicarPendientes.publicarPendientes();
            if (publicados > 0) {
                LOG.info("Relevo del outbox: {} eventos publicados", publicados);
            }
        } catch (RuntimeException e) {
            LOG.warn("El relevo del outbox falló; se reintenta en la siguiente vuelta", e);
        }
    }
}
