package co.edu.uco.kudos.servicios.casosuso;

import co.edu.uco.kudos.dominio.usuario.UsuarioId;

/** URL del canal push del usuario. */
public interface ObtenerConexionPushCasoUso {

    String ejecutar(UsuarioId usuario);
}
