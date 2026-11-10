package co.edu.uco.kudos.dobles;

import co.edu.uco.kudos.dominio.categoria.Categoria;
import co.edu.uco.kudos.dominio.categoria.CategoriaId;
import co.edu.uco.kudos.dominio.kudo.CursorDelFeed;
import co.edu.uco.kudos.dominio.kudo.Kudo;
import co.edu.uco.kudos.dominio.kudo.TipoDeArchivo;
import co.edu.uco.kudos.dominio.usuario.Rol;
import co.edu.uco.kudos.dominio.usuario.Usuario;
import co.edu.uco.kudos.dominio.usuario.UsuarioId;
import co.edu.uco.kudos.servicios.output.dto.EventoKudoPublicadoDTO;
import co.edu.uco.kudos.servicios.output.dto.RegistroAuditoriaDTO;
import co.edu.uco.kudos.servicios.output.ports.repositorios.AlmacenAdjuntos;
import co.edu.uco.kudos.servicios.output.ports.repositorios.Auditoria;
import co.edu.uco.kudos.servicios.output.ports.repositorios.CacheDelFeed;
import co.edu.uco.kudos.servicios.output.ports.repositorios.entidades.CategoriaRepositorio;
import co.edu.uco.kudos.servicios.output.ports.repositorios.GeneradorDeIds;
import co.edu.uco.kudos.servicios.output.ports.repositorios.entidades.KudoRepositorio;
import co.edu.uco.kudos.servicios.output.ports.repositorios.entidades.OutboxRepositorio;
import co.edu.uco.kudos.servicios.output.ports.repositorios.UnidadDeTrabajo;
import co.edu.uco.kudos.servicios.output.ports.repositorios.entidades.UsuarioRepositorio;
import co.edu.uco.kudos.servicios.output.ports.serviciosexternos.CatalogoDeParametros;
import co.edu.uco.kudos.servicios.output.ports.serviciosexternos.PublicadorDeEventos;
import co.edu.uco.kudos.servicios.output.ports.serviciosexternos.Reloj;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Supplier;

/** Dobles en memoria de los puertos de salida, para probar los casos de uso sin servicios reales (QA-16). */
public final class Dobles {

    public static final Instant AHORA = Instant.parse("2026-11-10T15:00:00Z");
    public static final byte[] PNG = {(byte) 0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A, 1, 2, 3, 4};

    private Dobles() {
    }

    public static Usuario usuario(String nombre) {
        return Usuario.desdeIdentidad(new UsuarioId(UUID.nameUUIDFromBytes(nombre.getBytes())), nombre,
                nombre.toLowerCase() + "@uco.edu.co", "+573000000000", Rol.ESTUDIANTE);
    }

    public static class Usuarios implements UsuarioRepositorio {
        public final Map<UsuarioId, Usuario> filas = new HashMap<>();
        public int guardados;

        public Usuarios con(Usuario... usuarios) {
            for (Usuario u : usuarios) {
                filas.put(u.id(), u);
            }
            return this;
        }

        @Override
        public Optional<Usuario> porId(UsuarioId id) {
            return Optional.ofNullable(filas.get(id));
        }

        @Override
        public void guardar(Usuario usuario) {
            guardados++;
            filas.put(usuario.id(), usuario);
        }
    }

    public static class Categorias implements CategoriaRepositorio {
        public final Map<CategoriaId, Categoria> filas = new LinkedHashMap<>();

        public Categorias con(Categoria... categorias) {
            for (Categoria c : categorias) {
                filas.put(c.id(), c);
            }
            return this;
        }

        @Override
        public Optional<Categoria> porId(CategoriaId id) {
            return Optional.ofNullable(filas.get(id));
        }

        @Override
        public List<Categoria> activas() {
            return filas.values().stream().filter(Categoria::estaActiva).toList();
        }
    }

    public static class Kudos implements KudoRepositorio {
        public final List<Kudo> filas = new ArrayList<>();
        public int consultas;

        @Override
        public void guardar(Kudo kudo) {
            filas.add(kudo);
        }

        @Override
        public List<Kudo> pagina(CursorDelFeed despuesDe, int limite) {
            consultas++;
            Comparator<Kudo> orden = Comparator.comparing(Kudo::fecha).thenComparing(k -> k.id().valor());
            return filas.stream()
                    .sorted(orden.reversed())
                    .filter(k -> despuesDe == null || orden.compare(k, referencia(despuesDe)) < 0)
                    .limit(limite)
                    .toList();
        }

        private Kudo referencia(CursorDelFeed cursor) {
            return filas.stream().filter(k -> k.id().equals(cursor.id())).findFirst().orElseThrow();
        }
    }

    public static class Outbox implements OutboxRepositorio {
        public final List<EventoKudoPublicadoDTO> pendientes = new ArrayList<>();
        public final List<UUID> enviados = new ArrayList<>();

