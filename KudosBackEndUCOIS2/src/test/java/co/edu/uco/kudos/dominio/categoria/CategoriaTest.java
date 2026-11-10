package co.edu.uco.kudos.dominio.categoria;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

/** Pruebas de la categoría: estado activo y validación de id y nombre. */
class CategoriaTest {

    @Test
    void sabeSiEstaActiva() {
        assertThat(new Categoria(new CategoriaId(1), "Liderazgo", true).estaActiva()).isTrue();
        assertThat(new Categoria(new CategoriaId(2), "Retirada", false).estaActiva()).isFalse();
    }

    @Test
    void validaIdYNombre() {
        assertThatThrownBy(() -> new CategoriaId(0)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new Categoria(new CategoriaId(1), " ", true))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
