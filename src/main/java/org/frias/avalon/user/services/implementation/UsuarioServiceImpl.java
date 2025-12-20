package org.frias.avalon.user.services.implementation;

import jakarta.persistence.EntityNotFoundException;
import org.frias.avalon.user.dtos.UserResponseDto;
import org.frias.avalon.user.entities.User;
import org.frias.avalon.user.repositories.UsuarioRepository;
import org.frias.avalon.user.services.interfaces.UsuarioService;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UsuarioServiceImpl implements UsuarioService {


    private final UsuarioRepository usuarioRepository;

    public UsuarioServiceImpl(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }


    @Override
    public User searchByUserName(String userName) {


        return usuarioRepository.findByUserName(userName)
                .orElseThrow();

    }

    @Override
    public User getUserEmployeeStatus(String numberId) {

        Optional<User> user = usuarioRepository.findActiveEmployeeByNumberId(numberId);
        if (user.isPresent()) {
            return user.get();
        }
        return null;
    }
}
