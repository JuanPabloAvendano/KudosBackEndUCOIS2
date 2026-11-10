package co.edu.uco.kudos.infraestructura.output.serviciosexternos.parametercatalog;

import co.edu.uco.kudos.dominio.kudo.TipoDeArchivo;
import co.edu.uco.kudos.servicios.output.ports.serviciosexternos.CatalogoDeParametros;
import java.time.Duration;
import java.util.EnumSet;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;

/** Parameter Catalog leído del entorno de Spring (ahí lo deja App Configuration). */
@Component
public class CatalogoDeParametrosAdapter implements CatalogoDeParametros {

    private final PropiedadesDelCatalogo propiedades;
    private final Set<TipoDeArchivo> tiposPermitidos;

    public CatalogoDeParametrosAdapter(PropiedadesDelCatalogo propiedades) {
        this.propiedades = propiedades;
        this.tiposPermitidos = propiedades.tiposDeAdjuntoPermitidos().stream()
                .map(nombre -> TipoDeArchivo.valueOf(nombre.trim().toUpperCase()))
                .collect(Collectors.toCollection(() -> EnumSet.noneOf(TipoDeArchivo.class)));
    }

    @Override
    public long tamanoMaximoAdjunto() {
        return propiedades.tamanoMaximoAdjunto().toBytes();
    }

    @Override
    public Set<TipoDeArchivo> tiposDeAdjuntoPermitidos() {
        return Set.copyOf(tiposPermitidos);
    }

    @Override
    public Duration vigenciaEnlaceAdjunto() {
        return propiedades.vigenciaEnlaceAdjunto();
    }

    @Override
    public Duration ttlDelFeed() {
        return propiedades.ttlDelFeed();
    }

    @Override
    public int tamanoDePagina() {
        return propiedades.tamanoDePagina();
    }

    @Override
    public int tamanoMaximoDePagina() {
        return propiedades.tamanoMaximoDePagina();
    }

    @Override
    public int loteDelRelevo() {
        return propiedades.loteDelRelevo();
    }
}