        @Override
        public void agregar(EventoKudoPublicadoDTO evento) {
            pendientes.add(evento);
        }

        @Override
        public List<EventoKudoPublicadoDTO> tomarPendientes(int lote) {
            return pendientes.stream().filter(e -> !enviados.contains(e.eventoId())).limit(lote).toList();
        }

        @Override
        public void marcarEnviados(List<UUID> eventoIds, Instant enviadoEn) {
            enviados.addAll(eventoIds);
        }
    }

    public static class Almacen implements AlmacenAdjuntos {
        public final Map<String, byte[]> archivos = new HashMap<>();
        public final List<String> eliminados = new ArrayList<>();

        @Override
        public void guardar(String clave, String tipoMime, long tamano, InputStream contenido) {
            try {
                archivos.put(clave, contenido.readAllBytes());
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        }

        @Override
        public String enlaceTemporal(String clave, Duration vigencia) {
            return "https://almacen.invalid/" + clave + "?vence=" + vigencia.toMinutes();
        }

        @Override
        public void eliminar(String clave) {
            eliminados.add(clave);
            archivos.remove(clave);
        }
    }

    public static class Cache implements CacheDelFeed {
        public final Map<Integer, List<Kudo>> paginas = new HashMap<>();
        public int invalidaciones;
        public boolean caida;

        @Override
        public Optional<List<Kudo>> leer(int tamano) {
            revisar();
            return Optional.ofNullable(paginas.get(tamano));
        }

        @Override
        public void guardar(int tamano, List<Kudo> kudos, Duration ttl) {
            revisar();
            paginas.put(tamano, kudos);
        }

        @Override
        public void invalidar() {
            revisar();
            invalidaciones++;
            paginas.clear();
        }

        private void revisar() {
            if (caida) {
                throw new IllegalStateException("Redis no responde");
            }
        }
    }

    public static class RegistroDeAuditoria implements Auditoria {
        public final List<RegistroAuditoriaDTO> registros = new ArrayList<>();
        public boolean caida;

        @Override
        public void registrar(RegistroAuditoriaDTO registro) {
            if (caida) {
                throw new IllegalStateException("Table Storage no responde");
            }
            registros.add(registro);
        }
    }

    public static class Publicador implements PublicadorDeEventos {
        public final List<EventoKudoPublicadoDTO> publicados = new ArrayList<>();
        public boolean caido;

        @Override
        public void publicar(List<EventoKudoPublicadoDTO> eventos) {
            if (caido) {
                throw new IllegalStateException("Event Grid no responde");
            }
            publicados.addAll(eventos);
        }
    }

    /** Ejecuta el trabajo en el acto; si {@code fallarAlConfirmar}, lanza después de ejecutarlo (como un commit fallido). */
    public static class UnidadInmediata implements UnidadDeTrabajo {
        public int transacciones;
        public boolean fallarAlConfirmar;

        @Override
        public <T> T ejecutar(Supplier<T> trabajo) {
            transacciones++;
            T resultado = trabajo.get();
            if (fallarAlConfirmar) {
                throw new IllegalStateException("La base de datos rechazó el commit");
            }
            return resultado;
        }
    }

    public static class RelojFijo implements Reloj {
        public Instant ahora = AHORA;

        @Override
        public Instant ahora() {
            return ahora;
        }
    }

    public static class IdsSecuenciales implements GeneradorDeIds {
        private final AtomicLong siguiente = new AtomicLong(1);

        @Override
        public UUID nuevo() {
            return new UUID(0x0190_0000_0000_7000L, siguiente.getAndIncrement());
        }
    }

    public static class Parametros implements CatalogoDeParametros {
        public long tamanoMaximoAdjunto = 1024;
        public Set<TipoDeArchivo> tipos = EnumSet.of(TipoDeArchivo.PNG, TipoDeArchivo.JPEG, TipoDeArchivo.PDF);
        public int tamanoDePagina = 2;
        public int tamanoMaximoDePagina = 5;
        public int loteDelRelevo = 10;

        @Override
        public long tamanoMaximoAdjunto() {
            return tamanoMaximoAdjunto;
        }

        @Override
        public Set<TipoDeArchivo> tiposDeAdjuntoPermitidos() {
            return tipos;
        }

        @Override
        public Duration vigenciaEnlaceAdjunto() {
            return Duration.ofMinutes(10);
        }

        @Override
        public Duration ttlDelFeed() {
            return Duration.ofSeconds(30);
        }

        @Override
        public int tamanoDePagina() {
            return tamanoDePagina;
        }

        @Override
        public int tamanoMaximoDePagina() {
            return tamanoMaximoDePagina;
        }

        @Override
        public int loteDelRelevo() {
            return loteDelRelevo;
        }
    }
}
