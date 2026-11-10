package co.edu.uco.kudos.servicios.output.ports.repositorios.entidades;

import co.edu.uco.kudos.dominio.kudo.CursorDelFeed;
import co.edu.uco.kudos.dominio.kudo.Kudo;
import java.util.List;

/** Kudos con sus adjuntos. */
public interface KudoRepositorio {

    void guardar(Kudo kudo);

    /**
     * Kudos del más reciente al más antiguo.
     *
     * @param despuesDe posición del último Kudo visto; nulo para la primera página
     */
    List<Kudo> pagina(CursorDelFeed despuesDe, int limite);
}
