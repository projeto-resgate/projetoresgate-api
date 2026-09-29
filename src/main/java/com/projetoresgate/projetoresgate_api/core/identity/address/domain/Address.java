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

    private String streetType;

    private String streetName;

    private String zipCode;

    private String number;

    private String referencePoint;

    private String complement;

    private String neighborhood;

    private String city;

    private String state;

    protected Address() {
    }

    private Address(UUID id, String streetType, String streetName, String zipCode, String number,
                    String referencePoint, String complement, String neighborhood, String city, String state) {
        this.id = id;
        this.streetType = streetType;
        this.streetName = streetName;
        this.zipCode = zipCode;
        this.number = number;
        this.referencePoint = referencePoint;
        this.complement = complement;
        this.neighborhood = neighborhood;
        this.city = city;
        this.state = state;
        validate();
    }

    public static Address create(String streetType, String streetName, String zipCode, String number,
                                 String referencePoint, String complement, String neighborhood, String city, String state) {
        return new Address(UUID.randomUUID(), streetType, streetName, zipCode, number, referencePoint,
                complement, neighborhood, city, state);
    }

    public Updater update() {
        return new Updater();
    }

    public class Updater {

        public Updater streetType(String streetType) {
            Address.this.streetType = streetType;
            return this;
        }

        public Updater streetName(String streetName) {
            Address.this.streetName = streetName;
            return this;
        }

        public Updater zipCode(String zipCode) {
            Address.this.zipCode = zipCode;
            return this;
        }

        public Updater number(String number) {
            Address.this.number = number;
            return this;
        }

        public Updater referencePoint(String referencePoint) {
            Address.this.referencePoint = referencePoint;
            return this;
        }

        public Updater complement(String complement) {
            Address.this.complement = complement;
            return this;
        }

        public Updater neighborhood(String neighborhood) {
            Address.this.neighborhood = neighborhood;
            return this;
        }

        public Updater city(String city) {
            Address.this.city = city;
            return this;
        }

        public Updater state(String state) {
            Address.this.state = state;
            return this;
        }

        public Address apply() {
            Address.this.validate();
            return Address.this;
        }
    }

    public void validate() {
        if (!StringUtils.hasText(this.zipCode)) {
            throw new InternalException("O CEP não pode ser vazio.");
        }
        if (this.zipCode.length() > 20) {
            throw new InternalException("O CEP não pode exceder 20 caracteres.");
        }
        if (StringUtils.hasText(this.streetType) && this.streetType.length() > 50) {
            throw new InternalException("O tipo de logradouro não pode exceder 50 caracteres.");
        }
        if (StringUtils.hasText(this.streetName) && this.streetName.length() > 255) {
            throw new InternalException("O nome do logradouro não pode exceder 255 caracteres.");
        }
        if (StringUtils.hasText(this.number) && this.number.length() > 20) {
            throw new InternalException("O número não pode exceder 20 caracteres.");
        }
        if (StringUtils.hasText(this.referencePoint) && this.referencePoint.length() > 255) {
            throw new InternalException("O ponto de referência não pode exceder 255 caracteres.");
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
        return Objects.equals(streetType, other.streetType)
                && Objects.equals(streetName, other.streetName)
                && Objects.equals(zipCode, other.zipCode)
                && Objects.equals(number, other.number)
                && Objects.equals(referencePoint, other.referencePoint)
                && Objects.equals(complement, other.complement)
                && Objects.equals(neighborhood, other.neighborhood)
                && Objects.equals(city, other.city)
                && Objects.equals(state, other.state);
    }

    @Override
    public int hashCode() {
        return Objects.hash(streetType, streetName, zipCode, number, referencePoint, complement, neighborhood, city, state);
    }

    public UUID getId() {
        return id;
    }

    public String getStreetType() {
        return streetType;
    }

    public String getStreetName() {
        return streetName;
    }

    public String getZipCode() {
        return zipCode;
    }

    public String getNumber() {
        return number;
    }

    public String getReferencePoint() {
        return referencePoint;
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
