package co.edu.uco.kudos.infraestructura.output.adapter;

import co.edu.uco.kudos.servicios.output.ports.repositorios.UnidadDeTrabajo;
import java.util.function.Supplier;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionTemplate;

/** Transacción programática con {@link TransactionTemplate}: deshace todo ante cualquier {@link RuntimeException}. */
@Component
public class UnidadDeTrabajoAdapter implements UnidadDeTrabajo {

    private final TransactionTemplate plantilla;

    public UnidadDeTrabajoAdapter(TransactionTemplate plantilla) {
        this.plantilla = plantilla;
    }

    @Override
    public <T> T ejecutar(Supplier<T> trabajo) {
        return plantilla.execute(estado -> trabajo.get());
    }
}
