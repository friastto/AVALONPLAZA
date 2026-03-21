package org.frias.avalon.core.jwt.services.implementation;


import org.frias.avalon.core.jwt.services.interfaces.AuthService;
import org.frias.avalon.core.jwt.Dtos.AuthRequest;
import org.frias.avalon.core.jwt.Dtos.AuthResponse;
import org.frias.avalon.core.jwt.util.JwtUtils;
import org.frias.avalon.useravalon.entities.UserAvalon;
import org.frias.avalon.useravalon.repositories.UserRepository;

import org.frias.avalon.util.PassSecure;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AuthServiceImpl implements AuthService {


    private final UserRepository usuarioRepo;


    private final JwtUtils jwtUtils;

    public AuthServiceImpl(UserRepository usuarioRepo, JwtUtils jwtUtils) {
        this.usuarioRepo = usuarioRepo;
        this.jwtUtils = jwtUtils;
    }

    /**
     * Realiza el login: valida usuario y contraseña + genera JWT
     */

    @Override
    public AuthResponse login(AuthRequest request){

        // Buscamos el usuario en la base de datos
        UserAvalon usuario = usuarioRepo.findByUserName(request.username())
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado"));

        // Verificación de contraseña con salt hash
        if (!PassSecure.verifyPassword(request.password(), usuario.getHashSalt(), usuario.getHashPassword())) {
            throw new BadCredentialsException("Credenciales inválidas");
        }

       // System.out.println("el rol de esto es "+usuario.getRolId());

        // Creamos el objeto User que Spring entiende
        UserDetails userDetails = new User(usuario.getUserName(), usuario.getHashPassword(),
                List.of(new SimpleGrantedAuthority(usuario.getRolId().getShortName())));

        // Generamos el JWT pasamos el user detail y el id de la empresa a la que pertenece ese usuario

        Long empresaId =  null;
        Long outletId =  null;

        if (usuario.getCompanyId() != null) {
            // Si tiene empresa, extraemos su ID real (Es un Empleado)
            empresaId = usuario.getCompanyId().getId();
        }if (usuario.getOutletId() != null) {
            // Si tiene empresa, extraemos su ID real (Es un Empleado)
            outletId = usuario.getOutletId().getId();
        }

        String token = jwtUtils.generateToken(userDetails, empresaId,outletId);

        return new AuthResponse(token);
    }
}

