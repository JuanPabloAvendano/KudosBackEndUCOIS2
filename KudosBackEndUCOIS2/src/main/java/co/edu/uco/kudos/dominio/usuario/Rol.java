package co.edu.uco.kudos.dominio.usuario;

import java.util.Arrays;
import java.util.Optional;

/** Los siete roles institucionales del enunciado; cada persona tiene un rol activo. */
public enum Rol {
    ADMINISTRADOR_SISTEMA,
    RECTOR,
    COORDINADOR_ACADEMICO,
    COORDINADOR_FACULTAD,
    COORDINADOR_PROGRAMA,
    DOCENTE,
    ESTUDIANTE;

    /** Busca el rol por nombre, sin distinguir mayúsculas. */
    public static Optional<Rol> desde(String nombre) {
        if (nombre == null) {
            return Optional.empty();
        }
        return Arrays.stream(values()).filter(rol -> rol.name().equalsIgnoreCase(nombre.trim())).findFirst();
    }
}
