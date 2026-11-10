package co.edu.uco.kudos.dominio.kudo;

import static org.assertj.core.api.Assertions.assertThat;

import co.edu.uco.kudos.dominio.categoria.CategoriaId;
import co.edu.uco.kudos.dominio.usuario.Rol;
import co.edu.uco.kudos.dominio.usuario.Usuario;
import co.edu.uco.kudos.dominio.usuario.UsuarioId;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;

/** Pruebas del evento KudoPublicado: lleva los datos de contacto del destinatario. */
class KudoPublicadoTest {

    @Test
    void llevaLosDatosDeContactoDelDestinatario() {
        Usuario luis = Usuario.desdeIdentidad(new UsuarioId(UUID.randomUUID()), "Luis", "luis@uco.edu.co", null,
                Rol.DOCENTE);
        Kudo kudo = Kudo.publicar(new KudoId(UUID.randomUUID()), new UsuarioId(UUID.randomUUID()), luis.id(),
                new CategoriaId(1), "Gracias", Instant.parse("2026-11-10T15:00:00Z"), List.of());
        UUID evento = UUID.randomUUID();
        UUID correlacion = UUID.randomUUID();

        KudoPublicado publicado = KudoPublicado.de(evento, kudo, "Ana", Destinatario.de(luis), "Liderazgo", correlacion);

        assertThat(publicado.eventoId()).isEqualTo(evento);
        assertThat(publicado.kudoId()).isEqualTo(kudo.id());
        assertThat(publicado.publicadoEn()).isEqualTo(kudo.fecha());
        assertThat(publicado.destinatario().correo()).isEqualTo("luis@uco.edu.co");
        assertThat(publicado.destinatario().telefonoOpcional()).isEmpty();
        assertThat(publicado.correlationId()).isEqualTo(correlacion);
    }
}
