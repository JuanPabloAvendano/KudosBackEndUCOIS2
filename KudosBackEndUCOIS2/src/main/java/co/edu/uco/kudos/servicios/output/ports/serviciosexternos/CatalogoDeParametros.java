package co.edu.uco.kudos.servicios.output.ports.serviciosexternos;

import co.edu.uco.kudos.dominio.kudo.TipoDeArchivo;
import java.time.Duration;
import java.util.Set;

/** Parameter Catalog (ADR-003): ningún límite de negocio vive en el código. */
public interface CatalogoDeParametros {

    long tamanoMaximoAdjunto();

    Set<TipoDeArchivo> tiposDeAdjuntoPermitidos();

    Duration vigenciaEnlaceAdjunto();

    Duration ttlDelFeed();

    int tamanoDePagina();

    int tamanoMaximoDePagina();

    int loteDelRelevo();
}
