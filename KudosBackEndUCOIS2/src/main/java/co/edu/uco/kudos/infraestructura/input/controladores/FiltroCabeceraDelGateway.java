package co.edu.uco.kudos.infraestructura.input.controladores;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Rechaza (403) toda petición que no traiga la cabecera secreta de API Management: así nadie llama al servicio
 * saltándose el WAF y el gateway (ADR-013). La salud queda abierta para las sondas de Container Apps.
 */
public class FiltroCabeceraDelGateway extends OncePerRequestFilter {

    private static final String PROBLEMA =
            "{\"status\":403,\"title\":\"Acceso denegado\",\"detail\":\"La petición no llegó por el API Gateway\"}";

    private final String cabecera;
    private final byte[] secreto;

    public FiltroCabeceraDelGateway(PropiedadesDeEntrada.Gateway gateway) {
        this.cabecera = gateway.cabecera();
        this.secreto = gateway.secreto().getBytes(StandardCharsets.UTF_8);
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return request.getRequestURI().startsWith("/actuator/health");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain cadena)
            throws ServletException, IOException {
        String recibido = request.getHeader(cabecera);
        if (recibido == null || !MessageDigest.isEqual(secreto, recibido.getBytes(StandardCharsets.UTF_8))) {
            // Se responde aquí mismo: sendError haría un despacho interno a /error que Spring Security corta con 401
            response.setStatus(HttpStatus.FORBIDDEN.value());
            response.setContentType(MediaType.APPLICATION_PROBLEM_JSON_VALUE);
            response.setCharacterEncoding(StandardCharsets.UTF_8.name());
            response.getWriter().write(PROBLEMA);
            return;
        }
        cadena.doFilter(request, response);
    }
}
