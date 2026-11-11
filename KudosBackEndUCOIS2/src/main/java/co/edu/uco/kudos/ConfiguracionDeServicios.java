package co.edu.uco.kudos;

import co.edu.uco.kudos.servicios.casosuso.ConsultarCategoriasCasoUso;
import co.edu.uco.kudos.servicios.casosuso.ConsultarFeedCasoUso;
import co.edu.uco.kudos.servicios.casosuso.ConsultarUsuariosCasoUso;
import co.edu.uco.kudos.servicios.casosuso.ObtenerConexionPushCasoUso;
import co.edu.uco.kudos.servicios.casosuso.PublicarEventosPendientesCasoUso;
import co.edu.uco.kudos.servicios.casosuso.PublicarKudoCasoUso;
import co.edu.uco.kudos.servicios.casosuso.RegistrarUsuarioDesdeIdentidadCasoUso;
import co.edu.uco.kudos.servicios.casosuso.impl.ConsultarCategoriasCasoUsoImpl;
import co.edu.uco.kudos.servicios.casosuso.impl.ConsultarFeedCasoUsoImpl;
import co.edu.uco.kudos.servicios.casosuso.impl.ConsultarUsuariosCasoUsoImpl;
import co.edu.uco.kudos.servicios.casosuso.impl.ObtenerConexionPushCasoUsoImpl;
import co.edu.uco.kudos.servicios.casosuso.impl.EfectosPosteriores;
import co.edu.uco.kudos.servicios.casosuso.impl.PreparacionDelKudo;
import co.edu.uco.kudos.servicios.casosuso.impl.PublicarEventosPendientesCasoUsoImpl;
import co.edu.uco.kudos.servicios.casosuso.impl.PublicarKudoCasoUsoImpl;
import co.edu.uco.kudos.servicios.casosuso.impl.RegistrarUsuarioDesdeIdentidadCasoUsoImpl;
import co.edu.uco.kudos.servicios.input.adapter.ConsultarCategoriasInputAdapter;
import co.edu.uco.kudos.servicios.input.adapter.ConsultarFeedInputAdapter;
import co.edu.uco.kudos.servicios.input.adapter.ConsultarUsuariosInputAdapter;
import co.edu.uco.kudos.servicios.input.adapter.ObtenerConexionPushInputAdapter;
import co.edu.uco.kudos.servicios.input.adapter.PublicarEventosPendientesInputAdapter;
import co.edu.uco.kudos.servicios.input.adapter.PublicarKudoInputAdapter;
import co.edu.uco.kudos.servicios.input.ports.ConsultarCategoriasInputPort;
import co.edu.uco.kudos.servicios.input.ports.ConsultarFeedInputPort;
import co.edu.uco.kudos.servicios.input.ports.ConsultarUsuariosInputPort;
import co.edu.uco.kudos.servicios.input.ports.ObtenerConexionPushInputPort;
import co.edu.uco.kudos.servicios.input.ports.PublicarEventosPendientesInputPort;
import co.edu.uco.kudos.servicios.input.ports.PublicarKudoInputPort;
import co.edu.uco.kudos.servicios.output.adapter.EventoKudoPublicadoOutputAdapter;
import co.edu.uco.kudos.servicios.output.adapter.impl.EventoKudoPublicadoOutputAdapterImpl;
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
import co.edu.uco.kudos.servicios.output.ports.serviciosexternos.ConexionesPush;
import co.edu.uco.kudos.servicios.output.ports.serviciosexternos.PublicadorDeEventos;
import co.edu.uco.kudos.servicios.output.ports.serviciosexternos.Reloj;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Arma los beans de servicios (casos de uso y adaptadores de entrada), que no llevan anotaciones de Spring. Es la única
 * clase que conoce a la vez servicios e infraestructura (regla de dependencias, diseño detallado §2).
 */
@Configuration(proxyBeanMethods = false)
@EnableScheduling
public class ConfiguracionDeServicios {

    @Bean
    EventoKudoPublicadoOutputAdapter eventoKudoPublicadoOutputAdapter() {
        return new EventoKudoPublicadoOutputAdapterImpl();
    }

    @Bean
    EfectosPosteriores efectosPosteriores(Auditoria auditoria, CacheDelFeed cache, GeneradorDeIds ids, Reloj reloj) {
        return new EfectosPosteriores(auditoria, cache, ids, reloj);
    }

