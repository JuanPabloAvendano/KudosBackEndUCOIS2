package co.edu.uco.kudos.servicios.output.ports.serviciosexternos;

import co.edu.uco.kudos.dominio.usuario.UsuarioId;

/** Servicio de push en tiempo real: da la URL con la que el navegador de un usuario abre su WebSocket. */
public interface ConexionesPush {

    /** URL firmada y temporal para que el usuario reciba sus notificaciones. */
    String urlDeConexion(UsuarioId usuario);
}
