package co.edu.uco.kudos.servicios.output.ports.repositorios;

import java.util.function.Supplier;

/**
 * Abre una transacción, ejecuta el trabajo y confirma; ante cualquier {@link RuntimeException} deshace todo. Así los
 * casos de uso no importan Spring (A-31 de ucotestapi).
 */
public interface UnidadDeTrabajo {

    <T> T ejecutar(Supplier<T> trabajo);

    default void ejecutar(Runnable trabajo) {
        ejecutar(() -> {
            trabajo.run();
            return null;
        });
    }
}
