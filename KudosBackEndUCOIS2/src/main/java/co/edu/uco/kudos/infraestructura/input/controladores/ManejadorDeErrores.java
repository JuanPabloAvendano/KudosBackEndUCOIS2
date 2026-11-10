package co.edu.uco.kudos.infraestructura.input.controladores;

import co.edu.uco.kudos.dominio.reglas.ReglaIncumplida;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

/**
 * Traduce los errores a {@code application/problem+json} (RFC 9457). Una regla del negocio sale como 422 con su
 * código; un fallo técnico sale como 500 sin detalles internos (A05 de OWASP).
 */
@RestControllerAdvice
public class ManejadorDeErrores {

    private static final Logger LOG = LoggerFactory.getLogger(ManejadorDeErrores.class);

    @ExceptionHandler(ReglaIncumplida.class)
    ProblemDetail reglaIncumplida(ReglaIncumplida e) {
        ProblemDetail problema = ProblemDetail.forStatusAndDetail(HttpStatus.UNPROCESSABLE_CONTENT, e.getMessage());
        problema.setTitle("Regla del negocio incumplida");
        problema.setProperty("codigo", e.codigo().name());
        return problema;
    }

    @ExceptionHandler({MissingServletRequestParameterException.class, MethodArgumentTypeMismatchException.class,
            IllegalArgumentException.class})
    ProblemDetail solicitudInvalida(Exception e) {
        ProblemDetail problema = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, e.getMessage());
        problema.setTitle("Solicitud inválida");
        return problema;
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    ProblemDetail adjuntoMuyGrande(MaxUploadSizeExceededException e) {
        ProblemDetail problema = ProblemDetail.forStatusAndDetail(HttpStatus.CONTENT_TOO_LARGE,
                "El adjunto supera el tamaño permitido");
        problema.setTitle("Adjunto muy grande");
        return problema;
    }

    @ExceptionHandler(AccessDeniedException.class)
    ProblemDetail accesoDenegado(AccessDeniedException e) {
        ProblemDetail problema = ProblemDetail.forStatusAndDetail(HttpStatus.FORBIDDEN, e.getMessage());
        problema.setTitle("Acceso denegado");
        return problema;
    }

    @ExceptionHandler(Exception.class)
    ProblemDetail errorInterno(Exception e) {
        LOG.error("Error no controlado", e);
        ProblemDetail problema = ProblemDetail.forStatusAndDetail(HttpStatus.INTERNAL_SERVER_ERROR,
                "Ocurrió un error inesperado; intenta de nuevo");
        problema.setTitle("Error interno");
        return problema;
    }
}
