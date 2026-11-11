package co.edu.uco.kudos.servicios.input.adapter;

import co.edu.uco.kudos.dominio.usuario.Usuario;
import co.edu.uco.kudos.servicios.casosuso.ObtenerConexionPushCasoUso;
import co.edu.uco.kudos.servicios.casosuso.RegistrarUsuarioDesdeIdentidadCasoUso;
import co.edu.uco.kudos.servicios.input.dto.request.IdentidadRequest;
import co.edu.uco.kudos.servicios.input.dto.response.ConexionPushResponse;
import co.edu.uco.kudos.servicios.input.ports.ObtenerConexionPushInputPort;

/** Registra a quien se conecta y le entrega la URL de su canal push. */
public final class ObtenerConexionPushInputAdapter implements ObtenerConexionPushInputPort {

    private final RegistrarUsuarioDesdeIdentidadCasoUso registrarUsuario;
    private final ObtenerConexionPushCasoUso obtenerConexion;

    public ObtenerConexionPushInputAdapter(RegistrarUsuarioDesdeIdentidadCasoUso registrarUsuario,
            ObtenerConexionPushCasoUso obtenerConexion) {
        this.registrarUsuario = registrarUsuario;
        this.obtenerConexion = obtenerConexion;
    }

    @Override
    public ConexionPushResponse conectar(IdentidadRequest identidad) {
        Usuario usuario = IdentidadMapper.aUsuario(identidad);
        registrarUsuario.ejecutar(usuario);
        return new ConexionPushResponse(obtenerConexion.ejecutar(usuario.id()));
    }
}
