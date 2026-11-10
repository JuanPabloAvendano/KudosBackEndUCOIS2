package co.edu.uco.kudos.infraestructura.output.repositorios.estructurados.mapper.impl;

import co.edu.uco.kudos.dominio.categoria.Categoria;
import co.edu.uco.kudos.dominio.categoria.CategoriaId;
import co.edu.uco.kudos.infraestructura.output.repositorios.estructurados.jpa.entidades.CategoriaEntidadJPA;
import co.edu.uco.kudos.infraestructura.output.repositorios.estructurados.mapper.CategoriaEntidadMapper;
import org.springframework.stereotype.Component;

/** Reconstruye la Categoría del dominio desde su fila. */
@Component
public class CategoriaEntidadMapperImpl implements CategoriaEntidadMapper {

    @Override
    public Categoria aDominio(CategoriaEntidadJPA entidad) {
        return new Categoria(new CategoriaId(entidad.getId()), entidad.getNombre(), entidad.isActiva());
    }
}
