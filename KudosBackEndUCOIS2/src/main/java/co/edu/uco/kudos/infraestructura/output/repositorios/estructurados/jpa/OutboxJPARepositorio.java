package co.edu.uco.kudos.infraestructura.output.repositorios.estructurados.jpa;

import co.edu.uco.kudos.infraestructura.output.repositorios.estructurados.jpa.entidades.OutboxEventoEntidadJPA;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/** Outbox; la toma usa el índice parcial {@code WHERE enviado_en IS NULL}. */
public interface OutboxJPARepositorio extends JpaRepository<OutboxEventoEntidadJPA, UUID> {

    @Query(value = """
            select * from outbox_evento
            where enviado_en is null
            order by creado_en
            limit :lote
            for update skip locked""", nativeQuery = true)
    List<OutboxEventoEntidadJPA> tomarPendientes(@Param("lote") int lote);

    @Modifying
    @Query("update OutboxEventoEntidadJPA o set o.enviadoEn = :enviadoEn where o.id in :ids")
    int marcarEnviados(@Param("ids") List<UUID> ids, @Param("enviadoEn") Instant enviadoEn);
}
