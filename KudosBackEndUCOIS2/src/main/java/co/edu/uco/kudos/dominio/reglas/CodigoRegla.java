package co.edu.uco.kudos.dominio.reglas;

/** Reglas del negocio de Publicar Kudo; cada código llega al cliente en la respuesta 422. */
public enum CodigoRegla {
    CONTENIDO_VACIO,
    AUTORRECONOCIMIENTO,
    DESTINATARIO_NO_EXISTE,
    CATEGORIA_NO_EXISTE,
    CATEGORIA_INACTIVA,
    ADJUNTO_VACIO,
    ADJUNTO_MUY_GRANDE,
    TIPO_DE_ADJUNTO_NO_PERMITIDO,
    CURSOR_INVALIDO
}
