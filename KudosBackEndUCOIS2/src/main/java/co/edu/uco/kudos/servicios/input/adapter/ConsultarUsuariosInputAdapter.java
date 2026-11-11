package co.edu.uco.kudos.servicios.input.adapter;

import co.edu.uco.kudos.servicios.casosuso.ConsultarUsuariosCasoUso;
import co.edu.uco.kudos.servicios.casosuso.RegistrarUsuarioDesdeIdentidadCasoUso;
import co.edu.uco.kudos.servicios.input.dto.request.IdentidadRequest;
import co.edu.uco.kudos.servicios.input.dto.response.UsuarioResponse;
import co.edu.uco.kudos.servicios.input.ports.ConsultarUsuariosInputPort;
import java.util.List;

/** Registra a quien consulta (así aparece en la lista) y entrega id y nombre de cada usuario registrado. */
public final class ConsultarUsuariosInputAdapter implements ConsultarUsuariosInputPort {

    private final RegistrarUsuarioDesdeIdentidadCasoUso registrarUsuario;
    private final ConsultarUsuariosCasoUso consultarUsuarios;

    public ConsultarUsuariosInputAdapter(RegistrarUsuarioDesdeIdentidadCasoUso registrarUsuario,
            ConsultarUsuariosCasoUso consultarUsuarios) {
        this.registrarUsuario = registrarUsuario;
        this.consultarUsuarios = consultarUsuarios;
    }

    @Override
    public List<UsuarioResponse> consultar(IdentidadRequest identidad) {
        registrarUsuario.ejecutar(IdentidadMapper.aUsuario(identidad));
        return consultarUsuarios.ejecutar().stream()
                .map(usuario -> new UsuarioResponse(usuario.id().valor(), usuario.nombre()))
                .toList();
    }
}
