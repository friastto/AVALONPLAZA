package org.frias.avalon.domain.recoverypassword.repository;

import org.frias.avalon.domain.recoverypassword.service.entity.PasswordReset;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PasswordResetRepository extends JpaRepository<PasswordReset,Long> {

    Optional<PasswordReset> findByTokenAndUserName(String token, String userName);

    void deleteByUserName(String userName);
}
