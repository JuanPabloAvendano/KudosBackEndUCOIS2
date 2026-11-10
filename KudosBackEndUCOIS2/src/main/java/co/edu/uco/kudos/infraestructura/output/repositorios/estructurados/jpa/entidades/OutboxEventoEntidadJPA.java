package co.edu.uco.kudos.infraestructura.output.repositorios.estructurados.jpa.entidades;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import java.time.Instant;
import java.util.UUID;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import org.springframework.data.domain.Persistable;

/** Fila de {@code outbox_evento}: el evento tal cual se publicará; sin FK para que el relevo no toque lo demás. */
@Entity
@Table(name = "outbox_evento")
public class OutboxEventoEntidadJPA implements Persistable<UUID> {

    @Id
    private UUID id;

    @Column(nullable = false)
    private String tipo;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(nullable = false, columnDefinition = "jsonb")
    private String payload;

    @Column(name = "creado_en", nullable = false)
    private Instant creadoEn;

    @Column(name = "enviado_en")
    private Instant enviadoEn;

    @Transient
    private boolean nuevo;

    protected OutboxEventoEntidadJPA() {
    }

    public OutboxEventoEntidadJPA(UUID id, String tipo, String payload, Instant creadoEn) {
        this.id = id;
        this.tipo = tipo;
        this.payload = payload;
        this.creadoEn = creadoEn;
        this.nuevo = true;
    }

    @Override
    public UUID getId() {
        return id;
    }

    @Override
    public boolean isNew() {
        return nuevo;
    }

    public String getPayload() {
        return payload;
    }

    public Instant getEnviadoEn() {
        return enviadoEn;
    }
}
