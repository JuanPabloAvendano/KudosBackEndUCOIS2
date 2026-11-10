package co.edu.uco.kudos.dominio.kudo;

import co.edu.uco.kudos.dominio.categoria.CategoriaId;
import co.edu.uco.kudos.dominio.reglas.CodigoRegla;
import co.edu.uco.kudos.dominio.reglas.ReglaIncumplida;
import co.edu.uco.kudos.dominio.usuario.UsuarioId;
import java.time.Instant;
import java.util.List;
import java.util.Objects;

/**
 * Raíz del agregado Kudo: un reconocimiento de un miembro a otro. Referencia a Usuario y Categoría solo por id
 * (otros agregados, otros componentes).
 */
public final class Kudo {

    private final KudoId id;
    private final UsuarioId autor;
    private final UsuarioId destinatario;
    private final CategoriaId categoria;
    private final String contenido;
    private final Instant fecha;
    private final List<Adjunto> adjuntos;

    private Kudo(KudoId id, UsuarioId autor, UsuarioId destinatario, CategoriaId categoria, String contenido,
            Instant fecha, List<Adjunto> adjuntos) {
        this.id = Objects.requireNonNull(id, "El id es obligatorio");
        this.autor = Objects.requireNonNull(autor, "El autor es obligatorio");
        this.destinatario = Objects.requireNonNull(destinatario, "El destinatario es obligatorio");
        this.categoria = Objects.requireNonNull(categoria, "La categoría es obligatoria");
        this.contenido = Objects.requireNonNull(contenido, "El contenido es obligatorio");
        this.fecha = Objects.requireNonNull(fecha, "La fecha es obligatoria");
        this.adjuntos = List.copyOf(adjuntos);
    }

    /** Publica un Kudo nuevo aplicando las reglas del negocio. */
    public static Kudo publicar(KudoId id, UsuarioId autor, UsuarioId destinatario, CategoriaId categoria,
            String contenido, Instant fecha, List<Adjunto> adjuntos) {
        ReglaIncumplida.exigir(contenido != null && !contenido.isBlank(), CodigoRegla.CONTENIDO_VACIO,
                "El Kudo necesita un mensaje");
        ReglaIncumplida.exigir(!Objects.equals(autor, destinatario), CodigoRegla.AUTORRECONOCIMIENTO,
                "Nadie puede darse un Kudo a sí mismo");
        return new Kudo(id, autor, destinatario, categoria, contenido.strip(), fecha, adjuntos);
    }

    /** Reconstruye un Kudo ya guardado (sin volver a validar reglas de publicación). */
    public static Kudo reconstruir(KudoId id, UsuarioId autor, UsuarioId destinatario, CategoriaId categoria,
            String contenido, Instant fecha, List<Adjunto> adjuntos) {
        return new Kudo(id, autor, destinatario, categoria, contenido, fecha, adjuntos);
    }

    public KudoId id() {
        return id;
    }

    public UsuarioId autor() {
        return autor;
    }

    public UsuarioId destinatario() {
        return destinatario;
    }

    public CategoriaId categoria() {
        return categoria;
    }

    public String contenido() {
        return contenido;
    }

    public Instant fecha() {
        return fecha;
    }

    public List<Adjunto> adjuntos() {
        return adjuntos;
    }
}
