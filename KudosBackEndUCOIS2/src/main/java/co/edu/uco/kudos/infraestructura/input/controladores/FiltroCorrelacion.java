package co.edu.uco.kudos.infraestructura.input.controladores;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.UUID;
import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Toma el {@code X-Correlation-Id} que llega (o crea uno) y lo deja en el log, en la respuesta y para el caso de uso:
 * el mismo id sigue al Kudo hasta la auditoría y el evento (QA-7).
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class FiltroCorrelacion extends OncePerRequestFilter {

    public static final String CABECERA = "X-Correlation-Id";
    public static final String ATRIBUTO = "correlationId";

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain cadena)
            throws ServletException, IOException {
        UUID correlationId = leer(request.getHeader(CABECERA));
        request.setAttribute(ATRIBUTO, correlationId);
        response.setHeader(CABECERA, correlationId.toString());
        MDC.put(ATRIBUTO, correlationId.toString());
        try {
            cadena.doFilter(request, response);
        } finally {
            MDC.remove(ATRIBUTO);
        }
    }

    private static UUID leer(String valor) {
        if (valor != null) {
            try {
                return UUID.fromString(valor);
            } catch (IllegalArgumentException e) {
                // Un id mal formado se reemplaza: el cliente no decide el formato del registro
            }
        }
        return UUID.randomUUID();
    }
}
