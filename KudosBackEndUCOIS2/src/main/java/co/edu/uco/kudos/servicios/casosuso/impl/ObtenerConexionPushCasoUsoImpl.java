package co.edu.uco.kudos.servicios.casosuso.impl;

import co.edu.uco.kudos.dominio.usuario.UsuarioId;
import co.edu.uco.kudos.servicios.casosuso.ObtenerConexionPushCasoUso;
import co.edu.uco.kudos.servicios.output.ports.serviciosexternos.ConexionesPush;

/**
 * Pide al servicio de push la URL del usuario. La Logic App le envía a ese mismo usuario (userId = id del
 * destinatario), así que cada quien solo recibe sus propios Kudos.
 */
public final class ObtenerConexionPushCasoUsoImpl implements ObtenerConexionPushCasoUso {

    private final ConexionesPush conexiones;

    public ObtenerConexionPushCasoUsoImpl(ConexionesPush conexiones) {
        this.conexiones = conexiones;
    }

    @Override
    public String ejecutar(UsuarioId usuario) {
        return conexiones.urlDeConexion(usuario);
    }
}
