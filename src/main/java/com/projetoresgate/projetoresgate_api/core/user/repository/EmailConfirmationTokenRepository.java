package com.projetoresgate.projetoresgate_api.core.user.repository;

import com.projetoresgate.projetoresgate_api.core.user.domain.EmailConfirmationToken;
import com.projetoresgate.projetoresgate_api.core.user.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;
import java.util.UUID;

public interface EmailConfirmationTokenRepository extends JpaRepository<EmailConfirmationToken, UUID> {
    Optional<EmailConfirmationToken> findByToken(String token);

    @Modifying
    @Query("delete from EmailConfirmationToken t where t.user = ?1")
    void deleteByUser(User user);
}
