package co.edu.uco.kudos.infraestructura.output.repositorios.estructurados.mapper;

import co.edu.uco.kudos.dominio.kudo.Kudo;
import co.edu.uco.kudos.infraestructura.output.repositorios.estructurados.jpa.entidades.KudoEntidadJPA;

/** Traductor entre el Kudo del dominio (con sus adjuntos) y sus filas JPA. */
public interface KudoEntidadMapper {

    KudoEntidadJPA aEntidad(Kudo kudo);

    Kudo aDominio(KudoEntidadJPA entidad);
}
