package org.frias.avalon.core.jwt.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {


    private final JwtAuthenticationFilter jwtAuthFilter;


    private final CustomUserDetailsService userDetailsService;

    public SecurityConfig(JwtAuthenticationFilter jwtAuthFilter, CustomUserDetailsService userDetailsService) {
        this.jwtAuthFilter = jwtAuthFilter;
        this.userDetailsService = userDetailsService;
    }

    @Bean
    @Profile("!test") // 👈 seguridad activa solo si NO es perfil test
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        return http
                .csrf(AbstractHttpConfigurer::disable) // Deshabilitamos CSRF para APIs
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/avalon/user/login").permitAll()
                        .requestMatchers("/avalon/user/create").permitAll()
                        .requestMatchers("/avalon/user/{userId}/assignment/role").hasAnyRole("ADMINTI","ADMIN","GERGEN")
                        .requestMatchers("/avalon/user/assignment/role/consumer/self").permitAll()


                        /*
                                               .requestMatchers("/avalon/public/**").permitAll()

                                               .requestMatchers("/user/link-user-to-person").hasAnyRole("ADMINTI","ADMIN","GERGEN")
                                               .requestMatchers("/user/create").permitAll() // cualquiera puede crearse un usuario

                                               .requestMatchers("/userAvalon/create").permitAll()

                                               .requestMatchers("/company/**").hasAnyRole("ADMINTI","ADMIN","GERGEN")
                                               .requestMatchers("/outlet/**").hasAnyRole("ADMINTI","ADMIN","GERGEN")


                                               .requestMatchers("/productOutlet/**").permitAl

                                               .requestMatchers("/masterData/**").permitAll()

                                               .requestMatchers("/consumer/**").permitAll()

                                               .requestMatchers("/avalon/admin/saas/product/**").hasAnyRole("ADMIN","ADMINTI")


                                               /*
                                               .requestMatchers("/master/create/bulk").permitAll() // crear tipos público
                                               .requestMatchers("/admin/register/newUser").permitAll() //registro de usuario publico
                                               .requestMatchers("/empresa").permitAll() //registro de usuario publico

                                               .requestMatchers("/producto/**").permitAll() //registro de usuario publico
                                               .requestMatchers("/api/admin").hasRole("ADMIN") // solo admin
                                               .requestMatchers("/api/user/**").hasAnyRole("USER","ADMIN") // user o admin

                                                */
                        .anyRequest().authenticated()) // todo lo demás requiere login
                .userDetailsService(userDetailsService) // nuestro servicio personalizado
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class) // filtro antes de login
                .build();
    }


}

