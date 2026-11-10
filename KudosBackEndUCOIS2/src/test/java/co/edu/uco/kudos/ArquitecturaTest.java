package co.edu.uco.kudos;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;
import static com.tngtech.archunit.library.Architectures.layeredArchitecture;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

/**
 * Regla de dependencias del diagrama de paquetes (diseño detallado §2, A-2 de ucotestapi):
 * infraestructura → servicios → dominio, y el dominio no conoce a nadie.
 */
class ArquitecturaTest {

    private static final String BASE = "co.edu.uco.kudos";
    private static JavaClasses clases;

    @BeforeAll
    static void importar() {
        clases = new ClassFileImporter().withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS).importPackages(BASE);
    }

    @Test
    void lasCapasSoloDependenHaciaDentro() {
        layeredArchitecture().consideringOnlyDependenciesInLayers()
                .withOptionalLayers(true)
                .layer("Raiz").definedBy(BASE)
                .layer("Dominio").definedBy(BASE + ".dominio..")
                .layer("Servicios").definedBy(BASE + ".servicios..")
                .layer("Infraestructura").definedBy(BASE + ".infraestructura..")
                .whereLayer("Infraestructura").mayOnlyBeAccessedByLayers("Raiz")
                .whereLayer("Servicios").mayOnlyBeAccessedByLayers("Infraestructura", "Raiz")
                .whereLayer("Dominio").mayOnlyBeAccessedByLayers("Servicios", "Infraestructura", "Raiz")
                .check(clases);
    }

    @Test
    void elDominioYLosServiciosNoUsanFrameworks() {
        noClasses().that().resideInAnyPackage(BASE + ".dominio..", BASE + ".servicios..")
                .should().dependOnClassesThat().resideInAnyPackage("org.springframework..", "jakarta.persistence..")
                .allowEmptyShould(true)
                .check(clases);
    }

    @Test
    void losControladoresSoloUsanPuertosYDtoDeEntrada() {
        noClasses().that().resideInAPackage(BASE + ".infraestructura.input..")
                .should().dependOnClassesThat().resideInAnyPackage(
                        BASE + ".servicios.casosuso..", BASE + ".servicios.input.adapter..", BASE + ".servicios.output..")
                .allowEmptyShould(true)
                .check(clases);
    }

    @Test
    void laInfraestructuraDeSalidaSoloImplementaPuertosDeSalida() {
        noClasses().that().resideInAPackage(BASE + ".infraestructura.output..")
                .should().dependOnClassesThat().resideInAnyPackage(
                        BASE + ".servicios.input..", BASE + ".servicios.casosuso..", BASE + ".servicios.output.adapter..",
                        BASE + ".infraestructura.input..")
                .allowEmptyShould(true)
                .check(clases);
    }
}
