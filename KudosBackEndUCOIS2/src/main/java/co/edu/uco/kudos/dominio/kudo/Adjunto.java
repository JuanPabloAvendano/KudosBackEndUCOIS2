package co.edu.uco.kudos.dominio.kudo;

import co.edu.uco.kudos.dominio.reglas.CodigoRegla;
import co.edu.uco.kudos.dominio.reglas.ReglaIncumplida;
import java.util.Objects;
import java.util.Set;

/**
 * Entidad del agregado Kudo. El archivo se nombra en el servidor ({@code claveArchivo}); el nombre original del
 * cliente no se guarda.
 */
public record Adjunto(AdjuntoId id, String claveArchivo, TipoDeArchivo tipo, long tamano) {

    public Adjunto {
        Objects.requireNonNull(id, "El id es obligatorio");
        Objects.requireNonNull(tipo, "El tipo es obligatorio");
        if (claveArchivo == null || claveArchivo.isBlank()) {
            throw new IllegalArgumentException("La clave del archivo es obligatoria");
        }
    }

    /**
     * Crea el adjunto de un Kudo nuevo validando tamaño y tipo real.
     *
     * @param cabecera primeros bytes del archivo, para reconocer su tipo
     */
    public static Adjunto nuevo(KudoId kudo, AdjuntoId id, byte[] cabecera, long tamano, long tamanoMaximo,
            Set<TipoDeArchivo> permitidos) {
        ReglaIncumplida.exigir(tamano > 0, CodigoRegla.ADJUNTO_VACIO, "El adjunto está vacío");
        ReglaIncumplida.exigir(tamano <= tamanoMaximo, CodigoRegla.ADJUNTO_MUY_GRANDE,
                "El adjunto supera el tamaño máximo de " + tamanoMaximo + " bytes");
        TipoDeArchivo tipo = TipoDeArchivo.detectar(cabecera)
                .filter(permitidos::contains)
                .orElseThrow(() -> new ReglaIncumplida(CodigoRegla.TIPO_DE_ADJUNTO_NO_PERMITIDO,
                        "El contenido del adjunto no es de un tipo permitido"));
        String clave = "kudos/" + kudo.valor() + "/" + id.valor();
        return new Adjunto(id, clave, tipo, tamano);
    }
}
