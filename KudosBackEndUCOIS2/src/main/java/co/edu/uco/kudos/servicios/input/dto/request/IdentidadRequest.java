package co.edu.uco.kudos.servicios.input.dto.request;

import java.util.UUID;

/** Quién hace la petición, según el token del Identity Provider. */
public record IdentidadRequest(UUID id, String nombre, String correo, String telefono, String rol) {
}
