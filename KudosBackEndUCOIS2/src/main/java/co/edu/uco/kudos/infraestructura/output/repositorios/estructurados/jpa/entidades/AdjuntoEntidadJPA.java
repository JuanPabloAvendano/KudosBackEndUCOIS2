package co.edu.uco.kudos.infraestructura.output.repositorios.estructurados.jpa.entidades;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.util.UUID;

/** Fila de {@code adjunto}: solo la referencia al archivo, que vive en el Almacén de adjuntos. */
@Entity
@Table(name = "adjunto")
public class AdjuntoEntidadJPA {

    @Id
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "kudo_id", nullable = false)
    private KudoEntidadJPA kudo;

    @Column(name = "clave_archivo", nullable = false, unique = true)
    private String claveArchivo;

    @Column(nullable = false)
    private String tipo;

    @Column(nullable = false)
    private long tamano;

    protected AdjuntoEntidadJPA() {
    }

    public AdjuntoEntidadJPA(UUID id, KudoEntidadJPA kudo, String claveArchivo, String tipo, long tamano) {
        this.id = id;
        this.kudo = kudo;
        this.claveArchivo = claveArchivo;
        this.tipo = tipo;
        this.tamano = tamano;
    }

    public UUID getId() {
        return id;
    }

    public String getClaveArchivo() {
        return claveArchivo;
    }

    public String getTipo() {
        return tipo;
    }

    public long getTamano() {
        return tamano;
    }
}
