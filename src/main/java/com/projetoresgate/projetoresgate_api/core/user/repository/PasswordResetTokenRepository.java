package com.projetoresgate.projetoresgate_api.core.user.repository;

import com.projetoresgate.projetoresgate_api.core.user.domain.PasswordResetToken;
import com.projetoresgate.projetoresgate_api.core.user.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;
import java.util.UUID;

public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, UUID> {
    Optional<PasswordResetToken> findByTokenHash(String tokenHash);
    
    @Modifying
    @Query("delete from PasswordResetToken t where t.user = ?1")
    void deleteByUser(User user);
}
