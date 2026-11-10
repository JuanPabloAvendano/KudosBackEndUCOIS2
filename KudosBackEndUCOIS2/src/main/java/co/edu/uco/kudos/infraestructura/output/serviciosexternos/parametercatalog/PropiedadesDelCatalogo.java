package co.edu.uco.kudos.infraestructura.output.serviciosexternos.parametercatalog;

import java.time.Duration;
import java.util.Set;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.util.unit.DataSize;

/**
 * Parámetros del negocio (ADR-003). Llegan al entorno desde App Configuration o como variables de entorno que Juan
 * configura en el portal; no hay valores por defecto en el código.
 */
@ConfigurationProperties("kudos.parametros")
public record PropiedadesDelCatalogo(DataSize tamanoMaximoAdjunto, Set<String> tiposDeAdjuntoPermitidos,
        Duration vigenciaEnlaceAdjunto, Duration ttlDelFeed, int tamanoDePagina, int tamanoMaximoDePagina,
        int loteDelRelevo) {
}
