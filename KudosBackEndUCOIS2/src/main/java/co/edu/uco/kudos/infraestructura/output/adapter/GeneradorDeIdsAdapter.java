package co.edu.uco.kudos.infraestructura.output.adapter;

import co.edu.uco.kudos.servicios.output.ports.repositorios.GeneradorDeIds;
import co.edu.uco.kudos.servicios.output.ports.serviciosexternos.Reloj;
import co.edu.uco.kudos.transversal.UuidV7;
import java.util.UUID;
import org.springframework.stereotype.Component;

/** Genera ids UUID v7 con la hora del {@link Reloj}, así quedan ordenados en el tiempo. */
@Component
public class GeneradorDeIdsAdapter implements GeneradorDeIds {

    private final Reloj reloj;

    public GeneradorDeIdsAdapter(Reloj reloj) {
        this.reloj = reloj;
    }

    @Override
    public UUID nuevo() {
        return UuidV7.generar(reloj.ahora());
    }
}
