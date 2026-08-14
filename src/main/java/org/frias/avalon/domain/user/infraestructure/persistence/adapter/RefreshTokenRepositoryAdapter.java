package org.frias.avalon.domain.user.infraestructure.persistence.adapter;

import org.frias.avalon.domain.user.infraestructure.persistence.mapper.RefreshTokenMapper;
import org.frias.avalon.domain.user.domain.model.RefreshTokenDomain;
import org.frias.avalon.domain.user.domain.port.RefreshTokenRepositoryPort;
import org.frias.avalon.domain.user.infraestructure.persistence.repository.RefreshTokenRepository;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Component
public class RefreshTokenRepositoryAdapter implements RefreshTokenRepositoryPort {
    private final RefreshTokenRepository jpa;
    private final RefreshTokenMapper mapper;

    public RefreshTokenRepositoryAdapter(RefreshTokenRepository jpa, RefreshTokenMapper mapper) {
        this.jpa = jpa;
        this.mapper = mapper;
    }

    @Override
    public RefreshTokenDomain save(RefreshTokenDomain refreshToken) {


        return mapper.toDomain(jpa.save(mapper.toEntity(refreshToken)));
    }

    @Override
    public Optional<RefreshTokenDomain> findByRefreshToken(String refreshTokenValue) {
        return jpa.findByRefreshToken(refreshTokenValue).map(mapper::toDomain);
    }


    @Override
    public void deleteByUser(Long userAvalonId) {

    }

    @Override
    public void delete(UUID refreshToken) {

    }

    @Override
    public void deleteByRefreshToken(String refreshTokenValue) {
        jpa.deleteByRefreshToken(refreshTokenValue);
    }

}
