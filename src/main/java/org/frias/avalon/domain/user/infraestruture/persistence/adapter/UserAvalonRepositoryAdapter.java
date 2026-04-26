package org.frias.avalon.domain.user.infraestruture.persistence.adapter;

import org.frias.avalon.domain.user.domain.mapper.UserAvalonMapper;
import org.frias.avalon.domain.user.domain.port.UserAvalonRepositoryPort;
import org.frias.avalon.domain.user.domain.model.UserAvalonDomain;
import org.frias.avalon.domain.user.infraestruture.persistence.entity.UserAvalon;
import org.frias.avalon.domain.user.infraestruture.persistence.repository.JpaUserAvalonRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
public class UserAvalonRepositoryAdapter implements UserAvalonRepositoryPort {

    private final JpaUserAvalonRepository jpa;
    private final UserAvalonMapper mapper;

    public UserAvalonRepositoryAdapter(JpaUserAvalonRepository jpa, UserAvalonMapper mapper) {
        this.jpa = jpa;
        this.mapper = mapper;
    }


    @Override
    public Optional<UserAvalonDomain> findById(Long id) {

        return jpa.findById(id).map(mapper::toDomain);
    }

    @Override
    public UserAvalonDomain save(UserAvalonDomain userRoot) {

        UserAvalon ua = mapper.toEntity(userRoot);

        UserAvalon userSaved = jpa.save(ua);

        return mapper.toDomain(userSaved);
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

    @Override
    public List<UserAvalonDomain> getAll() {
        List<UserAvalon> userAvalonList = jpa.findAll();

        return userAvalonList.stream().map(mapper::toDomain).toList();
    }

    @Override
    public Optional<UserAvalonDomain> findByUserNmae(String userName) {
        return jpa.findByUserName(userName).map(mapper::toDomain);
    }
}
