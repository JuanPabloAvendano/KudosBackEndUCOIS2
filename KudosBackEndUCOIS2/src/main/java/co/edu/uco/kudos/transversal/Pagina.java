package co.edu.uco.kudos.transversal;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;

/** Una página de resultados y el cursor opaco para pedir la siguiente (vacío si no hay más). */
public record Pagina<T>(List<T> elementos, String siguienteCursor) {

    public Pagina {
        elementos = List.copyOf(elementos);
    }

    public Optional<String> siguiente() {
        return Optional.ofNullable(siguienteCursor);
    }

    public <R> Pagina<R> mapear(Function<T, R> funcion) {
        return new Pagina<>(elementos.stream().map(funcion).toList(), siguienteCursor);
    }
}
