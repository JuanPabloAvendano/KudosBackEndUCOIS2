package co.edu.uco.kudos.servicios.casosuso.impl;

import co.edu.uco.kudos.dominio.usuario.Usuario;
import co.edu.uco.kudos.servicios.casosuso.ConsultarUsuariosCasoUso;
import co.edu.uco.kudos.servicios.output.ports.repositorios.entidades.UsuarioRepositorio;
import java.util.List;

/** Devuelve los usuarios registrados (quienes ya entraron al menos una vez), ordenados por nombre. */
public final class ConsultarUsuariosCasoUsoImpl implements ConsultarUsuariosCasoUso {

    private final UsuarioRepositorio usuarios;

    public ConsultarUsuariosCasoUsoImpl(UsuarioRepositorio usuarios) {
        this.usuarios = usuarios;
    }

    @Override
    public List<Usuario> ejecutar() {
        return usuarios.todos();
    }
}
