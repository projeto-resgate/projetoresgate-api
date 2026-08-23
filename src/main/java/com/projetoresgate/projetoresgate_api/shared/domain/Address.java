package com.projetoresgate.projetoresgate_api.shared.domain;

import com.projetoresgate.projetoresgate_api.infrastructure.exception.InternalException;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.util.StringUtils;

import java.io.Serializable;

import static java.util.Objects.nonNull;

/**
 * Objeto de valor que representa um endereço.
 * Reutilizável pelos domínios que possuem endereço (ex: NaturalPerson, LegalPerson).
 */
@Embeddable
public class Address implements Serializable {

    @NotBlank(message = "O CEP é obrigatório")
    @Size(max = 20, message = "O CEP não pode exceder 20 caracteres")
    @Column(name = "zip_code", length = 20)
    private String zipCode;

    @Size(max = 20, message = "O número não pode exceder 20 caracteres")
    @Column(name = "number", length = 20)
    private String number;

    @Size(max = 100, message = "O complemento não pode exceder 100 caracteres")
    @Column(name = "complement", length = 100)
    private String complement;

    @Size(max = 100, message = "O bairro não pode exceder 100 caracteres")
    @Column(name = "neighborhood", length = 100)
    private String neighborhood;

    @NotBlank(message = "A cidade é obrigatória")
    @Size(max = 100, message = "A cidade não pode exceder 100 caracteres")
    @Column(name = "city", length = 100)
    private String city;

    @NotBlank(message = "O estado é obrigatório")
    @Size(max = 50, message = "O estado não pode exceder 50 caracteres")
    @Column(name = "state", length = 50)
    private String state;

    protected Address() {
    }

    private Address(String zipCode, String number, String complement, String neighborhood, String city, String state) {
        this.zipCode = zipCode;
        this.number = number;
        this.complement = complement;
        this.neighborhood = neighborhood;
        this.city = city;
        this.state = state;
        validate();
    }

    public static Address create(String zipCode, String number, String complement, String neighborhood, String city, String state) {
        return new Address(zipCode, number, complement, neighborhood, city, state);
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

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Address)) {
            return false;
        }
        Address other = (Address) o;
        return equalsValue(this.zipCode, other.zipCode)
                && equalsValue(this.number, other.number)
                && equalsValue(this.complement, other.complement)
                && equalsValue(this.neighborhood, other.neighborhood)
                && equalsValue(this.city, other.city)
                && equalsValue(this.state, other.state);
    }

    @Override
    public int hashCode() {
        int result = nonNull(zipCode) ? zipCode.hashCode() : 0;
        result = 31 * result + (nonNull(city) ? city.hashCode() : 0);
        result = 31 * result + (nonNull(state) ? state.hashCode() : 0);
        return result;
    }

    private boolean equalsValue(String a, String b) {
        if (a == null && b == null) {
            return true;
        }
        return a != null && a.equals(b);
    }
}
