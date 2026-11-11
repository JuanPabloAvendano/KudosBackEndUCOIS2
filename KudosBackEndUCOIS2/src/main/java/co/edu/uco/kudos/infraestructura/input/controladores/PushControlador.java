package co.edu.uco.kudos.infraestructura.input.controladores;

import co.edu.uco.kudos.servicios.input.dto.response.ConexionPushResponse;
import co.edu.uco.kudos.servicios.input.ports.ObtenerConexionPushInputPort;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Conexión push: el front pide aquí la URL de Web PubSub y abre el WebSocket con ella. */
@RestController
@RequestMapping("/api/push")
public class PushControlador {

    private final ObtenerConexionPushInputPort obtenerConexion;
    private final IdentidadDesdeToken identidad;

    public PushControlador(ObtenerConexionPushInputPort obtenerConexion, IdentidadDesdeToken identidad) {
        this.obtenerConexion = obtenerConexion;
        this.identidad = identidad;
    }

    @GetMapping("/conexion")
    public ConexionPushResponse conexion(@AuthenticationPrincipal Jwt jwt) {
        return obtenerConexion.conectar(identidad.leer(jwt));
    }
}
