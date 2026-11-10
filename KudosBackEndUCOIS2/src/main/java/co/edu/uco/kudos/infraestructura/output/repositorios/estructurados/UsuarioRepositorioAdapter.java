package co.edu.uco.kudos.infraestructura.output.repositorios.estructurados;

import co.edu.uco.kudos.dominio.usuario.Usuario;
import co.edu.uco.kudos.dominio.usuario.UsuarioId;
import co.edu.uco.kudos.infraestructura.output.repositorios.estructurados.jpa.UsuarioJPARepositorio;
import co.edu.uco.kudos.infraestructura.output.repositorios.estructurados.jpa.entidades.UsuarioEntidadJPA;
import co.edu.uco.kudos.infraestructura.output.repositorios.estructurados.mapper.UsuarioEntidadMapper;
import co.edu.uco.kudos.servicios.output.ports.repositorios.entidades.UsuarioRepositorio;
import co.edu.uco.kudos.servicios.output.ports.serviciosexternos.Reloj;
import java.util.Optional;
import org.springframework.stereotype.Component;

/** Usuarios en PostgreSQL: inserta al nuevo o actualiza sus datos con los del token. */
@Component
public class UsuarioRepositorioAdapter implements UsuarioRepositorio {

    private final UsuarioJPARepositorio jpa;
    private final UsuarioEntidadMapper mapper;
    private final Reloj reloj;

    public UsuarioRepositorioAdapter(UsuarioJPARepositorio jpa, UsuarioEntidadMapper mapper, Reloj reloj) {
        this.jpa = jpa;
        this.mapper = mapper;
        this.reloj = reloj;
    }

    @Override
    public Optional<Usuario> porId(UsuarioId id) {
        return jpa.findById(id.valor()).map(mapper::aDominio);
    }

    @Override
    public void guardar(Usuario usuario) {
        String telefono = usuario.telefono().orElse(null);
        UsuarioEntidadJPA entidad = jpa.findById(usuario.id().valor())
                .map(existente -> {
                    existente.actualizar(usuario.nombre(), usuario.correo(), telefono, usuario.rol().name(),
                            reloj.ahora());
                    return existente;
                })
                .orElseGet(() -> new UsuarioEntidadJPA(usuario.id().valor(), usuario.nombre(), usuario.correo(),
                        telefono, usuario.rol().name(), reloj.ahora()));
        jpa.save(entidad);
    }
}
