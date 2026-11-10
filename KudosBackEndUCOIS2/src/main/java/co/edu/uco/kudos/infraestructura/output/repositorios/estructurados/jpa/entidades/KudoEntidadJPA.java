package co.edu.uco.kudos.infraestructura.output.repositorios.estructurados.jpa.entidades;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.hibernate.annotations.BatchSize;
import org.springframework.data.domain.Persistable;

/**
 * Fila de {@code kudo} con sus adjuntos. El id lo genera el dominio (UUID v7), por eso implementa
 * {@link Persistable}: un Kudo siempre se inserta, nunca se actualiza.
 */
@Entity
@Table(name = "kudo")
public class KudoEntidadJPA implements Persistable<UUID> {

    @Id
    private UUID id;

    @Column(name = "autor_id", nullable = false)
    private UUID autorId;

    @Column(name = "destinatario_id", nullable = false)
    private UUID destinatarioId;

    @Column(name = "categoria_id", nullable = false)
    private Integer categoriaId;

    @Column(nullable = false)
    private String contenido;

    @Column(nullable = false)
    private Instant fecha;

    @OneToMany(mappedBy = "kudo", cascade = CascadeType.PERSIST)
    @BatchSize(size = 50)
    private List<AdjuntoEntidadJPA> adjuntos = new ArrayList<>();

    @Transient
    private boolean nuevo;

    protected KudoEntidadJPA() {
    }

    public KudoEntidadJPA(UUID id, UUID autorId, UUID destinatarioId, Integer categoriaId, String contenido,
            Instant fecha) {
        this.id = id;
        this.autorId = autorId;
        this.destinatarioId = destinatarioId;
        this.categoriaId = categoriaId;
        this.contenido = contenido;
        this.fecha = fecha;
        this.nuevo = true;
    }

    public void agregarAdjunto(AdjuntoEntidadJPA adjunto) {
        adjuntos.add(adjunto);
    }

    @Override
    public UUID getId() {
        return id;
    }

    @Override
    public boolean isNew() {
        return nuevo;
    }

    public UUID getAutorId() {
        return autorId;
    }

    public UUID getDestinatarioId() {
        return destinatarioId;
    }

    public Integer getCategoriaId() {
        return categoriaId;
    }

    public String getContenido() {
        return contenido;
    }

    public Instant getFecha() {
        return fecha;
    }

    public List<AdjuntoEntidadJPA> getAdjuntos() {
        return adjuntos;
    }
}
