package co.edu.uco.kudos.infraestructura.output.serviciosexternos.push;

import co.edu.uco.kudos.dominio.usuario.UsuarioId;
import co.edu.uco.kudos.servicios.output.ports.serviciosexternos.ConexionesPush;
import com.azure.messaging.webpubsub.WebPubSubServiceClient;
import com.azure.messaging.webpubsub.models.GetClientAccessTokenOptions;

/**
 * Azure Web PubSub: firma una URL de cliente para el hub con el userId del usuario. La Logic App envía a
 * {@code /users/{id}}, así que solo ese navegador recibe el aviso.
 */
public class WebPubSubConexionesAdapter implements ConexionesPush {

    private final WebPubSubServiceClient cliente;

    public WebPubSubConexionesAdapter(WebPubSubServiceClient cliente) {
        this.cliente = cliente;
    }

    @Override
    public String urlDeConexion(UsuarioId usuario) {
        return cliente.getClientAccessToken(new GetClientAccessTokenOptions().setUserId(usuario.valor().toString()))
                .getUrl();
    }
}
