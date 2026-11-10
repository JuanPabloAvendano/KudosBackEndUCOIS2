package co.edu.uco.kudos.dominio.usuario;

import java.util.Objects;
import java.util.Optional;

/**
 * Raíz del agregado Usuario. Se crea o actualiza con los datos del token en cada petición; sin teléfono, el canal
 * de WhatsApp se omite para ese destinatario.
 */
public final class Usuario {

    private final UsuarioId id;
    private final String nombre;
    private final String correo;
    private final String telefono;
    private final Rol rol;

    private Usuario(UsuarioId id, String nombre, String correo, String telefono, Rol rol) {
        this.id = Objects.requireNonNull(id, "El id es obligatorio");
        this.nombre = obligatorio(nombre, "El nombre es obligatorio");
        this.correo = obligatorio(correo, "El correo es obligatorio");
        this.telefono = telefono == null || telefono.isBlank() ? null : telefono.trim();
        this.rol = Objects.requireNonNull(rol, "El rol es obligatorio");
    }

    public static Usuario desdeIdentidad(UsuarioId id, String nombre, String correo, String telefono, Rol rol) {
        return new Usuario(id, nombre, correo, telefono, rol);
    }

    private static String obligatorio(String valor, String mensaje) {
        if (valor == null || valor.isBlank()) {
            throw new IllegalArgumentException(mensaje);
        }
        return valor.trim();
    }

    public boolean tieneRol(Rol otro) {
        return rol == otro;
    }

    /** Verdadero si el token trae datos distintos a los guardados (hay que actualizar la fila). */
    public boolean difiereDe(Usuario otro) {
        return !nombre.equals(otro.nombre) || !correo.equals(otro.correo)
                || !Objects.equals(telefono, otro.telefono) || rol != otro.rol;
    }

    public UsuarioId id() {
        return id;
    }

    public String nombre() {
        return nombre;
    }

    public String correo() {
        return correo;
    }

    public Optional<String> telefono() {
        return Optional.ofNullable(telefono);
    }

    public Rol rol() {
        return rol;
    }
}
