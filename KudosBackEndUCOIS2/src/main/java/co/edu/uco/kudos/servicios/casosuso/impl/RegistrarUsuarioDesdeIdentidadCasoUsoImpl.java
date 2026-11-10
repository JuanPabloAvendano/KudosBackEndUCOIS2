package co.edu.uco.kudos.servicios.casosuso.impl;

import co.edu.uco.kudos.dominio.usuario.Usuario;
import co.edu.uco.kudos.servicios.casosuso.RegistrarUsuarioDesdeIdentidadCasoUso;
import co.edu.uco.kudos.servicios.output.ports.repositorios.UnidadDeTrabajo;
import co.edu.uco.kudos.servicios.output.ports.repositorios.entidades.UsuarioRepositorio;

/** Guarda al usuario del token solo si es nuevo o si cambió algún dato; si no, no toca la base de datos. */
public final class RegistrarUsuarioDesdeIdentidadCasoUsoImpl implements RegistrarUsuarioDesdeIdentidadCasoUso {

    private final UsuarioRepositorio usuarios;
    private final UnidadDeTrabajo unidadDeTrabajo;

    public RegistrarUsuarioDesdeIdentidadCasoUsoImpl(UsuarioRepositorio usuarios, UnidadDeTrabajo unidadDeTrabajo) {
        this.usuarios = usuarios;
        this.unidadDeTrabajo = unidadDeTrabajo;
    }

    @Override
    public Usuario ejecutar(Usuario desdeToken) {
        boolean hayQueGuardar = usuarios.porId(desdeToken.id())
                .map(guardado -> guardado.difiereDe(desdeToken))
                .orElse(true);
        if (hayQueGuardar) {
            unidadDeTrabajo.ejecutar(() -> usuarios.guardar(desdeToken));
        }
        return desdeToken;
    }
}
