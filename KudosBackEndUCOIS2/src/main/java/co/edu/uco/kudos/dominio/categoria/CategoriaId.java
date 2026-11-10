package co.edu.uco.kudos.dominio.categoria;

/** Identificador de la categoría. */
public record CategoriaId(int valor) {

    public CategoriaId {
        if (valor <= 0) {
            throw new IllegalArgumentException("El id de la categoría debe ser positivo");
        }
    }
}
