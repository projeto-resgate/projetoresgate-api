package com.projetoresgate.projetoresgate_api.core.identity.user.domain;

import com.projetoresgate.projetoresgate_api.core.identity.user.domain.enums.UserRole;
import com.projetoresgate.projetoresgate_api.infrastructure.exception.InternalException;
import com.projetoresgate.projetoresgate_api.shared.entity.AuditableEntity;
import jakarta.persistence.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;
import org.springframework.util.StringUtils;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "users")
@SQLDelete(sql = "UPDATE users SET deleted_at = now() WHERE id = ?")
@SQLRestriction("deleted_at IS NULL")
public class User extends AuditableEntity {

    @Id
    private UUID id;

    private String email;

    private String password;

    private String name;

    private String nickname;

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "user_roles", joinColumns = @JoinColumn(name = "user_id"))
    @Enumerated(EnumType.STRING)
    @Column(name = "role")
    private Set<UserRole> roles = new HashSet<>();

    @Column(nullable = false)
    private long tokenVersion;

    protected User() {
    }

    private User(UUID id, String email, String encodedPassword, String name, String nickname) {
        this.id = id;
        this.email = email;
        this.password = encodedPassword;
        this.name = name;
        this.nickname = nickname;
        this.roles.add(UserRole.USER);
        validate();
    }

    public static User create(String email, String encodedPassword, String name, String nickname) {
        return new User(UUID.randomUUID(), email, encodedPassword, name, nickname);
    }

    public void changePassword(String newEncodedPassword) {
        if (!StringUtils.hasText(newEncodedPassword)) {
            throw new InternalException("A senha não pode ser vazia.");
        }
        this.password = newEncodedPassword;
        validate();
    }

    public void invalidateTokens() {
        this.tokenVersion++;
    }

    public void validate() {
        if (!StringUtils.hasText(this.email)) {
            throw new InternalException("O e-mail não pode ser vazio.");
        }
        if (!StringUtils.hasText(this.name)) {
            throw new InternalException("O nome não pode ser vazio.");
        }
    }

    public Updater update() {
        return new Updater();
    }

    public class Updater {
        public Updater name(String name) {
            User.this.name = name;
            return this;
        }

        public Updater nickname(String nickname) {
            User.this.nickname = nickname;
            return this;
        }

        public void apply() {
            User.this.validate();
        }
    }

    public UUID getId() {
        return id;
    }

    public String getEmail() {
        return email;
    }

    public String getPassword() {
        return password;
    }

    public String getName() {
        return name;
    }

    public String getNickname() {
        return nickname;
    }

    public Set<UserRole> getRoles() {
        return roles;
    }

    public long getTokenVersion() {
        return tokenVersion;
    }

    public void addRole(UserRole role) {
        this.roles.add(role);
    }
}