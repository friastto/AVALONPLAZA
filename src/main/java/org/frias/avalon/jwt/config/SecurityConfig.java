package org.frias.avalon.jwt.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Autowired
    private JwtAuthenticationFilter jwtAuthFilter;

    @Autowired
    private CustomUserDetailsService userDetailsService;

    @Bean
    @Profile("!test") // 👈 seguridad activa solo si NO es perfil test
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        return http
                .csrf(AbstractHttpConfigurer::disable) // Deshabilitamos CSRF para APIs
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/userAvalon/save").permitAll() // login público
                        .requestMatchers("/userAvalon/validateCredentials").permitAll()
                        .requestMatchers("/company/**").hasAnyRole("ADMINTI","ADMIN")
                        .requestMatchers("/outlet/**").hasAnyRole("ADMINTI","ADMIN","GERGEN")
                        .requestMatchers("/masterData/**").permitAll()
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