    @Bean
    PreparacionDelKudo preparacionDelKudo(UsuarioRepositorio usuarios, CategoriaRepositorio categorias,
            GeneradorDeIds ids, Reloj reloj, CatalogoDeParametros parametros, EventoKudoPublicadoOutputAdapter evento) {
        return new PreparacionDelKudo(usuarios, categorias, ids, reloj, parametros, evento);
    }

    @Bean
    RegistrarUsuarioDesdeIdentidadCasoUso registrarUsuarioDesdeIdentidad(UsuarioRepositorio usuarios,
            UnidadDeTrabajo unidadDeTrabajo) {
        return new RegistrarUsuarioDesdeIdentidadCasoUsoImpl(usuarios, unidadDeTrabajo);
    }

    @Bean
    PublicarKudoCasoUso publicarKudoCasoUso(PreparacionDelKudo preparacion, AlmacenAdjuntos almacen,
            KudoRepositorio kudos, OutboxRepositorio outbox, UnidadDeTrabajo unidadDeTrabajo,
            EfectosPosteriores efectos) {
        return new PublicarKudoCasoUsoImpl(preparacion, almacen, kudos, outbox, unidadDeTrabajo, efectos);
    }

    @Bean
    ConsultarFeedCasoUso consultarFeedCasoUso(KudoRepositorio kudos, CacheDelFeed cache, AlmacenAdjuntos almacen,
            CatalogoDeParametros parametros) {
        return new ConsultarFeedCasoUsoImpl(kudos, cache, almacen, parametros);
    }

    @Bean
    PublicarEventosPendientesCasoUso publicarEventosPendientesCasoUso(OutboxRepositorio outbox,
            PublicadorDeEventos publicador, UnidadDeTrabajo unidadDeTrabajo, CatalogoDeParametros parametros,
            Reloj reloj, EfectosPosteriores efectos) {
        return new PublicarEventosPendientesCasoUsoImpl(outbox, publicador, unidadDeTrabajo, parametros, reloj,
                efectos);
    }

    @Bean
    ConsultarCategoriasCasoUso consultarCategoriasCasoUso(CategoriaRepositorio categorias) {
        return new ConsultarCategoriasCasoUsoImpl(categorias);
    }

    @Bean
    ConsultarUsuariosCasoUso consultarUsuariosCasoUso(UsuarioRepositorio usuarios) {
        return new ConsultarUsuariosCasoUsoImpl(usuarios);
    }

    @Bean
    ObtenerConexionPushCasoUso obtenerConexionPushCasoUso(ConexionesPush conexiones) {
        return new ObtenerConexionPushCasoUsoImpl(conexiones);
    }

    @Bean
    PublicarKudoInputPort publicarKudoInputPort(RegistrarUsuarioDesdeIdentidadCasoUso registrar,
            PublicarKudoCasoUso publicar) {
        return new PublicarKudoInputAdapter(registrar, publicar);
    }

    @Bean
    ConsultarFeedInputPort consultarFeedInputPort(RegistrarUsuarioDesdeIdentidadCasoUso registrar,
            ConsultarFeedCasoUso consultar) {
        return new ConsultarFeedInputAdapter(registrar, consultar);
    }

    @Bean
    ConsultarCategoriasInputPort consultarCategoriasInputPort(ConsultarCategoriasCasoUso consultar) {
        return new ConsultarCategoriasInputAdapter(consultar);
    }

    @Bean
    ConsultarUsuariosInputPort consultarUsuariosInputPort(RegistrarUsuarioDesdeIdentidadCasoUso registrar,
            ConsultarUsuariosCasoUso consultar) {
        return new ConsultarUsuariosInputAdapter(registrar, consultar);
    }

    @Bean
    ObtenerConexionPushInputPort obtenerConexionPushInputPort(RegistrarUsuarioDesdeIdentidadCasoUso registrar,
            ObtenerConexionPushCasoUso obtener) {
        return new ObtenerConexionPushInputAdapter(registrar, obtener);
    }

    @Bean
    PublicarEventosPendientesInputPort publicarEventosPendientesInputPort(PublicarEventosPendientesCasoUso caso) {
        return new PublicarEventosPendientesInputAdapter(caso);
    }
}
