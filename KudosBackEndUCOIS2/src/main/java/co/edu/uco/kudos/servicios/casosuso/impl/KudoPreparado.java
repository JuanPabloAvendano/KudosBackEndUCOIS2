package co.edu.uco.kudos.servicios.casosuso.impl;

import co.edu.uco.kudos.dominio.kudo.Kudo;
import co.edu.uco.kudos.servicios.casosuso.ArchivoNuevo;
import co.edu.uco.kudos.servicios.output.dto.EventoKudoPublicadoDTO;
import java.util.Optional;

/** Kudo validado, su evento y el archivo por subir, listos para la transacción. */
public record KudoPreparado(Kudo kudo, EventoKudoPublicadoDTO evento, ArchivoNuevo archivo) {

    public Optional<ArchivoNuevo> archivoOpcional() {
        return Optional.ofNullable(archivo);
    }
}
