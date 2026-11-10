package co.edu.uco.kudos.servicios.casosuso.impl;

import static org.assertj.core.api.Assertions.assertThat;

import co.edu.uco.kudos.dobles.Dobles;
import co.edu.uco.kudos.dominio.categoria.CategoriaId;
import co.edu.uco.kudos.dominio.kudo.Adjunto;
import co.edu.uco.kudos.dominio.kudo.AdjuntoId;
import co.edu.uco.kudos.dominio.kudo.CursorDelFeed;
import co.edu.uco.kudos.dominio.kudo.Kudo;
import co.edu.uco.kudos.dominio.kudo.KudoId;
import co.edu.uco.kudos.dominio.kudo.TipoDeArchivo;
import co.edu.uco.kudos.dominio.usuario.UsuarioId;
import co.edu.uco.kudos.servicios.casosuso.KudoConEnlaces;
import co.edu.uco.kudos.transversal.Pagina;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/** Pruebas del feed: orden, cursor, caché, caída de la caché, límite de tamaño y enlaces. */
class ConsultarFeedCasoUsoImplTest {

    private Dobles.Kudos kudos;
    private Dobles.Cache cache;
    private Dobles.Parametros parametros;
    private ConsultarFeedCasoUsoImpl casoUso;

    @BeforeEach
    void preparar() {
        kudos = new Dobles.Kudos();
        cache = new Dobles.Cache();
        parametros = new Dobles.Parametros();
        casoUso = new ConsultarFeedCasoUsoImpl(kudos, cache, new Dobles.Almacen(), parametros);
        for (int i = 1; i <= 3; i++) {
            kudos.guardar(kudo(i, List.of()));
        }
    }

    private static Kudo kudo(int minuto, List<Adjunto> adjuntos) {
        return Kudo.reconstruir(new KudoId(new UUID(0, minuto)), new UsuarioId(new UUID(1, 1)),
                new UsuarioId(new UUID(2, 2)), new CategoriaId(1), "Kudo " + minuto,
                Dobles.AHORA.plusSeconds(60L * minuto), adjuntos);
    }

    @Test
    void laPrimeraPaginaVaDelMasRecienteAlMasAntiguoYTraeCursor() {
        Pagina<KudoConEnlaces> pagina = casoUso.ejecutar(null, null);

        assertThat(pagina.elementos()).extracting(k -> k.kudo().contenido()).containsExactly("Kudo 3", "Kudo 2");
        assertThat(pagina.siguiente()).isPresent();
    }

    @Test
    void conElCursorTraeLaSiguientePaginaYAlFinalNoHayMas() {
        Pagina<KudoConEnlaces> primera = casoUso.ejecutar(null, null);

        Pagina<KudoConEnlaces> segunda = casoUso.ejecutar(
                CursorDelFeed.decodificar(primera.siguienteCursor()), null);

        assertThat(segunda.elementos()).extracting(k -> k.kudo().contenido()).containsExactly("Kudo 1");
        assertThat(segunda.siguiente()).isEmpty();
    }

    @Test
    void laSegundaLecturaDeLaPrimeraPaginaSaleDeLaCache() {
        casoUso.ejecutar(null, null);
        casoUso.ejecutar(null, null);

        assertThat(kudos.consultas).isEqualTo(1);
        assertThat(cache.paginas).containsKey(2);
    }

    @Test
    void siLaCacheFallaLeeDeLaBaseDeDatos() {
        cache.caida = true;

        assertThat(casoUso.ejecutar(null, null).elementos()).hasSize(2);
        assertThat(kudos.consultas).isEqualTo(1);
    }

    @Test
    void limitaElTamanoPedidoAlMaximoDelCatalogo() {
        parametros.tamanoMaximoDePagina = 2;

        assertThat(casoUso.ejecutar(null, 50).elementos()).hasSize(2);
        assertThat(casoUso.ejecutar(null, 0).elementos()).hasSize(1);
    }

    @Test
    void cadaAdjuntoLlevaSuEnlaceTemporal() {
        Adjunto adjunto = new Adjunto(new AdjuntoId(new UUID(9, 9)), "kudos/a/b", TipoDeArchivo.PNG, 5);
        kudos.guardar(kudo(10, List.of(adjunto)));

        KudoConEnlaces primero = casoUso.ejecutar(null, 1).elementos().getFirst();

        assertThat(primero.enlaces()).containsEntry(adjunto.id(), "https://almacen.invalid/kudos/a/b?vence=10");
    }
}
