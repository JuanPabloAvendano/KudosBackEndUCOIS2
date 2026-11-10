package co.edu.uco.kudos.servicios.casosuso;

import co.edu.uco.kudos.dominio.categoria.CategoriaId;
import co.edu.uco.kudos.dominio.usuario.UsuarioId;
import java.util.Optional;
import java.util.UUID;

/** Datos ya traducidos al dominio para publicar un Kudo. */
public record SolicitudDePublicacion(UsuarioId autor, UsuarioId destinatario, CategoriaId categoria, String contenido,
        ArchivoNuevo archivo, UUID correlationId) {

    public Optional<ArchivoNuevo> archivoOpcional() {
        return Optional.ofNullable(archivo);
    }
}
