package co.edu.uco.kudos.servicios.casosuso;

import co.edu.uco.kudos.dominio.usuario.Usuario;
import java.util.List;

/** Personas registradas a las que se les puede dar un Kudo. */
public interface ConsultarUsuariosCasoUso {

    List<Usuario> ejecutar();
}
