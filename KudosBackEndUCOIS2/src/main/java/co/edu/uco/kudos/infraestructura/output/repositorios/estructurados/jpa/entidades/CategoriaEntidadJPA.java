package co.edu.uco.kudos.infraestructura.output.repositorios.estructurados.jpa.entidades;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/** Fila de {@code categoria}. */
@Entity
@Table(name = "categoria")
public class CategoriaEntidadJPA {

    @Id
    private Integer id;

    @Column(nullable = false, unique = true)
    private String nombre;

    @Column(nullable = false)
    private boolean activa;

    protected CategoriaEntidadJPA() {
    }

    public Integer getId() {
        return id;
    }

    public String getNombre() {
        return nombre;
    }

    public boolean isActiva() {
        return activa;
    }
}
