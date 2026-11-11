package co.edu.uco.kudos.infraestructura.output;

import co.edu.uco.kudos.infraestructura.output.mensajeria.productores.EventGridPublicadorAdapter;
import co.edu.uco.kudos.infraestructura.output.repositorios.noestructurados.AlmacenAdjuntosBlobAdapter;
import co.edu.uco.kudos.infraestructura.output.repositorios.semiestructurados.AuditoriaTableAdapter;
import co.edu.uco.kudos.infraestructura.output.serviciosexternos.push.WebPubSubConexionesAdapter;
import co.edu.uco.kudos.servicios.output.ports.repositorios.AlmacenAdjuntos;
import co.edu.uco.kudos.servicios.output.ports.repositorios.Auditoria;
import co.edu.uco.kudos.servicios.output.ports.serviciosexternos.ConexionesPush;
import co.edu.uco.kudos.servicios.output.ports.serviciosexternos.PublicadorDeEventos;
import co.edu.uco.kudos.servicios.output.ports.serviciosexternos.Reloj;
import com.azure.core.credential.TokenCredential;
import com.azure.data.tables.TableClientBuilder;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.messaging.eventgrid.EventGridPublisherClientBuilder;
import com.azure.messaging.webpubsub.WebPubSubServiceClientBuilder;
import com.azure.storage.blob.BlobServiceClientBuilder;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import tools.jackson.databind.json.JsonMapper;

/**
 * Clientes de Azure con la identidad administrada del servicio (DefaultAzureCredential): sin cadenas de conexión ni
 * llaves (ADR-013). Los clientes no se conectan al crearse, solo en la primera llamada.
 */
@Configuration(proxyBeanMethods = false)
public class ClientesAzure {

    /** Endpoints y nombres de los recursos de Azure (los pone Juan en el portal, ADR-003). */
    @ConfigurationProperties("kudos.azure")
    public record PropiedadesAzure(String blobEndpoint, String contenedorAdjuntos, String tablaEndpoint,
            String tablaAuditoria, String eventGridEndpoint, String eventGridFuente, String webPubSubEndpoint,
            String webPubSubHub) {
    }

    @Bean
    TokenCredential credencialAzure() {
        return new DefaultAzureCredentialBuilder().build();
    }

    @Bean
    AlmacenAdjuntos almacenAdjuntos(PropiedadesAzure azure, TokenCredential credencial, Reloj reloj) {
        return new AlmacenAdjuntosBlobAdapter(new BlobServiceClientBuilder().endpoint(azure.blobEndpoint())
                .credential(credencial).buildClient(), azure.contenedorAdjuntos(), reloj);
    }

    @Bean
    Auditoria auditoria(PropiedadesAzure azure, TokenCredential credencial) {
        return new AuditoriaTableAdapter(new TableClientBuilder().endpoint(azure.tablaEndpoint())
                .tableName(azure.tablaAuditoria()).credential(credencial).buildClient());
    }

    @Bean
    PublicadorDeEventos publicadorDeEventos(PropiedadesAzure azure, TokenCredential credencial, JsonMapper json) {
        return new EventGridPublicadorAdapter(new EventGridPublisherClientBuilder()
                .endpoint(azure.eventGridEndpoint()).credential(credencial).buildCloudEventPublisherClient(),
                azure.eventGridFuente(), json);
    }

    @Bean
    ConexionesPush conexionesPush(PropiedadesAzure azure, TokenCredential credencial) {
        return new WebPubSubConexionesAdapter(new WebPubSubServiceClientBuilder().endpoint(azure.webPubSubEndpoint())
                .hub(azure.webPubSubHub()).credential(credencial).buildClient());
    }
}
