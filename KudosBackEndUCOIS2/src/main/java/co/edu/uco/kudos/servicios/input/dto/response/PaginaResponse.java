package co.edu.uco.kudos.servicios.input.dto.response;

import java.util.List;

/** Página de resultados; {@code siguienteCursor} es nulo cuando no hay más. */
public record PaginaResponse<T>(List<T> elementos, String siguienteCursor) {

    public PaginaResponse {
        elementos = List.copyOf(elementos);
    }
}
