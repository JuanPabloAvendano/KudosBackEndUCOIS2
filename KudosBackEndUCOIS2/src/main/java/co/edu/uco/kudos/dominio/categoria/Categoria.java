package co.edu.uco.kudos.dominio.categoria;

import java.util.Objects;

/** Raíz del agregado Categoría; Reconocimientos la consulta para validar el Kudo. */
public record Categoria(CategoriaId id, String nombre, boolean activa) {

    public Categoria {
        Objects.requireNonNull(id, "El id es obligatorio");
        if (nombre == null || nombre.isBlank()) {
            throw new IllegalArgumentException("El nombre es obligatorio");
        }
    }

    public boolean estaActiva() {
        return activa;
    }
}
