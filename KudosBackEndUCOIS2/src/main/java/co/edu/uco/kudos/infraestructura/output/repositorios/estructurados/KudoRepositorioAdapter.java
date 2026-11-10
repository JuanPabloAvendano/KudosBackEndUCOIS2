package co.edu.uco.kudos.infraestructura.output.repositorios.estructurados;

import co.edu.uco.kudos.dominio.kudo.CursorDelFeed;
import co.edu.uco.kudos.dominio.kudo.Kudo;
import co.edu.uco.kudos.infraestructura.output.repositorios.estructurados.jpa.KudoJPARepositorio;
import co.edu.uco.kudos.infraestructura.output.repositorios.estructurados.jpa.entidades.KudoEntidadJPA;
import co.edu.uco.kudos.infraestructura.output.repositorios.estructurados.mapper.KudoEntidadMapper;
import co.edu.uco.kudos.servicios.output.ports.repositorios.entidades.KudoRepositorio;
import java.util.List;
import org.springframework.data.domain.Limit;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/** Kudos en PostgreSQL: inserta con sus adjuntos y lee el feed por keyset. */
@Component
public class KudoRepositorioAdapter implements KudoRepositorio {

    private final KudoJPARepositorio jpa;
    private final KudoEntidadMapper mapper;

    public KudoRepositorioAdapter(KudoJPARepositorio jpa, KudoEntidadMapper mapper) {
        this.jpa = jpa;
        this.mapper = mapper;
    }

    @Override
    public void guardar(Kudo kudo) {
        jpa.save(mapper.aEntidad(kudo));
    }

    /** Lectura en su propia transacción de solo lectura para cargar los adjuntos por lotes. */
    @Override
    @Transactional(readOnly = true)
    public List<Kudo> pagina(CursorDelFeed despuesDe, int limite) {
        List<KudoEntidadJPA> filas = despuesDe == null
                ? jpa.findAllByOrderByFechaDescIdDesc(Limit.of(limite))
                : jpa.despuesDe(despuesDe.fecha(), despuesDe.id().valor(), Limit.of(limite));
        return filas.stream().map(mapper::aDominio).toList();
    }
}
