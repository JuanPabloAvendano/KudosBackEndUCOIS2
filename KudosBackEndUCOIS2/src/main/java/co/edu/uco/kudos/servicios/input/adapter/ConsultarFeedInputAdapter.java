package co.edu.uco.kudos.servicios.input.adapter;

import co.edu.uco.kudos.dominio.kudo.CursorDelFeed;
import co.edu.uco.kudos.dominio.kudo.Kudo;
import co.edu.uco.kudos.servicios.casosuso.ConsultarFeedCasoUso;
import co.edu.uco.kudos.servicios.casosuso.KudoConEnlaces;
import co.edu.uco.kudos.servicios.casosuso.RegistrarUsuarioDesdeIdentidadCasoUso;
import co.edu.uco.kudos.servicios.input.dto.request.IdentidadRequest;
import co.edu.uco.kudos.servicios.input.dto.response.AdjuntoResponse;
import co.edu.uco.kudos.servicios.input.dto.response.KudoResponse;
import co.edu.uco.kudos.servicios.input.dto.response.PaginaResponse;
import co.edu.uco.kudos.servicios.input.ports.ConsultarFeedInputPort;
import co.edu.uco.kudos.transversal.Pagina;

/** Registra al lector desde su token y le entrega una página del feed. */
public final class ConsultarFeedInputAdapter implements ConsultarFeedInputPort {

    private final RegistrarUsuarioDesdeIdentidadCasoUso registrarUsuario;
    private final ConsultarFeedCasoUso consultarFeed;

    public ConsultarFeedInputAdapter(RegistrarUsuarioDesdeIdentidadCasoUso registrarUsuario,
            ConsultarFeedCasoUso consultarFeed) {
        this.registrarUsuario = registrarUsuario;
        this.consultarFeed = consultarFeed;
    }

    @Override
    public PaginaResponse<KudoResponse> consultar(IdentidadRequest identidad, String cursor, Integer tamano) {
        registrarUsuario.ejecutar(IdentidadMapper.aUsuario(identidad));
        CursorDelFeed posicion = cursor == null || cursor.isBlank() ? null : CursorDelFeed.decodificar(cursor);
        Pagina<KudoResponse> pagina = consultarFeed.ejecutar(posicion, tamano).mapear(ConsultarFeedInputAdapter::aResponse);
        return new PaginaResponse<>(pagina.elementos(), pagina.siguienteCursor());
    }

    private static KudoResponse aResponse(KudoConEnlaces conEnlaces) {
        Kudo kudo = conEnlaces.kudo();
        return new KudoResponse(kudo.id().valor(), kudo.autor().valor(), kudo.destinatario().valor(),
                kudo.categoria().valor(), kudo.contenido(), kudo.fecha(),
                kudo.adjuntos().stream()
                        .map(adjunto -> new AdjuntoResponse(adjunto.id().valor(), adjunto.tipo().mime(),
                                adjunto.tamano(), conEnlaces.enlaces().get(adjunto.id())))
                        .toList());
    }
}
