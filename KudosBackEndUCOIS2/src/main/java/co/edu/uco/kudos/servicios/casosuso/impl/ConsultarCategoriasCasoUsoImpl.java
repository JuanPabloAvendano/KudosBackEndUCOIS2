package co.edu.uco.kudos.servicios.casosuso.impl;

import co.edu.uco.kudos.dominio.categoria.Categoria;
import co.edu.uco.kudos.servicios.casosuso.ConsultarCategoriasCasoUso;
import co.edu.uco.kudos.servicios.output.ports.repositorios.entidades.CategoriaRepositorio;
import java.util.List;

/** Devuelve las categorías activas para el formulario de publicar. */
public final class ConsultarCategoriasCasoUsoImpl implements ConsultarCategoriasCasoUso {

    private final CategoriaRepositorio categorias;

    public ConsultarCategoriasCasoUsoImpl(CategoriaRepositorio categorias) {
        this.categorias = categorias;
    }

    @Override
    public List<Categoria> ejecutar() {
        return categorias.activas();
    }
}
