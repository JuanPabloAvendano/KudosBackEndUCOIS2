package co.edu.uco.kudos.servicios.input.dto.response;

import java.util.UUID;

/** Persona registrada: solo lo necesario para elegirla y nombrarla (sin correo ni teléfono). */
public record UsuarioResponse(UUID id, String nombre) {
}
