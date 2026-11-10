package co.edu.uco.kudos.servicios.casosuso.impl;

import co.edu.uco.kudos.dominio.categoria.Categoria;
import co.edu.uco.kudos.dominio.kudo.Adjunto;
import co.edu.uco.kudos.dominio.kudo.AdjuntoId;
import co.edu.uco.kudos.dominio.kudo.Destinatario;
import co.edu.uco.kudos.dominio.kudo.Kudo;
import co.edu.uco.kudos.dominio.kudo.KudoId;
import co.edu.uco.kudos.dominio.kudo.KudoPublicado;
import co.edu.uco.kudos.dominio.kudo.TipoDeArchivo;
import co.edu.uco.kudos.dominio.reglas.CodigoRegla;
import co.edu.uco.kudos.dominio.reglas.ReglaIncumplida;
import co.edu.uco.kudos.dominio.usuario.Usuario;
import co.edu.uco.kudos.servicios.casosuso.ArchivoNuevo;
import co.edu.uco.kudos.servicios.casosuso.SolicitudDePublicacion;
import co.edu.uco.kudos.servicios.output.adapter.EventoKudoPublicadoOutputAdapter;
import co.edu.uco.kudos.servicios.output.ports.repositorios.entidades.CategoriaRepositorio;
import co.edu.uco.kudos.servicios.output.ports.repositorios.GeneradorDeIds;
import co.edu.uco.kudos.servicios.output.ports.repositorios.entidades.UsuarioRepositorio;
import co.edu.uco.kudos.servicios.output.ports.serviciosexternos.CatalogoDeParametros;
import co.edu.uco.kudos.servicios.output.ports.serviciosexternos.Reloj;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.util.List;

/** Valida destinatario, categoría y adjunto, y arma el Kudo con su evento (pasos 7-13 de la secuencia). */
public final class PreparacionDelKudo {

    private final UsuarioRepositorio usuarios;
    private final CategoriaRepositorio categorias;
    private final GeneradorDeIds ids;
    private final Reloj reloj;
    private final CatalogoDeParametros parametros;
    private final EventoKudoPublicadoOutputAdapter eventoAdapter;

    public PreparacionDelKudo(UsuarioRepositorio usuarios, CategoriaRepositorio categorias, GeneradorDeIds ids,
            Reloj reloj, CatalogoDeParametros parametros, EventoKudoPublicadoOutputAdapter eventoAdapter) {
        this.usuarios = usuarios;
        this.categorias = categorias;
        this.ids = ids;
        this.reloj = reloj;
        this.parametros = parametros;
        this.eventoAdapter = eventoAdapter;
    }

    public KudoPreparado preparar(SolicitudDePublicacion solicitud) {
        Usuario autor = usuarios.porId(solicitud.autor())
                .orElseThrow(() -> new IllegalStateException("El autor no está registrado"));
        Usuario destinatario = usuarios.porId(solicitud.destinatario())
                .orElseThrow(() -> new ReglaIncumplida(CodigoRegla.DESTINATARIO_NO_EXISTE,
                        "El destinatario no está registrado en UCO Kudos"));
        Categoria categoria = categorias.porId(solicitud.categoria())
                .orElseThrow(() -> new ReglaIncumplida(CodigoRegla.CATEGORIA_NO_EXISTE, "La categoría no existe"));
        ReglaIncumplida.exigir(categoria.estaActiva(), CodigoRegla.CATEGORIA_INACTIVA, "La categoría no está activa");

        KudoId kudoId = new KudoId(ids.nuevo());
        List<Adjunto> adjuntos = solicitud.archivoOpcional().map(archivo -> List.of(adjunto(kudoId, archivo)))
                .orElse(List.of());
        Kudo kudo = Kudo.publicar(kudoId, autor.id(), destinatario.id(), categoria.id(), solicitud.contenido(),
                reloj.ahora(), adjuntos);
        KudoPublicado evento = KudoPublicado.de(ids.nuevo(), kudo, autor.nombre(), Destinatario.de(destinatario),
                categoria.nombre(), solicitud.correlationId());
        return new KudoPreparado(kudo, eventoAdapter.aDto(evento), solicitud.archivo());
    }

    private Adjunto adjunto(KudoId kudoId, ArchivoNuevo archivo) {
        return Adjunto.nuevo(kudoId, new AdjuntoId(ids.nuevo()), cabecera(archivo), archivo.tamano(),
                parametros.tamanoMaximoAdjunto(), parametros.tiposDeAdjuntoPermitidos());
    }

    private static byte[] cabecera(ArchivoNuevo archivo) {
        try (InputStream flujo = archivo.contenido().get()) {
            return flujo.readNBytes(TipoDeArchivo.BYTES_DE_CABECERA);
        } catch (IOException e) {
            throw new UncheckedIOException("No se pudo leer el adjunto", e);
        }
    }
}
