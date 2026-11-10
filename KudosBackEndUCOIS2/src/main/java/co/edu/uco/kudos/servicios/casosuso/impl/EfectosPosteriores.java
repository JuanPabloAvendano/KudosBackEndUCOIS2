package co.edu.uco.kudos.servicios.casosuso.impl;

import co.edu.uco.kudos.dominio.kudo.Kudo;
import co.edu.uco.kudos.servicios.output.dto.EventoKudoPublicadoDTO;
import co.edu.uco.kudos.servicios.output.dto.RegistroAuditoriaDTO;
import co.edu.uco.kudos.servicios.output.ports.repositorios.Auditoria;
import co.edu.uco.kudos.servicios.output.ports.repositorios.CacheDelFeed;
import co.edu.uco.kudos.servicios.output.ports.repositorios.GeneradorDeIds;
import co.edu.uco.kudos.servicios.output.ports.serviciosexternos.Reloj;
import java.lang.System.Logger.Level;
import java.util.UUID;

/**
 * Lo que pasa después del commit (best-effort): auditar y limpiar la caché del feed. Si falla, se registra en el log
 * y la respuesta no cambia; la caché tiene TTL de respaldo (ADR-011).
 */
public final class EfectosPosteriores {

    private static final System.Logger LOG = System.getLogger(EfectosPosteriores.class.getName());

    private final Auditoria auditoria;
    private final CacheDelFeed cache;
    private final GeneradorDeIds ids;
    private final Reloj reloj;

    public EfectosPosteriores(Auditoria auditoria, CacheDelFeed cache, GeneradorDeIds ids, Reloj reloj) {
        this.auditoria = auditoria;
        this.cache = cache;
        this.ids = ids;
        this.reloj = reloj;
    }

    public void kudoGuardado(Kudo kudo, UUID correlationId) {
        auditar(RegistroAuditoriaDTO.KUDO_GUARDADO, kudo.autor().valor(), "kudo/" + kudo.id().valor(), correlationId);
        try {
            cache.invalidar();
        } catch (RuntimeException e) {
            LOG.log(Level.WARNING, "No se pudo invalidar la caché del feed; vence por TTL", e);
        }
    }

    public void eventoPublicado(EventoKudoPublicadoDTO evento) {
        auditar(RegistroAuditoriaDTO.EVENTO_PUBLICADO, null, "evento/" + evento.eventoId(), evento.correlationId());
    }

    private void auditar(String accion, UUID actor, String recurso, UUID correlationId) {
        try {
            auditoria.registrar(new RegistroAuditoriaDTO(ids.nuevo(), accion, actor, recurso, correlationId,
                    reloj.ahora()));
        } catch (RuntimeException e) {
            LOG.log(Level.WARNING, "No se pudo auditar " + accion + " de " + recurso, e);
        }
    }
}
