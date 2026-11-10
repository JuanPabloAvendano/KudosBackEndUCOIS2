package co.edu.uco.kudos.infraestructura.input.controladores;

import java.util.Collection;
import java.util.List;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.web.authentication.BearerTokenAuthenticationFilter;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Seguridad de entrada (ADR-013): cabecera secreta del gateway, JWT de Entra External ID validado siempre (firma,
 * emisor, vencimiento) y roles del token como autoridades. Sin sesión. Solo la salud queda abierta. CSRF sigue
 * activo: el resource server ya exime a las peticiones con {@code Authorization: Bearer}, que es como llama el front.
 */
@Configuration(proxyBeanMethods = false)
public class ConfiguracionDeSeguridad {

    @Bean
    SecurityFilterChain cadenaDeSeguridad(HttpSecurity http, PropiedadesDeEntrada propiedades) throws Exception {
        return http
                .authorizeHttpRequests(peticiones -> peticiones
                        .requestMatchers("/actuator/health/**").permitAll()
                        .anyRequest().authenticated())
                .addFilterBefore(new FiltroCabeceraDelGateway(propiedades.gateway()),
                        BearerTokenAuthenticationFilter.class)
                .oauth2ResourceServer(servidor -> servidor
                        .jwt(jwt -> jwt.jwtAuthenticationConverter(convertidor(propiedades.claims().roles()))))
                .sessionManagement(sesion -> sesion.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .build();
    }

    private static JwtAuthenticationConverter convertidor(String claimDeRoles) {
        Converter<Jwt, Collection<GrantedAuthority>> roles = jwt -> {
            List<String> valores = jwt.getClaimAsStringList(claimDeRoles);
            return valores == null ? List.of()
                    : valores.stream().<GrantedAuthority>map(rol -> new SimpleGrantedAuthority("ROLE_" + rol)).toList();
        };
        JwtAuthenticationConverter convertidor = new JwtAuthenticationConverter();
        convertidor.setJwtGrantedAuthoritiesConverter(roles);
        return convertidor;
    }
}
