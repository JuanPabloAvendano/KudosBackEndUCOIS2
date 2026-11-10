package co.edu.uco.kudos.servicios.output.ports.repositorios;

import co.edu.uco.kudos.dominio.kudo.Kudo;
import java.time.Duration;
import java.util.List;
import java.util.Optional;

/**
 * Caché de la primera página del feed (Cache-Aside, ADR-011 y ADR-017). No es fuente de verdad: si falla, el caso
 * de uso lee de la base de datos.
 */
public interface CacheDelFeed {

    Optional<List<Kudo>> leer(int tamano);

    void guardar(int tamano, List<Kudo> kudos, Duration ttl);

    /** Borra la primera página de todos los tamaños (tras publicar un Kudo). */
    void invalidar();
}
