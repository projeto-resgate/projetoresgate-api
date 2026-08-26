package com.projetoresgate.projetoresgate_api.core.identity.legalperson.domain;

import com.projetoresgate.projetoresgate_api.infrastructure.exception.InternalException;
import jakarta.persistence.Embeddable;
import org.springframework.util.StringUtils;

import java.io.Serializable;

@Embeddable
public class Representative implements Serializable {

    private String name;

    private String cellphone;

    private String phone;

    private String email;

    protected Representative() {
    }

    private Representative(String name, String cellphone, String phone, String email) {
        this.name = name;
        this.cellphone = cellphone;
        this.phone = phone;
        this.email = email;
        validate();
    }

    public static Representative create(String name, String cellphone, String phone, String email) {
        return new Representative(name, cellphone, phone, email);
    }

    public void validate() {
        if (!StringUtils.hasText(this.name)) {
            throw new InternalException("O nome do representante não pode ser vazio.");
        }
        if (this.name.length() > 255) {
            throw new InternalException("O nome do representante não pode exceder 255 caracteres.");
        }
        if (StringUtils.hasText(this.cellphone) && this.cellphone.length() > 20) {
            throw new InternalException("O celular do representante não pode exceder 20 caracteres.");
        }
        if (StringUtils.hasText(this.phone) && this.phone.length() > 20) {
            throw new InternalException("O telefone do representante não pode exceder 20 caracteres.");
        }
        if (StringUtils.hasText(this.email) && this.email.length() > 255) {
            throw new InternalException("O e-mail do representante não pode exceder 255 caracteres.");
        }
    }

    public String getName() {
        return name;
    }

    public String getCellphone() {
        return cellphone;
    }

    public String getPhone() {
        return phone;
    }

    public String getEmail() {
        return email;
    }
}
