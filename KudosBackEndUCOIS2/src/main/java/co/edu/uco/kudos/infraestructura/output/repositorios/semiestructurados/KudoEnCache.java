package co.edu.uco.kudos.infraestructura.output.repositorios.semiestructurados;

import co.edu.uco.kudos.dominio.categoria.CategoriaId;
import co.edu.uco.kudos.dominio.kudo.Adjunto;
import co.edu.uco.kudos.dominio.kudo.AdjuntoId;
import co.edu.uco.kudos.dominio.kudo.Kudo;
import co.edu.uco.kudos.dominio.kudo.KudoId;
import co.edu.uco.kudos.dominio.kudo.TipoDeArchivo;
import co.edu.uco.kudos.dominio.usuario.UsuarioId;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

/** Forma JSON de un Kudo en la caché: tipos simples para no atar el formato guardado al dominio. */
record KudoEnCache(UUID id, UUID autor, UUID destinatario, int categoria, String contenido, Instant fecha,
        List<AdjuntoEnCache> adjuntos) {

    record AdjuntoEnCache(UUID id, String clave, String tipo, long tamano) {
    }

    static KudoEnCache desde(Kudo kudo) {
        return new KudoEnCache(kudo.id().valor(), kudo.autor().valor(), kudo.destinatario().valor(),
                kudo.categoria().valor(), kudo.contenido(), kudo.fecha(),
                kudo.adjuntos().stream()
                        .map(a -> new AdjuntoEnCache(a.id().valor(), a.claveArchivo(), a.tipo().name(), a.tamano()))
                        .toList());
    }

    Kudo aDominio() {
        return Kudo.reconstruir(new KudoId(id), new UsuarioId(autor), new UsuarioId(destinatario),
                new CategoriaId(categoria), contenido, fecha,
                adjuntos.stream()
                        .map(a -> new Adjunto(new AdjuntoId(a.id()), a.clave(), TipoDeArchivo.valueOf(a.tipo()),
                                a.tamano()))
                        .toList());
    }
}
