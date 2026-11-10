package co.edu.uco.kudos.servicios.output.ports.serviciosexternos;

import java.time.Instant;

/** Hora actual en UTC, con precisión de milisegundos (la del cursor del feed). */
public interface Reloj {

    Instant ahora();
}
