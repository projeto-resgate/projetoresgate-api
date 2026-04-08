package com.projetoresgate.projetoresgate_api.core.physicalperson.domain.vo;

import com.projetoresgate.projetoresgate_api.infrastructure.exception.InternalException;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import org.springframework.util.StringUtils;

import java.util.Objects;

@Embeddable
public class Rg {

    @Column(name = "rg", length = 20)
    private String value;

    protected Rg() {
    }

    public Rg(String value) {
        if (!StringUtils.hasText(value)) {
            this.value = null;
            return;
        }

        String cleaned = value.replaceAll("[^a-zA-Z0-9]", "").toUpperCase();

        if (cleaned.length() < 5 || cleaned.length() > 15) {
            throw new InternalException("O RG informado possui tamanho inválido.");
        }

        this.value = cleaned;
    }

    public String getValue() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Rg rg = (Rg) o;
        return Objects.equals(value, rg.value);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(value);
    }
}
