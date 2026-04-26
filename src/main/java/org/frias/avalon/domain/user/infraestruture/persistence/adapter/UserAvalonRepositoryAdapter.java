package org.frias.avalon.domain.user.infraestruture.persistence.adapter;

import org.frias.avalon.domain.user.domain.mapper.UserAvalonMapper;
import org.frias.avalon.domain.user.domain.port.UserAvalonRepositoryPort;
import org.frias.avalon.domain.user.domain.model.UserAvalonDomain;
import org.frias.avalon.domain.user.infraestruture.persistence.entity.UserAvalon;
import org.frias.avalon.domain.user.infraestruture.persistence.repository.JpaUserAvalonRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class UserAvalonRepositoryAdapter implements UserAvalonRepositoryPort {

    private final JpaUserAvalonRepository jpa;
    private final UserAvalonMapper userAvalonMapper;

    public UserAvalonRepositoryAdapter(JpaUserAvalonRepository jpa, UserAvalonMapper userAvalonMapper) {
        this.jpa = jpa;
        this.userAvalonMapper = userAvalonMapper;
    }


    @Override
    public Optional<UserAvalonDomain> findById(Long id) {
        return Optional.empty();
    }

    @Override
    public UserAvalonDomain save(UserAvalonDomain userRoot) {

        UserAvalon ua = userAvalonMapper.toEntity(userRoot);

        UserAvalon userSaved = jpa.save(ua);

        return userAvalonMapper.toDomain(userSaved);
    }

    @Override
    public void deleteById(Long id) {

    }

    @Override
    public boolean existsById(Long id) {
        return false;
    }

    @Override
    public boolean existByUsername(String userName) {
        return false;
    }
}
