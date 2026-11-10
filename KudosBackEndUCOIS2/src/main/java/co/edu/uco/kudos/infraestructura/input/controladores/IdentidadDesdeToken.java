package co.edu.uco.kudos.infraestructura.input.controladores;

import co.edu.uco.kudos.servicios.input.dto.request.IdentidadRequest;
import java.util.List;
import java.util.UUID;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;

/** Lee del JWT quién hace la petición. Sin un rol en el token responde 403 (un rol activo por persona, QA-4). */
@Component
public class IdentidadDesdeToken {

    private final PropiedadesDeEntrada.Claims claims;

    public IdentidadDesdeToken(PropiedadesDeEntrada propiedades) {
        this.claims = propiedades.claims();
    }

    public IdentidadRequest leer(Jwt jwt) {
        List<String> roles = jwt.getClaimAsStringList(claims.roles());
        if (roles == null || roles.isEmpty()) {
            throw new AccessDeniedException("El token no trae un rol institucional");
        }
        return new IdentidadRequest(UUID.fromString(jwt.getClaimAsString(claims.id())),
                jwt.getClaimAsString(claims.nombre()), correo(jwt), jwt.getClaimAsString(claims.telefono()),
                roles.getFirst());
    }

    /** Entra puede mandar el correo como texto o como lista ({@code emails}). */
    private String correo(Jwt jwt) {
        Object valor = jwt.getClaim(claims.correo());
        if (valor instanceof List<?> lista && !lista.isEmpty()) {
            return String.valueOf(lista.getFirst());
        }
        return valor == null ? null : valor.toString();
    }
}
