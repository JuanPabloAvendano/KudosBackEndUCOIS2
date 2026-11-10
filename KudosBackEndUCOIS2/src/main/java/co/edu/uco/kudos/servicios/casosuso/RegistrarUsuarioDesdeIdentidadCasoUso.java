package co.edu.uco.kudos.servicios.casosuso;

import co.edu.uco.kudos.dominio.usuario.Usuario;

/** Crea o actualiza al usuario con los datos del token (así existe cuando alguien le manda un Kudo). */
public interface RegistrarUsuarioDesdeIdentidadCasoUso {

    Usuario ejecutar(Usuario desdeToken);
}
