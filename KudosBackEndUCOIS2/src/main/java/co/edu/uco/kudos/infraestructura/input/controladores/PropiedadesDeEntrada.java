package co.edu.uco.kudos.infraestructura.input.controladores;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Configuración de la entrada HTTP (ADR-003 y ADR-013): la cabecera secreta que agrega API Management y los nombres
 * de los claims del token de Entra External ID. Los valores llegan del entorno; ninguno vive en el código.
 */
@ConfigurationProperties("kudos.entrada")
public record PropiedadesDeEntrada(Gateway gateway, Claims claims) {

    /** Cabecera secreta del gateway; sin ella el servicio responde 403. */
    public record Gateway(String cabecera, String secreto) {
    }

    /** Nombres de los claims del JWT. */
    public record Claims(String id, String nombre, String correo, String telefono, String roles) {
    }
}
