package com.projetoresgate.projetoresgate_api.core.user.domain;

import com.projetoresgate.projetoresgate_api.infrastructure.baseentities.BaseModel;
import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "email_confirmation_tokens")
public class EmailConfirmationToken extends BaseModel {

    @Column(nullable = false, unique = true, name = "token_hash")
    private String tokenHash;

    @OneToOne(targetEntity = User.class, fetch = FetchType.EAGER)
    @JoinColumn(nullable = false, name = "user_id")
    private User user;

    @Column(nullable = false)
    private LocalDateTime expiryDate;

    public EmailConfirmationToken() {
    }

    public EmailConfirmationToken(String tokenHash, User user, LocalDateTime expiryDate) {
        this.tokenHash = tokenHash;
        this.user = user;
        this.expiryDate = expiryDate;
    }

    public String getTokenHash() {
        return tokenHash;
    }

    public void setTokenHash(String tokenHash) {
        this.tokenHash = tokenHash;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public LocalDateTime getExpiryDate() {
        return expiryDate;
    }

    public void setExpiryDate(LocalDateTime expiryDate) {
        this.expiryDate = expiryDate;
    }

    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expiryDate);
    }
}
