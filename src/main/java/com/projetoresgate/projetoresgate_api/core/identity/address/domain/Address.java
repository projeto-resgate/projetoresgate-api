package com.projetoresgate.projetoresgate_api.core.identity.address.domain;

import com.projetoresgate.projetoresgate_api.infrastructure.exception.InternalException;
import com.projetoresgate.projetoresgate_api.shared.entity.AuditableEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;
import org.springframework.util.StringUtils;

import java.util.Objects;
import java.util.UUID;

import static java.util.Objects.nonNull;

@Entity
@Table(name = "address")
@SQLDelete(sql = "UPDATE address SET deleted_at = now() WHERE id = ?")
@SQLRestriction("deleted_at IS NULL")
public class Address extends AuditableEntity {

    @Id
    private UUID id;

    private String zipCode;

    private String number;

    private String complement;

    private String neighborhood;

    private String city;

    private String state;

    protected Address() {
    }

    private Address(UUID id, String zipCode, String number, String complement, String neighborhood, String city, String state) {
        this.id = id;
        this.zipCode = zipCode;
        this.number = number;
        this.complement = complement;
        this.neighborhood = neighborhood;
        this.city = city;
        this.state = state;
        validate();
    }

    public static Address create(String zipCode, String number, String complement, String neighborhood, String city, String state) {
        return new Address(UUID.randomUUID(), zipCode, number, complement, neighborhood, city, state);
    }

    public void validate() {
        if (!StringUtils.hasText(this.zipCode)) {
            throw new InternalException("O CEP não pode ser vazio.");
        }
        if (this.zipCode.length() > 20) {
            throw new InternalException("O CEP não pode exceder 20 caracteres.");
        }
        if (StringUtils.hasText(this.number) && this.number.length() > 20) {
            throw new InternalException("O número não pode exceder 20 caracteres.");
        }
        if (StringUtils.hasText(this.complement) && this.complement.length() > 100) {
            throw new InternalException("O complemento não pode exceder 100 caracteres.");
        }
        if (!StringUtils.hasText(this.city)) {
            throw new InternalException("A cidade não pode ser vazia.");
        }
        if (this.city.length() > 100) {
            throw new InternalException("A cidade não pode exceder 100 caracteres.");
        }
        if (!StringUtils.hasText(this.state)) {
            throw new InternalException("O estado não pode ser vazio.");
        }
        if (this.state.length() > 50) {
            throw new InternalException("O estado não pode exceder 50 caracteres.");
        }
        if (nonNull(this.neighborhood) && this.neighborhood.length() > 100) {
            throw new InternalException("O bairro não pode exceder 100 caracteres.");
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Address other)) return false;
        return Objects.equals(zipCode, other.zipCode)
                && Objects.equals(number, other.number)
                && Objects.equals(complement, other.complement)
                && Objects.equals(neighborhood, other.neighborhood)
                && Objects.equals(city, other.city)
                && Objects.equals(state, other.state);
    }

    @Override
    public int hashCode() {
        return Objects.hash(zipCode, number, complement, neighborhood, city, state);
    }

    public UUID getId() {
        return id;
    }

    public String getZipCode() {
        return zipCode;
    }

    public String getNumber() {
        return number;
    }

    public String getComplement() {
        return complement;
    }

    public String getNeighborhood() {
        return neighborhood;
    }

    public String getCity() {
        return city;
    }

    public String getState() {
        return state;
    }
}
