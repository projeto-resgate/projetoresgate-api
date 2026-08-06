package com.projetoresgate.projetoresgate_api.core.identity.naturalperson.domain;

import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "email_confirmation_tokens")
public class EmailConfirmationToken {

    @Id
    private UUID id;

    private String tokenHash;

    @OneToOne(targetEntity = NaturalPerson.class, fetch = FetchType.EAGER)
    @JoinColumn(nullable = false, name = "natural_person_id")
    private NaturalPerson naturalPerson;

    private LocalDateTime expiryDate;

    public EmailConfirmationToken() {
    }

    public EmailConfirmationToken(String tokenHash, NaturalPerson naturalPerson, LocalDateTime expiryDate) {
        this.id = UUID.randomUUID();
        this.tokenHash = tokenHash;
        this.naturalPerson = naturalPerson;
        this.expiryDate = expiryDate;
    }

    public UUID getId() {
        return id;
    }

    public String getTokenHash() {
        return tokenHash;
    }

    public NaturalPerson getNaturalPerson() {
        return naturalPerson;
    }

    public LocalDateTime getExpiryDate() {
        return expiryDate;
    }

    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expiryDate);
    }
}
