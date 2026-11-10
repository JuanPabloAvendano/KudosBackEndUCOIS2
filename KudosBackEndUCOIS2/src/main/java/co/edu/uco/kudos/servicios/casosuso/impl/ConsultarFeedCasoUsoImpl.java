package co.edu.uco.kudos.servicios.casosuso.impl;

import co.edu.uco.kudos.dominio.kudo.Adjunto;
import co.edu.uco.kudos.dominio.kudo.AdjuntoId;
import co.edu.uco.kudos.dominio.kudo.CursorDelFeed;
import co.edu.uco.kudos.dominio.kudo.Kudo;
import co.edu.uco.kudos.servicios.casosuso.ConsultarFeedCasoUso;
import co.edu.uco.kudos.servicios.casosuso.KudoConEnlaces;
import co.edu.uco.kudos.servicios.output.ports.repositorios.AlmacenAdjuntos;
import co.edu.uco.kudos.servicios.output.ports.repositorios.CacheDelFeed;
import co.edu.uco.kudos.servicios.output.ports.repositorios.entidades.KudoRepositorio;
import co.edu.uco.kudos.servicios.output.ports.serviciosexternos.CatalogoDeParametros;
import co.edu.uco.kudos.transversal.Pagina;
import java.lang.System.Logger.Level;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Feed paginado por keyset. La primera página usa Cache-Aside (ADR-011, ADR-017): si la caché falla, se lee de la
 * base de datos. Se pide un Kudo de más para saber si hay página siguiente.
 */
public final class ConsultarFeedCasoUsoImpl implements ConsultarFeedCasoUso {

    private static final System.Logger LOG = System.getLogger(ConsultarFeedCasoUsoImpl.class.getName());

    private final KudoRepositorio kudos;
    private final CacheDelFeed cache;
    private final AlmacenAdjuntos almacen;
    private final CatalogoDeParametros parametros;

    public ConsultarFeedCasoUsoImpl(KudoRepositorio kudos, CacheDelFeed cache, AlmacenAdjuntos almacen,
            CatalogoDeParametros parametros) {
        this.kudos = kudos;
        this.cache = cache;
        this.almacen = almacen;
        this.parametros = parametros;
    }

    @Override
    public Pagina<KudoConEnlaces> ejecutar(CursorDelFeed cursor, Integer tamano) {
        int limite = limitar(tamano);
        List<Kudo> encontrados = cursor == null ? primeraPagina(limite) : kudos.pagina(cursor, limite + 1);
        boolean hayMas = encontrados.size() > limite;
        List<Kudo> pagina = hayMas ? encontrados.subList(0, limite) : encontrados;
        String siguiente = hayMas ? CursorDelFeed.despuesDe(pagina.getLast()).codificar() : null;
        return new Pagina<>(pagina.stream().map(this::conEnlaces).toList(), siguiente);
    }

    private int limitar(Integer tamano) {
        int pedido = tamano == null ? parametros.tamanoDePagina() : tamano;
        return Math.clamp(pedido, 1, parametros.tamanoMaximoDePagina());
    }

    private List<Kudo> primeraPagina(int limite) {
        Optional<List<Kudo>> enCache = leerCache(limite);
        if (enCache.isPresent()) {
            return enCache.get();
        }
        List<Kudo> desdeBase = kudos.pagina(null, limite + 1);
        guardarCache(limite, desdeBase);
        return desdeBase;
    }

    private Optional<List<Kudo>> leerCache(int limite) {
        try {
            return cache.leer(limite);
        } catch (RuntimeException e) {
            LOG.log(Level.WARNING, "Caché del feed no disponible; se lee de la base de datos", e);
            return Optional.empty();
        }
    }

    private void guardarCache(int limite, List<Kudo> pagina) {
        try {
            cache.guardar(limite, pagina, parametros.ttlDelFeed());
        } catch (RuntimeException e) {
            LOG.log(Level.WARNING, "No se pudo guardar el feed en la caché", e);
        }
    }

    private KudoConEnlaces conEnlaces(Kudo kudo) {
        Map<AdjuntoId, String> enlaces = kudo.adjuntos().stream().collect(Collectors.toMap(Adjunto::id,
                adjunto -> almacen.enlaceTemporal(adjunto.claveArchivo(), parametros.vigenciaEnlaceAdjunto())));
        return new KudoConEnlaces(kudo, enlaces);
    }
}
