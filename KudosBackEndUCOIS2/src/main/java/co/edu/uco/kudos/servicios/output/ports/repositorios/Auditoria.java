package co.edu.uco.kudos.servicios.output.ports.repositorios;

import co.edu.uco.kudos.servicios.output.dto.RegistroAuditoriaDTO;

/** Registro de auditoría: solo inserción (ADR-012). */
public interface Auditoria {

    void registrar(RegistroAuditoriaDTO registro);
}
