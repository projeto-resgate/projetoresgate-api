package com.projetoresgate.projetoresgate_api.core.physicalperson.domain.vo;

import com.projetoresgate.projetoresgate_api.infrastructure.exception.InternalException;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import org.springframework.util.StringUtils;

import java.util.Objects;

@Embeddable
public class Cpf {

    @Column(name = "cpf", nullable = false, unique = true, length = 11)
    private String value;

    protected Cpf() {
    }

    public Cpf(String value) {
        if (!StringUtils.hasText(value)) {
            this.value = null;
            return;
        }
        String cleaned = value.replaceAll("\\D", "");
        if (!isValid(cleaned)) {
            throw new InternalException("O CPF informado é inválido.");
        }
        this.value = cleaned;
    }

    private boolean isValid(String cpf) {
        if (cpf.length() != 11 || cpf.matches("(\\d)\\1{10}")) return false;

        int d1 = calculateDigit(cpf.substring(0, 9), 10);
        int d2 = calculateDigit(cpf.substring(0, 9) + d1, 11);

        return cpf.equals(cpf.substring(0, 9) + d1 + d2);
    }

    private int calculateDigit(String str, int weight) {
        int sum = 0;
        for (char c : str.toCharArray()) {
            sum += Character.getNumericValue(c) * weight--;
        }
        int result = 11 - (sum % 11);
        return result > 9 ? 0 : result;
    }

    public String getValue() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Cpf cpf = (Cpf) o;
        return Objects.equals(value, cpf.value);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(value);
    }
}
