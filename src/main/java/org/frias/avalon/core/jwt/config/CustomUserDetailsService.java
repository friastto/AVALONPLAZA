package org.frias.avalon.core.jwt.config;


import org.frias.avalon.domain.usergeneral.useravalon.entities.UserAvalon;
import org.frias.avalon.domain.usergeneral.useravalon.repositories.UserRepository;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collections;

@Service
public class CustomUserDetailsService implements UserDetailsService {


    private final UserRepository usuarioRepository;

    public CustomUserDetailsService(UserRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }


    // Spring Security llama este método automáticamente para autenticar

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

        // Buscamos el usuario en la base de datos
        UserAvalon usuario = usuarioRepository.findByUserName(username)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado"));

        // Devolvemos un objeto User que Spring Security entiende
        return new User(usuario.getUserName(), usuario.getHashPassword(),
                Collections.singletonList(new SimpleGrantedAuthority(usuario.getRolId().getShortName())));
    }
}
