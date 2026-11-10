package co.edu.uco.kudos.servicios.casosuso.impl;

import static org.assertj.core.api.Assertions.assertThat;

import co.edu.uco.kudos.dobles.Dobles;
import co.edu.uco.kudos.dominio.categoria.Categoria;
import co.edu.uco.kudos.dominio.categoria.CategoriaId;
import co.edu.uco.kudos.dominio.usuario.Rol;
import co.edu.uco.kudos.dominio.usuario.Usuario;
import org.junit.jupiter.api.Test;

/** Pruebas de los casos de uso cortos: registrar usuario y consultar categorías. */
class CasosSencillosTest {

    @Test
    void registraAlUsuarioNuevoYSoloActualizaSiCambiaronSusDatos() {
        Dobles.Usuarios usuarios = new Dobles.Usuarios();
        Dobles.UnidadInmediata unidad = new Dobles.UnidadInmediata();
        RegistrarUsuarioDesdeIdentidadCasoUsoImpl registrar = new RegistrarUsuarioDesdeIdentidadCasoUsoImpl(usuarios,
                unidad);
        Usuario ana = Dobles.usuario("Ana");

        registrar.ejecutar(ana);
        registrar.ejecutar(ana);
        registrar.ejecutar(Usuario.desdeIdentidad(ana.id(), "Ana", ana.correo(), null, Rol.DOCENTE));

        assertThat(usuarios.guardados).isEqualTo(2);
        assertThat(unidad.transacciones).isEqualTo(2);
        assertThat(usuarios.filas.get(ana.id()).rol()).isEqualTo(Rol.DOCENTE);
    }

    @Test
    void consultaSoloLasCategoriasActivas() {
        Dobles.Categorias categorias = new Dobles.Categorias().con(new Categoria(new CategoriaId(1), "A", true),
                new Categoria(new CategoriaId(2), "B", false));

        assertThat(new ConsultarCategoriasCasoUsoImpl(categorias).ejecutar()).extracting(Categoria::nombre)
                .containsExactly("A");
    }
}
