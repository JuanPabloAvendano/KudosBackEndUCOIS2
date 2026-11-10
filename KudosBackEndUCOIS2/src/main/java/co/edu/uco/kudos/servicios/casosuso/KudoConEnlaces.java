package co.edu.uco.kudos.servicios.casosuso;

import co.edu.uco.kudos.dominio.kudo.AdjuntoId;
import co.edu.uco.kudos.dominio.kudo.Kudo;
import java.util.Map;

/** Un Kudo del feed con el enlace temporal de cada adjunto. */
public record KudoConEnlaces(Kudo kudo, Map<AdjuntoId, String> enlaces) {

    public KudoConEnlaces {
        enlaces = Map.copyOf(enlaces);
    }
}
