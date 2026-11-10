package co.edu.uco.kudos.infraestructura;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import co.edu.uco.kudos.dominio.kudo.TipoDeArchivo;
import co.edu.uco.kudos.infraestructura.input.controladores.IdentidadDesdeToken;
import co.edu.uco.kudos.infraestructura.input.controladores.PropiedadesDeEntrada;
import co.edu.uco.kudos.infraestructura.input.programados.RelevoOutbox;
import co.edu.uco.kudos.infraestructura.output.adapter.RelojUtc;
import co.edu.uco.kudos.infraestructura.output.serviciosexternos.parametercatalog.CatalogoDeParametrosAdapter;
import co.edu.uco.kudos.infraestructura.output.serviciosexternos.parametercatalog.PropiedadesDelCatalogo;
import co.edu.uco.kudos.servicios.input.dto.request.IdentidadRequest;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.Test;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.util.unit.DataSize;

/** Pruebas de adaptadores sin servicios externos: identidad del token, catálogo, reloj y relevo. */
class AdaptadoresSencillosTest {

    private static final PropiedadesDeEntrada ENTRADA = new PropiedadesDeEntrada(
            new PropiedadesDeEntrada.Gateway("X-Kudos-Gateway", "s"),
            new PropiedadesDeEntrada.Claims("oid", "name", "email", "phone_number", "roles"));

    private static Jwt.Builder token() {
        return Jwt.withTokenValue("t").header("alg", "RS256").issuedAt(Instant.now())
                .expiresAt(Instant.now().plusSeconds(60));
    }

    @Test
    void laIdentidadSaleDeLosClaimsConfigurados() {
        UUID id = UUID.randomUUID();
        Jwt jwt = token().claim("oid", id.toString()).claim("name", "Ana").claim("email", "ana@uco.edu.co")
                .claim("phone_number", "+57300").claim("roles", List.of("DOCENTE", "ESTUDIANTE")).build();

        IdentidadRequest identidad = new IdentidadDesdeToken(ENTRADA).leer(jwt);

        assertThat(identidad).isEqualTo(new IdentidadRequest(id, "Ana", "ana@uco.edu.co", "+57300", "DOCENTE"));
    }

    @Test
    void sinRolEnElTokenSeNiegaElAcceso() {
        Jwt jwt = token().claim("oid", UUID.randomUUID().toString()).claim("name", "Ana").build();

        assertThatThrownBy(() -> new IdentidadDesdeToken(ENTRADA).leer(jwt)).isInstanceOf(AccessDeniedException.class);
    }

    @Test
    void elCatalogoTraduceLosParametrosDelEntorno() {
        CatalogoDeParametrosAdapter catalogo = new CatalogoDeParametrosAdapter(new PropiedadesDelCatalogo(
                DataSize.ofKilobytes(512), Set.of("png", " pdf "), Duration.ofMinutes(10), Duration.ofSeconds(30), 10,
                50, 20));

        assertThat(catalogo.tamanoMaximoAdjunto()).isEqualTo(512 * 1024);
        assertThat(catalogo.tiposDeAdjuntoPermitidos()).containsExactlyInAnyOrder(TipoDeArchivo.PNG, TipoDeArchivo.PDF);
        assertThat(catalogo.vigenciaEnlaceAdjunto()).isEqualTo(Duration.ofMinutes(10));
        assertThat(catalogo.ttlDelFeed()).isEqualTo(Duration.ofSeconds(30));
        assertThat(catalogo.tamanoDePagina()).isEqualTo(10);
        assertThat(catalogo.tamanoMaximoDePagina()).isEqualTo(50);
        assertThat(catalogo.loteDelRelevo()).isEqualTo(20);
    }

    @Test
    void elRelojDaUtcEnMilisegundos() {
        Instant ahora = new RelojUtc().ahora();

        assertThat(ahora).isEqualTo(ahora.truncatedTo(ChronoUnit.MILLIS));
    }

    @Test
    void elRelevoNoSeCaeSiFallaUnaVuelta() {
        AtomicInteger vueltas = new AtomicInteger();
        RelevoOutbox relevo = new RelevoOutbox(() -> {
            if (vueltas.incrementAndGet() == 1) {
                throw new IllegalStateException("Event Grid caído");
            }
            return 3;
        });

        relevo.relevar();
        relevo.relevar();

        assertThat(vueltas).hasValue(2);
    }
}
