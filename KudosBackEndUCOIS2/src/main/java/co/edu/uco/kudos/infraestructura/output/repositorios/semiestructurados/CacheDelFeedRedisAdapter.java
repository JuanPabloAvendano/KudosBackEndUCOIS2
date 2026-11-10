package co.edu.uco.kudos.infraestructura.output.repositorios.semiestructurados;

import co.edu.uco.kudos.dominio.kudo.Kudo;
import co.edu.uco.kudos.servicios.output.ports.repositorios.CacheDelFeed;
import java.time.Duration;
import java.util.List;
import java.util.Optional;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.json.JsonMapper;

/**
 * Primera página del feed en Azure Managed Redis (ADR-017). Un solo hash guarda la página de cada tamaño: invalidar es
 * borrar una clave (ADR-011) y el TTL de respaldo vence todo el hash.
 */
@Component
public class CacheDelFeedRedisAdapter implements CacheDelFeed {

    static final String CLAVE = "kudos:feed:primera";
    private static final TypeReference<List<KudoEnCache>> LISTA = new TypeReference<>() {
    };

    private final StringRedisTemplate redis;
    private final JsonMapper json;

    public CacheDelFeedRedisAdapter(StringRedisTemplate redis, JsonMapper json) {
        this.redis = redis;
        this.json = json;
    }

    @Override
    public Optional<List<Kudo>> leer(int tamano) {
        Object valor = redis.opsForHash().get(CLAVE, String.valueOf(tamano));
        return Optional.ofNullable(valor)
                .map(texto -> json.readValue(texto.toString(), LISTA).stream().map(KudoEnCache::aDominio).toList());
    }

    @Override
    public void guardar(int tamano, List<Kudo> kudos, Duration ttl) {
        String valor = json.writeValueAsString(kudos.stream().map(KudoEnCache::desde).toList());
        redis.opsForHash().put(CLAVE, String.valueOf(tamano), valor);
        redis.expire(CLAVE, ttl);
    }

    @Override
    public void invalidar() {
        redis.delete(CLAVE);
    }
}
