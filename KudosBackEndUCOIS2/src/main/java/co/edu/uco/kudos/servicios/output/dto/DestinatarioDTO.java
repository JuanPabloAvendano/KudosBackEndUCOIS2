package co.edu.uco.kudos.servicios.output.dto;

import java.util.UUID;

/** Datos de contacto del destinatario en el evento. */
public record DestinatarioDTO(UUID id, String nombre, String correo, String telefono) {
}
