package co.edu.uco.kudos.infraestructura.output.repositorios.estructurados.mapper.impl;

import co.edu.uco.kudos.dominio.categoria.CategoriaId;
import co.edu.uco.kudos.dominio.kudo.Adjunto;
import co.edu.uco.kudos.dominio.kudo.AdjuntoId;
import co.edu.uco.kudos.dominio.kudo.Kudo;
import co.edu.uco.kudos.dominio.kudo.KudoId;
import co.edu.uco.kudos.dominio.kudo.TipoDeArchivo;
import co.edu.uco.kudos.dominio.usuario.UsuarioId;
import co.edu.uco.kudos.infraestructura.output.repositorios.estructurados.jpa.entidades.AdjuntoEntidadJPA;
import co.edu.uco.kudos.infraestructura.output.repositorios.estructurados.jpa.entidades.KudoEntidadJPA;
import co.edu.uco.kudos.infraestructura.output.repositorios.estructurados.mapper.KudoEntidadMapper;
import org.springframework.stereotype.Component;

/** Arma las filas de {@code kudo} y {@code adjunto} desde el dominio y reconstruye el Kudo al leerlas. */
@Component
public class KudoEntidadMapperImpl implements KudoEntidadMapper {

    @Override
    public KudoEntidadJPA aEntidad(Kudo kudo) {
        KudoEntidadJPA entidad = new KudoEntidadJPA(kudo.id().valor(), kudo.autor().valor(),
                kudo.destinatario().valor(), kudo.categoria().valor(), kudo.contenido(), kudo.fecha());
        kudo.adjuntos().forEach(adjunto -> entidad.agregarAdjunto(new AdjuntoEntidadJPA(adjunto.id().valor(),
                entidad, adjunto.claveArchivo(), adjunto.tipo().mime(), adjunto.tamano())));
        return entidad;
    }

    @Override
    public Kudo aDominio(KudoEntidadJPA entidad) {
        return Kudo.reconstruir(new KudoId(entidad.getId()), new UsuarioId(entidad.getAutorId()),
                new UsuarioId(entidad.getDestinatarioId()), new CategoriaId(entidad.getCategoriaId()),
                entidad.getContenido(), entidad.getFecha(),
                entidad.getAdjuntos().stream().map(KudoEntidadMapperImpl::adjuntoADominio).toList());
    }

    private static Adjunto adjuntoADominio(AdjuntoEntidadJPA adjunto) {
        TipoDeArchivo tipo = TipoDeArchivo.porMime(adjunto.getTipo())
                .orElseThrow(() -> new IllegalStateException("Tipo de adjunto desconocido: " + adjunto.getTipo()));
        return new Adjunto(new AdjuntoId(adjunto.getId()), adjunto.getClaveArchivo(), tipo, adjunto.getTamano());
    }
}
