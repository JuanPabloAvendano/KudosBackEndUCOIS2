package co.edu.uco.kudos.infraestructura.input.controladores;

import co.edu.uco.kudos.servicios.input.dto.response.UsuarioResponse;
import co.edu.uco.kudos.servicios.input.ports.ConsultarUsuariosInputPort;
import java.util.List;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Personas registradas, para el selector de destinatario y los nombres del feed. */
@RestController
@RequestMapping("/api/usuarios")
public class UsuarioControlador {

    private final ConsultarUsuariosInputPort consultarUsuarios;
    private final IdentidadDesdeToken identidad;

    public UsuarioControlador(ConsultarUsuariosInputPort consultarUsuarios, IdentidadDesdeToken identidad) {
        this.consultarUsuarios = consultarUsuarios;
        this.identidad = identidad;
    }

    @GetMapping
    public List<UsuarioResponse> registrados(@AuthenticationPrincipal Jwt jwt) {
        return consultarUsuarios.consultar(identidad.leer(jwt));
    }
}
