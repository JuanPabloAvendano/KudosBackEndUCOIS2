package co.edu.uco.kudos.infraestructura.output.repositorios.estructurados.jpa.entidades;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.PostLoad;
import jakarta.persistence.PostPersist;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import java.time.Instant;
import java.util.UUID;
import org.springframework.data.domain.Persistable;

/**
 * Fila de {@code usuario}. El id viene del token (no se genera): implementa {@link Persistable} para que guardar un
 * usuario nuevo no haga un SELECT previo (revisión del experto en persistencia, 9-nov).
 */
@Entity
@Table(name = "usuario")
public class UsuarioEntidadJPA implements Persistable<UUID> {

    @Id
    private UUID id;

    @Column(nullable = false)
    private String nombre;

    @Column(nullable = false)
    private String correo;

    private String telefono;

    @Column(nullable = false)
    private String rol;

    @Column(name = "actualizado_en", nullable = false)
    private Instant actualizadoEn;

    @Transient
    private boolean nuevo = true;

    protected UsuarioEntidadJPA() {
    }

    public UsuarioEntidadJPA(UUID id, String nombre, String correo, String telefono, String rol,
            Instant actualizadoEn) {
        this.id = id;
        this.nombre = nombre;
        this.correo = correo;
        this.telefono = telefono;
        this.rol = rol;
        this.actualizadoEn = actualizadoEn;
    }

    /** Copia los datos del token sobre una fila ya guardada. */
    public void actualizar(String nombre, String correo, String telefono, String rol, Instant actualizadoEn) {
        this.nombre = nombre;
        this.correo = correo;
        this.telefono = telefono;
        this.rol = rol;
        this.actualizadoEn = actualizadoEn;
    }

    @PostLoad
    @PostPersist
    void marcarGuardado() {
        this.nuevo = false;
    }

    @Override
    public UUID getId() {
        return id;
    }

    @Override
    public boolean isNew() {
        return nuevo;
    }

    public String getNombre() {
        return nombre;
    }

    public String getCorreo() {
        return correo;
    }

    public String getTelefono() {
        return telefono;
    }

    public String getRol() {
        return rol;
    }
}
