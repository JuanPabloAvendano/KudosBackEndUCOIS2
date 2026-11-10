package co.edu.uco.kudos.dominio.usuario;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.UUID;
import org.junit.jupiter.api.Test;

/** Pruebas del usuario: datos del token, cambios que obligan a actualizar y roles. */
class UsuarioTest {

    private static final UsuarioId ID = UsuarioId.de("0190a000-0000-7000-8000-0000000000aa");

    @Test
    void seCreaDesdeElTokenYSinTelefonoQuedaVacio() {
        Usuario ana = Usuario.desdeIdentidad(ID, " Ana ", "ana@uco.edu.co", " ", Rol.DOCENTE);

        assertThat(ana.nombre()).isEqualTo("Ana");
        assertThat(ana.correo()).isEqualTo("ana@uco.edu.co");
        assertThat(ana.telefono()).isEmpty();
        assertThat(ana.tieneRol(Rol.DOCENTE)).isTrue();
        assertThat(ana.tieneRol(Rol.RECTOR)).isFalse();
        assertThat(ana.rol()).isEqualTo(Rol.DOCENTE);
    }

    @Test
    void detectaCuandoElTokenTraeDatosNuevos() {
        Usuario guardado = Usuario.desdeIdentidad(ID, "Ana", "ana@uco.edu.co", "+57300", Rol.DOCENTE);

        assertThat(guardado.difiereDe(Usuario.desdeIdentidad(ID, "Ana", "ana@uco.edu.co", "+57300", Rol.DOCENTE))).isFalse();
        assertThat(guardado.difiereDe(Usuario.desdeIdentidad(ID, "Ana M", "ana@uco.edu.co", "+57300", Rol.DOCENTE))).isTrue();
        assertThat(guardado.difiereDe(Usuario.desdeIdentidad(ID, "Ana", "ana2@uco.edu.co", "+57300", Rol.DOCENTE))).isTrue();
        assertThat(guardado.difiereDe(Usuario.desdeIdentidad(ID, "Ana", "ana@uco.edu.co", null, Rol.DOCENTE))).isTrue();
        assertThat(guardado.difiereDe(Usuario.desdeIdentidad(ID, "Ana", "ana@uco.edu.co", "+57300", Rol.RECTOR))).isTrue();
    }

    @Test
    void exigeNombreYCorreo() {
        assertThatThrownBy(() -> Usuario.desdeIdentidad(ID, null, "a@uco.edu.co", null, Rol.DOCENTE))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> Usuario.desdeIdentidad(ID, "Ana", "", null, Rol.DOCENTE))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void reconoceLosRolesSinDistinguirMayusculas() {
        assertThat(Rol.desde(" coordinador_academico ")).contains(Rol.COORDINADOR_ACADEMICO);
        assertThat(Rol.desde("invitado")).isEmpty();
        assertThat(Rol.desde(null)).isEmpty();
        assertThat(new UsuarioId(UUID.randomUUID()).valor()).isNotNull();
    }
}
