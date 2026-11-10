package co.edu.uco.kudos.infraestructura.output.repositorios.estructurados.jpa;

import co.edu.uco.kudos.infraestructura.output.repositorios.estructurados.jpa.entidades.KudoEntidadJPA;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Limit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/** Feed por keyset sobre el índice {@code (fecha DESC, id DESC)}: no salta ni repite Kudos al paginar. */
public interface KudoJPARepositorio extends JpaRepository<KudoEntidadJPA, UUID> {

    List<KudoEntidadJPA> findAllByOrderByFechaDescIdDesc(Limit limite);

    @Query("""
            select k from KudoEntidadJPA k
            where k.fecha < :fecha or (k.fecha = :fecha and k.id < :id)
            order by k.fecha desc, k.id desc""")
    List<KudoEntidadJPA> despuesDe(@Param("fecha") Instant fecha, @Param("id") UUID id, Limit limite);
}
