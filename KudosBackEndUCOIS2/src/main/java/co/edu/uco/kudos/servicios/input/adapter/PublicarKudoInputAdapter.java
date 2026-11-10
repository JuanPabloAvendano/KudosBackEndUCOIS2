package co.edu.uco.kudos.servicios.input.adapter;

import co.edu.uco.kudos.dominio.categoria.CategoriaId;
import co.edu.uco.kudos.dominio.kudo.Kudo;
import co.edu.uco.kudos.dominio.usuario.Usuario;
import co.edu.uco.kudos.dominio.usuario.UsuarioId;
import co.edu.uco.kudos.servicios.casosuso.ArchivoNuevo;
import co.edu.uco.kudos.servicios.casosuso.PublicarKudoCasoUso;
import co.edu.uco.kudos.servicios.casosuso.RegistrarUsuarioDesdeIdentidadCasoUso;
import co.edu.uco.kudos.servicios.casosuso.SolicitudDePublicacion;
import co.edu.uco.kudos.servicios.input.dto.request.IdentidadRequest;
import co.edu.uco.kudos.servicios.input.dto.request.PublicarKudoRequest;
import co.edu.uco.kudos.servicios.input.dto.response.PublicarKudoResponse;
import co.edu.uco.kudos.servicios.input.ports.PublicarKudoInputPort;

/** Registra al autor desde su token y publica el Kudo (pasos 5-21 de la secuencia). */
public final class PublicarKudoInputAdapter implements PublicarKudoInputPort {

    private final RegistrarUsuarioDesdeIdentidadCasoUso registrarUsuario;
    private final PublicarKudoCasoUso publicarKudo;

    public PublicarKudoInputAdapter(RegistrarUsuarioDesdeIdentidadCasoUso registrarUsuario,
            PublicarKudoCasoUso publicarKudo) {
        this.registrarUsuario = registrarUsuario;
        this.publicarKudo = publicarKudo;
    }

    @Override
    public PublicarKudoResponse publicar(IdentidadRequest identidad, PublicarKudoRequest request) {
        if (request.destinatarioId() == null || request.categoriaId() == null) {
            throw new IllegalArgumentException("El destinatario y la categoría son obligatorios");
        }
        Usuario autor = registrarUsuario.ejecutar(IdentidadMapper.aUsuario(identidad));
        ArchivoNuevo archivo = request.adjuntoOpcional()
                .map(adjunto -> new ArchivoNuevo(adjunto.tamano(), adjunto.contenido()))
                .orElse(null);
        Kudo kudo = publicarKudo.ejecutar(new SolicitudDePublicacion(autor.id(),
                new UsuarioId(request.destinatarioId()), new CategoriaId(request.categoriaId()), request.contenido(),
                archivo, request.correlationId()));
        return new PublicarKudoResponse(kudo.id().valor(), kudo.fecha());
    }
}
