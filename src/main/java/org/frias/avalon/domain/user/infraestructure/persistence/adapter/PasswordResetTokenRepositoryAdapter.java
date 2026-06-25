package org.frias.avalon.domain.user.infraestructure.persistence.adapter;

import org.frias.avalon.domain.user.domain.model.PasswordResetTokenDomain;
import org.frias.avalon.domain.user.domain.port.PasswordResetTokenRepositoryPort;
import org.frias.avalon.domain.user.infraestructure.persistence.entity.PasswordResetToken;
import org.frias.avalon.domain.user.infraestructure.persistence.mapper.PasswordResetTokenMapper;
import org.frias.avalon.domain.user.infraestructure.persistence.repository.JpaPasswordResetTokenRepository;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class PasswordResetTokenRepositoryAdapter implements PasswordResetTokenRepositoryPort {

    private final JpaPasswordResetTokenRepository jpaRepository;
    private final PasswordResetTokenMapper mapper;

    public PasswordResetTokenRepositoryAdapter(JpaPasswordResetTokenRepository jpaRepository, PasswordResetTokenMapper mapper) {
        this.jpaRepository = jpaRepository;
        this.mapper = mapper;
    }

    @Override
    public PasswordResetTokenDomain save(PasswordResetTokenDomain tokenDomain) {
        PasswordResetToken entity = mapper.toEntity(tokenDomain);
        PasswordResetToken savedEntity = jpaRepository.save(entity);
        return mapper.toDomain(savedEntity);
    }

    @Override
    public Optional<PasswordResetTokenDomain> findByVerificationToken(String verificationToken) {
        return jpaRepository.findByVerificationToken(verificationToken).map(mapper::toDomain);
    }

    @Override
    public Optional<PasswordResetTokenDomain> findByUserIdAndPin(Long userId, String pin) {
        return jpaRepository.findByUserIdAndPin(userId, pin).map(mapper::toDomain);
    }

    @Override
    public void delete(PasswordResetTokenDomain tokenDomain) {
        PasswordResetToken entity = mapper.toEntity(tokenDomain);
        jpaRepository.delete(entity);
    }
}