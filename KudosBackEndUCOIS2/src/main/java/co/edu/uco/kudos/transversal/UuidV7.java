package co.edu.uco.kudos.transversal;

import java.security.SecureRandom;
import java.time.Instant;
import java.util.UUID;

/** Genera UUID versión 7 (RFC 9562): los primeros 48 bits son milisegundos, así los ids quedan ordenados en el tiempo. */
public final class UuidV7 {

    private static final SecureRandom ALEATORIO = new SecureRandom();

    private UuidV7() {
    }

    public static UUID generar(Instant instante) {
        long milisegundos = instante.toEpochMilli();
        long altos = (milisegundos << 16) | 0x7000L | (ALEATORIO.nextInt() & 0x0FFFL);
        long bajos = (ALEATORIO.nextLong() & 0x3FFFFFFFFFFFFFFFL) | 0x8000000000000000L;
        return new UUID(altos, bajos);
    }
}
