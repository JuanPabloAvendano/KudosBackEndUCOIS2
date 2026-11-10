package co.edu.uco.kudos.servicios.casosuso.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import co.edu.uco.kudos.dobles.Dobles;
import co.edu.uco.kudos.servicios.output.dto.DestinatarioDTO;
import co.edu.uco.kudos.servicios.output.dto.EventoKudoPublicadoDTO;
import co.edu.uco.kudos.servicios.output.dto.RegistroAuditoriaDTO;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/** Pruebas del relevo del outbox: publica el lote, lo marca y no marca nada si el broker falla. */
class PublicarEventosPendientesCasoUsoImplTest {

    private Dobles.Outbox outbox;
    private Dobles.Publicador publicador;
    private Dobles.RegistroDeAuditoria auditoria;
    private Dobles.Parametros parametros;
    private PublicarEventosPendientesCasoUsoImpl casoUso;

    @BeforeEach
    void preparar() {
        outbox = new Dobles.Outbox();
        publicador = new Dobles.Publicador();
        auditoria = new Dobles.RegistroDeAuditoria();
        parametros = new Dobles.Parametros();
        Dobles.RelojFijo reloj = new Dobles.RelojFijo();
        Dobles.IdsSecuenciales ids = new Dobles.IdsSecuenciales();
        casoUso = new PublicarEventosPendientesCasoUsoImpl(outbox, publicador, new Dobles.UnidadInmediata(),
                parametros, reloj, new EfectosPosteriores(auditoria, new Dobles.Cache(), ids, reloj));
        for (int i = 0; i < 3; i++) {
            outbox.agregar(evento());
        }
    }

    private static EventoKudoPublicadoDTO evento() {
        return new EventoKudoPublicadoDTO(UUID.randomUUID(), UUID.randomUUID(), "Ana",
                new DestinatarioDTO(UUID.randomUUID(), "Luis", "luis@uco.edu.co", null), "Liderazgo", Dobles.AHORA,
                UUID.randomUUID());
    }

    @Test
    void publicaElLoteLoMarcaEnviadoYAuditaCadaEvento() {
        parametros.loteDelRelevo = 2;

        assertThat(casoUso.ejecutar()).isEqualTo(2);
        assertThat(publicador.publicados).hasSize(2);
        assertThat(outbox.enviados).hasSize(2);
        assertThat(auditoria.registros).extracting(RegistroAuditoriaDTO::accion)
                .containsOnly(RegistroAuditoriaDTO.EVENTO_PUBLICADO);
        assertThat(casoUso.ejecutar()).isEqualTo(1);
        assertThat(casoUso.ejecutar()).isZero();
    }

    @Test
    void siElBrokerFallaNadaQuedaMarcadoComoEnviado() {
        publicador.caido = true;

        assertThatThrownBy(casoUso::ejecutar).isInstanceOf(IllegalStateException.class);
        assertThat(outbox.enviados).isEmpty();
        assertThat(auditoria.registros).isEmpty();
    }
}
