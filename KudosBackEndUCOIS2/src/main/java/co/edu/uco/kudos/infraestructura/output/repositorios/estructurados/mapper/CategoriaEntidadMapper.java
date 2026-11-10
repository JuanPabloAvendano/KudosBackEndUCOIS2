package co.edu.uco.kudos.infraestructura.output.repositorios.estructurados.mapper;

import co.edu.uco.kudos.dominio.categoria.Categoria;
import co.edu.uco.kudos.infraestructura.output.repositorios.estructurados.jpa.entidades.CategoriaEntidadJPA;

/** Traductor entre la Categoría del dominio y su fila JPA. */
public interface CategoriaEntidadMapper {

    Categoria aDominio(CategoriaEntidadJPA entidad);
}
