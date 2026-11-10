package co.edu.uco.kudos.infraestructura.input.controladores;

import co.edu.uco.kudos.servicios.input.dto.request.AdjuntoRequest;
import co.edu.uco.kudos.servicios.input.dto.request.PublicarKudoRequest;
import co.edu.uco.kudos.servicios.input.dto.response.KudoResponse;
import co.edu.uco.kudos.servicios.input.dto.response.PaginaResponse;
import co.edu.uco.kudos.servicios.input.dto.response.PublicarKudoResponse;
import co.edu.uco.kudos.servicios.input.ports.ConsultarFeedInputPort;
import co.edu.uco.kudos.servicios.input.ports.PublicarKudoInputPort;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/** API de Kudos: publicar (multipart, con adjunto opcional) y consultar el feed. */
@RestController
@RequestMapping("/api/kudos")
public class KudoControlador {

    private final PublicarKudoInputPort publicarKudo;
    private final ConsultarFeedInputPort consultarFeed;
    private final IdentidadDesdeToken identidad;

    public KudoControlador(PublicarKudoInputPort publicarKudo, ConsultarFeedInputPort consultarFeed,
            IdentidadDesdeToken identidad) {
        this.publicarKudo = publicarKudo;
        this.consultarFeed = consultarFeed;
        this.identidad = identidad;
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public PublicarKudoResponse publicar(@AuthenticationPrincipal Jwt jwt,
            @RequestAttribute(FiltroCorrelacion.ATRIBUTO) UUID correlationId,
            @RequestParam UUID destinatarioId, @RequestParam Integer categoriaId, @RequestParam String contenido,
            @RequestParam(required = false) MultipartFile adjunto) {
        AdjuntoRequest archivo = adjunto == null || adjunto.isEmpty() ? null
                : new AdjuntoRequest(adjunto.getContentType(), adjunto.getSize(), () -> abrir(adjunto));
        return publicarKudo.publicar(identidad.leer(jwt),
                new PublicarKudoRequest(destinatarioId, categoriaId, contenido, archivo, correlationId));
    }

    @GetMapping
    public PaginaResponse<KudoResponse> feed(@AuthenticationPrincipal Jwt jwt,
            @RequestParam(required = false) String cursor, @RequestParam(required = false) Integer tamano) {
        return consultarFeed.consultar(identidad.leer(jwt), cursor, tamano);
    }

    private static InputStream abrir(MultipartFile archivo) {
        try {
            return archivo.getInputStream();
        } catch (IOException e) {
            throw new UncheckedIOException("No se pudo leer el adjunto", e);
        }
    }
}
