package co.edu.uco.kudos.servicios.casosuso.impl;

import co.edu.uco.kudos.dominio.kudo.Adjunto;
import co.edu.uco.kudos.dominio.kudo.Kudo;
import co.edu.uco.kudos.servicios.casosuso.ArchivoNuevo;
import co.edu.uco.kudos.servicios.casosuso.PublicarKudoCasoUso;
import co.edu.uco.kudos.servicios.casosuso.SolicitudDePublicacion;
import co.edu.uco.kudos.servicios.output.ports.repositorios.AlmacenAdjuntos;
import co.edu.uco.kudos.servicios.output.ports.repositorios.entidades.KudoRepositorio;
import co.edu.uco.kudos.servicios.output.ports.repositorios.entidades.OutboxRepositorio;
import co.edu.uco.kudos.servicios.output.ports.repositorios.UnidadDeTrabajo;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.lang.System.Logger.Level;

/**
 * Publicar Kudo (secuencia del diseño detallado §3): prepara y valida, sube el adjunto antes de la transacción, guarda
 * Kudo + evento en una sola transacción (outbox, ADR-010) y, después del commit, audita y limpia la caché.
 */
public final class PublicarKudoCasoUsoImpl implements PublicarKudoCasoUso {

    private static final System.Logger LOG = System.getLogger(PublicarKudoCasoUsoImpl.class.getName());

    private final PreparacionDelKudo preparacion;
    private final AlmacenAdjuntos almacen;
    private final KudoRepositorio kudos;
    private final OutboxRepositorio outbox;
    private final UnidadDeTrabajo unidadDeTrabajo;
    private final EfectosPosteriores efectos;

    public PublicarKudoCasoUsoImpl(PreparacionDelKudo preparacion, AlmacenAdjuntos almacen, KudoRepositorio kudos,
            OutboxRepositorio outbox, UnidadDeTrabajo unidadDeTrabajo, EfectosPosteriores efectos) {
        this.preparacion = preparacion;
        this.almacen = almacen;
        this.kudos = kudos;
        this.outbox = outbox;
        this.unidadDeTrabajo = unidadDeTrabajo;
        this.efectos = efectos;
    }

    @Override
    public Kudo ejecutar(SolicitudDePublicacion solicitud) {
        KudoPreparado preparado = preparacion.preparar(solicitud);
        Kudo kudo = preparado.kudo();
        preparado.archivoOpcional().ifPresent(archivo -> subir(kudo.adjuntos().getFirst(), archivo));
        try {
            unidadDeTrabajo.ejecutar(() -> {
                kudos.guardar(kudo);
                outbox.agregar(preparado.evento());
            });
        } catch (RuntimeException e) {
            kudo.adjuntos().forEach(this::eliminarHuerfano);
            throw e;
        }
        efectos.kudoGuardado(kudo, solicitud.correlationId());
        return kudo;
    }

    private void subir(Adjunto adjunto, ArchivoNuevo archivo) {
        try (InputStream flujo = archivo.contenido().get()) {
            almacen.guardar(adjunto.claveArchivo(), adjunto.tipo().mime(), adjunto.tamano(), flujo);
        } catch (IOException e) {
            throw new UncheckedIOException("No se pudo leer el adjunto", e);
        }
    }

    private void eliminarHuerfano(Adjunto adjunto) {
        try {
            almacen.eliminar(adjunto.claveArchivo());
        } catch (RuntimeException e) {
            LOG.log(Level.WARNING, "Quedó un adjunto huérfano: " + adjunto.claveArchivo(), e);
        }
    }
}
