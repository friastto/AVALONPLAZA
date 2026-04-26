package org.frias.avalon.domain.user.domain.port;

import org.frias.avalon.domain.user.domain.model.UserAvalonDomain;

import java.util.List;
import java.util.Optional;

public interface UserAvalonRepositoryPort {

    Optional<UserAvalonDomain> findById(Long id);

    UserAvalonDomain save(UserAvalonDomain userAvalon);

    void deleteById(Long id);

    boolean existsById(Long id);

    boolean existByUsername(String userName);


    List<UserAvalonDomain> getAll();

    Optional<UserAvalonDomain> findByUserNmae(String userName);
}
