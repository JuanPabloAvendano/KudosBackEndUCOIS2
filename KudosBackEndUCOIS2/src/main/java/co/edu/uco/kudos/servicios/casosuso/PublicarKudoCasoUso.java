package co.edu.uco.kudos.servicios.casosuso;

import co.edu.uco.kudos.dominio.kudo.Kudo;

/** Publica un Kudo: lo guarda junto con su evento y avisa después del commit. */
public interface PublicarKudoCasoUso {

    Kudo ejecutar(SolicitudDePublicacion solicitud);
}
