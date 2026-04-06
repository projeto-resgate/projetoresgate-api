package com.projetoresgate.projetoresgate_api.core.user.domain;

import com.projetoresgate.projetoresgate_api.core.user.domain.enums.UserRole;
import com.projetoresgate.projetoresgate_api.infrastructure.baseentities.BaseModel;
import com.projetoresgate.projetoresgate_api.infrastructure.exception.InternalException;
import jakarta.persistence.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;
import org.springframework.util.StringUtils;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

@Entity
@Table(name = "users")
@SQLDelete(sql = "UPDATE users SET deleted_at = now() WHERE id = ?")
@Where(clause = "deleted_at is null")
public class User extends BaseModel {

    @Column(name = "email", nullable = false, unique = true)
    private String email;

    @Column(name = "password")
    private String password;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "nickname")
    private String nickname;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "user_roles", joinColumns = @JoinColumn(name = "user_id"))
    @Enumerated(EnumType.STRING)
    @Column(name = "role")
    private Set<UserRole> roles = new HashSet<>();

    @Column(name = "is_email_verified", nullable = false)
    private boolean isEmailVerified = false;

    private User(String email, String encodedPassword, String name, String nickname) {
        this.email = email;
        this.password = encodedPassword;
        this.name = name;
        this.nickname = nickname;
        this.roles.add(UserRole.USER);
        this.isEmailVerified = false;
        validate();
    }

    public User() {
    }

    public static User create(String email, String encodedPassword, String name, String nickname) {
        return new User(email, encodedPassword, name, nickname);
    }

    public void updateInfo(String name, String nickname) {
        this.name = name;
        this.nickname = nickname;
        validate();
    }

    public void changePassword(String newEncodedPassword) {
        if (!StringUtils.hasText(newEncodedPassword)) {
            throw new InternalException("A senha não pode ser vazia.");
        }
        this.password = newEncodedPassword;
        validate();
    }

    public void confirmEmail() {
        this.isEmailVerified = true;
    }

    public void validate() {
        if (!StringUtils.hasText(this.email)) {
            throw new InternalException("O e-mail não pode ser vazio.");
        }
        if (!StringUtils.hasText(this.name)) {
            throw new InternalException("O nome não pode ser vazio.");
        }
        if (StringUtils.hasText(this.password) && this.password.length() < 6) {
            throw new InternalException("A senha deve ter no mínimo 6 caracteres.");
        }
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public Set<UserRole> getRoles() {
        return roles;
    }

    public void setRoles(Set<UserRole> roles) {
        this.roles = roles;
    }

    public void addRole(UserRole role) {
        this.roles.add(role);
    }

    public boolean isEmailVerified() {
        return isEmailVerified;
    }

    public void setIsEmailVerified(boolean isEmailVerified) {
        this.isEmailVerified = isEmailVerified;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        User user = (User) o;
        return Objects.equals(email, user.email);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), email);
    }
}
