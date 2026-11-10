package co.edu.uco.kudos.infraestructura.output.repositorios.semiestructurados;

import co.edu.uco.kudos.servicios.output.dto.RegistroAuditoriaDTO;
import co.edu.uco.kudos.servicios.output.ports.repositorios.Auditoria;
import com.azure.data.tables.TableClient;
import com.azure.data.tables.models.TableEntity;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

/**
 * Registro de auditoría en Azure Table Storage (ADR-012): solo inserta. Partición = día UTC; fila = id del registro
 * (UUID v7, ordenado en el tiempo).
 */
public class AuditoriaTableAdapter implements Auditoria {

    private static final DateTimeFormatter DIA = DateTimeFormatter.ISO_LOCAL_DATE.withZone(ZoneOffset.UTC);

    private final TableClient tabla;

    public AuditoriaTableAdapter(TableClient tabla) {
        this.tabla = tabla;
    }

    @Override
    public void registrar(RegistroAuditoriaDTO registro) {
        TableEntity entidad = new TableEntity(DIA.format(registro.ocurridoEn()), registro.id().toString())
                .addProperty("Accion", registro.accion())
                .addProperty("Recurso", registro.recurso())
                .addProperty("CorrelationId", registro.correlationId().toString())
                .addProperty("OcurridoEn", registro.ocurridoEn().atOffset(ZoneOffset.UTC));
        if (registro.actorId() != null) {
            entidad.addProperty("ActorId", registro.actorId().toString());
        }
        tabla.createEntity(entidad);
    }
}
