package co.edu.uco.kudos.servicios.casosuso;

import co.edu.uco.kudos.dominio.kudo.CursorDelFeed;
import co.edu.uco.kudos.transversal.Pagina;

/** Consulta el feed paginado por keyset; la primera página sale de la caché. */
public interface ConsultarFeedCasoUso {

    /**
     * @param cursor posición del último Kudo visto; nulo para la primera página
     * @param tamano tamaño pedido; nulo para el del catálogo
     */
    Pagina<KudoConEnlaces> ejecutar(CursorDelFeed cursor, Integer tamano);
}
