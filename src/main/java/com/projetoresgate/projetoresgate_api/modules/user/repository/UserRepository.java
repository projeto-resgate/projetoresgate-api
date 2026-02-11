package com.projetoresgate.projetoresgate_api.modules.user.repository;

import com.projetoresgate.projetoresgate_api.modules.user.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {
    UserDetails findByEmail(String email);

    Optional<User> findUserByEmail(String email);
}
