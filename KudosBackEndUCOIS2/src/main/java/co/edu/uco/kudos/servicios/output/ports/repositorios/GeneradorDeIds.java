package co.edu.uco.kudos.servicios.output.ports.repositorios;

import java.util.UUID;

/** Genera ids nuevos (UUID v7). */
public interface GeneradorDeIds {

    UUID nuevo();
}
