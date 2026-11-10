package co.edu.uco.kudos.dominio.kudo;

import co.edu.uco.kudos.dominio.reglas.CodigoRegla;
import co.edu.uco.kudos.dominio.reglas.ReglaIncumplida;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;
import java.util.Objects;
import java.util.UUID;

/**
 * Posición en el feed para la paginación por keyset (fecha DESC, id DESC): la página siguiente empieza después del
 * último Kudo visto. Viaja al cliente como texto opaco.
 */
public record CursorDelFeed(Instant fecha, KudoId id) {

    private static final String SEPARADOR = "|";

    public CursorDelFeed {
        Objects.requireNonNull(fecha, "La fecha es obligatoria");
        Objects.requireNonNull(id, "El id es obligatorio");
    }

    public static CursorDelFeed despuesDe(Kudo kudo) {
        return new CursorDelFeed(kudo.fecha(), kudo.id());
    }

    public String codificar() {
        String texto = fecha.toEpochMilli() + SEPARADOR + id.valor();
        return Base64.getUrlEncoder().withoutPadding().encodeToString(texto.getBytes(StandardCharsets.UTF_8));
    }

    public static CursorDelFeed decodificar(String codificado) {
        try {
            String texto = new String(Base64.getUrlDecoder().decode(codificado), StandardCharsets.UTF_8);
            int corte = texto.indexOf(SEPARADOR);
            Instant fecha = Instant.ofEpochMilli(Long.parseLong(texto.substring(0, corte)));
            return new CursorDelFeed(fecha, new KudoId(UUID.fromString(texto.substring(corte + 1))));
        } catch (RuntimeException e) {
            throw new ReglaIncumplida(CodigoRegla.CURSOR_INVALIDO, "El cursor del feed no es válido");
        }
    }
}
