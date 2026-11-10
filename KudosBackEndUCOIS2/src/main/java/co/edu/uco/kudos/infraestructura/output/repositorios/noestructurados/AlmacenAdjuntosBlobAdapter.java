package co.edu.uco.kudos.infraestructura.output.repositorios.noestructurados;

import co.edu.uco.kudos.servicios.output.ports.repositorios.AlmacenAdjuntos;
import co.edu.uco.kudos.servicios.output.ports.serviciosexternos.Reloj;
import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.BlobServiceClient;
import com.azure.storage.blob.models.BlobHttpHeaders;
import com.azure.storage.blob.models.UserDelegationKey;
import com.azure.storage.blob.options.BlobParallelUploadOptions;
import com.azure.storage.blob.sas.BlobSasPermission;
import com.azure.storage.blob.sas.BlobServiceSasSignatureValues;
import java.io.InputStream;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

/**
 * Adjuntos en Azure Blob Storage con la identidad administrada del servicio (sin cadenas de conexión, ADR-013). El
 * enlace es un SAS de delegación de usuario, de solo lectura y corto (5-15 min, QA-10).
 */
public class AlmacenAdjuntosBlobAdapter implements AlmacenAdjuntos {

    private final BlobServiceClient servicio;
    private final BlobContainerClient contenedor;
    private final Reloj reloj;

    public AlmacenAdjuntosBlobAdapter(BlobServiceClient servicio, String contenedor, Reloj reloj) {
        this.servicio = servicio;
        this.contenedor = servicio.getBlobContainerClient(contenedor);
        this.reloj = reloj;
    }

    @Override
    public void guardar(String clave, String tipoMime, long tamano, InputStream contenido) {
        BlobParallelUploadOptions opciones = new BlobParallelUploadOptions(contenido)
                .setHeaders(new BlobHttpHeaders().setContentType(tipoMime));
        contenedor.getBlobClient(clave).uploadWithResponse(opciones, null, null);
    }

    @Override
    public String enlaceTemporal(String clave, Duration vigencia) {
        OffsetDateTime inicio = reloj.ahora().atOffset(ZoneOffset.UTC);
        OffsetDateTime vence = inicio.plus(vigencia);
        UserDelegationKey llave = servicio.getUserDelegationKey(inicio, vence);
        BlobClient blob = contenedor.getBlobClient(clave);
        BlobServiceSasSignatureValues valores = new BlobServiceSasSignatureValues(vence,
                new BlobSasPermission().setReadPermission(true));
        return blob.getBlobUrl() + "?" + blob.generateUserDelegationSas(valores, llave);
    }

    @Override
    public void eliminar(String clave) {
        contenedor.getBlobClient(clave).deleteIfExists();
    }
}
