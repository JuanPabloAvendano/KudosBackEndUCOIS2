package co.edu.uco.kudos.infraestructura.output.adapter;

import co.edu.uco.kudos.servicios.output.ports.serviciosexternos.Reloj;
import java.time.Clock;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import org.springframework.stereotype.Component;

/** Hora UTC truncada a milisegundos: la misma precisión del cursor del feed y de {@code timestamptz} al compararlas. */
@Component
public class RelojUtc implements Reloj {

    private final Clock reloj = Clock.systemUTC();

    @Override
    public Instant ahora() {
        return reloj.instant().truncatedTo(ChronoUnit.MILLIS);
    }
}
