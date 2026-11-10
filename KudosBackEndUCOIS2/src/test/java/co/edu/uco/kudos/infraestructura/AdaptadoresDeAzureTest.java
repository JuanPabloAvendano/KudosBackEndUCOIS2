package co.edu.uco.kudos.infraestructura;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import co.edu.uco.kudos.dobles.Dobles;
import co.edu.uco.kudos.infraestructura.output.mensajeria.productores.EventGridPublicadorAdapter;
import co.edu.uco.kudos.infraestructura.output.repositorios.noestructurados.AlmacenAdjuntosBlobAdapter;
import co.edu.uco.kudos.infraestructura.output.repositorios.semiestructurados.AuditoriaTableAdapter;
import co.edu.uco.kudos.servicios.output.dto.DestinatarioDTO;
import co.edu.uco.kudos.servicios.output.dto.EventoKudoPublicadoDTO;
import co.edu.uco.kudos.servicios.output.dto.RegistroAuditoriaDTO;
import com.azure.core.models.CloudEvent;
import com.azure.data.tables.TableClient;
import com.azure.data.tables.models.TableEntity;
import com.azure.messaging.eventgrid.EventGridPublisherClient;
import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.BlobServiceClient;
import com.azure.storage.blob.models.UserDelegationKey;
import com.azure.storage.blob.options.BlobParallelUploadOptions;
import com.azure.storage.blob.sas.BlobServiceSasSignatureValues;
import java.io.ByteArrayInputStream;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import tools.jackson.databind.json.JsonMapper;

/** Los adaptadores de Azure arman bien cada llamada al SDK (sin red: el SDK va con dobles de Mockito). */
class AdaptadoresDeAzureTest {

    private static final EventoKudoPublicadoDTO EVENTO = new EventoKudoPublicadoDTO(UUID.randomUUID(),
            UUID.randomUUID(), "Ana", new DestinatarioDTO(UUID.randomUUID(), "Luis", "luis@uco.edu.co", "+57300"),
            "Liderazgo", Dobles.AHORA, UUID.randomUUID());

    @Test
    void elBlobSubeConSuTipoDaUnEnlaceSasDeSoloLecturaYBorra() {
        BlobServiceClient servicio = mock(BlobServiceClient.class);
        BlobContainerClient contenedor = mock(BlobContainerClient.class);
        BlobClient blob = mock(BlobClient.class);
        when(servicio.getBlobContainerClient("adjuntos")).thenReturn(contenedor);
        when(contenedor.getBlobClient("kudos/a/b")).thenReturn(blob);
        when(blob.getBlobUrl()).thenReturn("https://cuenta.blob.core.windows.net/adjuntos/kudos/a/b");
        UserDelegationKey llave = new UserDelegationKey();
        when(servicio.getUserDelegationKey(any(OffsetDateTime.class), any(OffsetDateTime.class))).thenReturn(llave);
        when(blob.generateUserDelegationSas(any(BlobServiceSasSignatureValues.class), eq(llave))).thenReturn("sig=1");
        AlmacenAdjuntosBlobAdapter adapter = new AlmacenAdjuntosBlobAdapter(servicio, "adjuntos", new Dobles.RelojFijo());

        adapter.guardar("kudos/a/b", "image/png", 3, new ByteArrayInputStream(new byte[3]));
        String enlace = adapter.enlaceTemporal("kudos/a/b", Duration.ofMinutes(10));
        adapter.eliminar("kudos/a/b");

        ArgumentCaptor<BlobParallelUploadOptions> opciones = ArgumentCaptor.forClass(BlobParallelUploadOptions.class);
        verify(blob).uploadWithResponse(opciones.capture(), isNull(), isNull());
        assertThat(opciones.getValue().getHeaders().getContentType()).isEqualTo("image/png");
        ArgumentCaptor<BlobServiceSasSignatureValues> sas = ArgumentCaptor.forClass(BlobServiceSasSignatureValues.class);
        verify(blob).generateUserDelegationSas(sas.capture(), eq(llave));
        assertThat(sas.getValue().getPermissions()).isEqualTo("r");
        assertThat(sas.getValue().getExpiryTime()).isEqualTo(OffsetDateTime.parse("2026-11-10T15:10:00Z"));
        assertThat(enlace).isEqualTo("https://cuenta.blob.core.windows.net/adjuntos/kudos/a/b?sig=1");
        verify(blob).deleteIfExists();
    }

    @Test
    void laAuditoriaInsertaPorDiaConSuCorrelationId() {
        TableClient tabla = mock(TableClient.class);
        UUID id = UUID.randomUUID();
        UUID actor = UUID.randomUUID();

        new AuditoriaTableAdapter(tabla).registrar(new RegistroAuditoriaDTO(id, RegistroAuditoriaDTO.KUDO_GUARDADO,
                actor, "kudo/1", EVENTO.correlationId(), Dobles.AHORA));
        new AuditoriaTableAdapter(tabla).registrar(new RegistroAuditoriaDTO(UUID.randomUUID(),
                RegistroAuditoriaDTO.EVENTO_PUBLICADO, null, "evento/1", EVENTO.correlationId(), Dobles.AHORA));

        ArgumentCaptor<TableEntity> filas = ArgumentCaptor.forClass(TableEntity.class);
        verify(tabla, org.mockito.Mockito.times(2)).createEntity(filas.capture());
        TableEntity primera = filas.getAllValues().getFirst();
        assertThat(primera.getPartitionKey()).isEqualTo("2026-11-10");
        assertThat(primera.getRowKey()).isEqualTo(id.toString());
        assertThat(primera.getProperty("Accion")).isEqualTo("KUDO_GUARDADO");
        assertThat(primera.getProperty("ActorId")).isEqualTo(actor.toString());
        assertThat(primera.getProperty("CorrelationId")).isEqualTo(EVENTO.correlationId().toString());
        assertThat(filas.getAllValues().get(1).getProperty("ActorId")).isNull();
    }

    @Test
    @SuppressWarnings("unchecked")
    void eventGridRecibeUnCloudEventConElContratoAcordado() {
        EventGridPublisherClient<CloudEvent> cliente = mock(EventGridPublisherClient.class);
        JsonMapper json = JsonMapper.builder().build();

        new EventGridPublicadorAdapter(cliente, "/uco/kudos-service", json).publicar(List.of(EVENTO));

        ArgumentCaptor<Iterable<CloudEvent>> enviados = ArgumentCaptor.forClass(Iterable.class);
        verify(cliente).sendEvents(enviados.capture());
        CloudEvent evento = enviados.getValue().iterator().next();
        assertThat(evento.getId()).isEqualTo(EVENTO.eventoId().toString());
        assertThat(evento.getType()).isEqualTo("uco.kudos.KudoPublicado.v1");
        assertThat(evento.getSource()).isEqualTo("/uco/kudos-service");
        assertThat(evento.getSubject()).isEqualTo("kudos/" + EVENTO.kudoId());
        assertThat(evento.getDataContentType()).isEqualTo("application/json");
        assertThat(json.readValue(evento.getData().toString(), EventoKudoPublicadoDTO.class)).isEqualTo(EVENTO);
    }
}
