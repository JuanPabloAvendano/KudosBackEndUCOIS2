package co.edu.uco.kudos;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

/** Punto de arranque de kudos-service: Spring Boot y escaneo de las propiedades de configuración. */
@SpringBootApplication
@ConfigurationPropertiesScan
public class KudosApplication {

    public static void main(String[] args) {
        SpringApplication.run(KudosApplication.class, args);
    }
}
