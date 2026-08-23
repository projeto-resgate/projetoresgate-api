package com.projetoresgate.projetoresgate_api.core.identity.naturalperson.domain;

import com.projetoresgate.projetoresgate_api.core.identity.naturalperson.domain.enums.Gender;
import com.projetoresgate.projetoresgate_api.infrastructure.exception.InternalException;
import com.projetoresgate.projetoresgate_api.shared.domain.Address;
import com.projetoresgate.projetoresgate_api.shared.entity.AuditableEntity;
import jakarta.persistence.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.util.UUID;

import static java.util.Objects.nonNull;

@Entity
@Table(name = "natural_person")
@SQLDelete(sql = "UPDATE natural_person SET deleted_at = now() WHERE id = ?")
@SQLRestriction("deleted_at IS NULL")
public class NaturalPerson extends AuditableEntity {

    @Id
    private UUID id;

    private String name;

    private String email;

    private String nickname;

    private String cpf;

    private String rg;

    private LocalDate birthDate;

    @Enumerated(EnumType.STRING)
    private Gender gender;

    private String phone;

    private String cellphone;

    private boolean isEmailVerified = false;

    @Embedded
    private Address address;

    protected NaturalPerson() {
    }

    private NaturalPerson(UUID id, String name, String email, String nickname, String cpf, String rg, LocalDate birthDate, Gender gender, String phone, String cellphone, Address address) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.nickname = nickname;
        this.cpf = cpf;
        this.rg = rg;
        this.birthDate = birthDate;
        this.gender = gender;
        this.phone = phone;
        this.cellphone = cellphone;
        this.address = address;
        this.isEmailVerified = false;
        validate();
    }

    public static NaturalPerson create(String name, String email, String nickname, String cpf, String rg, LocalDate birthDate, Gender gender, String phone, String cellphone) {
        return new NaturalPerson(UUID.randomUUID(), name, email, nickname, cpf, rg, birthDate, gender, phone, cellphone, null);
    }

    public static NaturalPerson create(String name, String email, String nickname, String cpf, String rg, LocalDate birthDate, Gender gender, String phone, String cellphone, Address address) {
        return new NaturalPerson(UUID.randomUUID(), name, email, nickname, cpf, rg, birthDate, gender, phone, cellphone, address);
    }

    public void confirmEmail() {
        this.isEmailVerified = true;
    }

    public void validate() {
        if (!StringUtils.hasText(this.name)) {
            throw new InternalException("O nome não pode ser vazio.");
        }
        if (!StringUtils.hasText(this.email)) {
            throw new InternalException("O e-mail não pode ser vazio.");
        }
        if (StringUtils.hasText(this.cpf) && this.cpf.length() > 11) {
            throw new InternalException("O CPF não pode exceder 11 caracteres.");
        }
        if (StringUtils.hasText(this.rg) && this.rg.length() > 20) {
            throw new InternalException("O RG não pode exceder 20 caracteres.");
        }
        if (nonNull(this.birthDate) && this.birthDate.isAfter(LocalDate.now())) {
            throw new InternalException("A data de nascimento não pode estar no futuro.");
        }
        if (StringUtils.hasText(this.phone) && this.phone.length() > 20) {
            throw new InternalException("O telefone não pode exceder 20 caracteres.");
        }
        if (StringUtils.hasText(this.cellphone) && this.cellphone.length() > 20) {
            throw new InternalException("O celular não pode exceder 20 caracteres.");
        }
        if (nonNull(this.address)) {
            this.address.validate();
        }
    }

    public Updater update() {
        return new Updater();
    }

    public class Updater {

        public Updater name(String name) {
            NaturalPerson.this.name = name;
            return this;
        }

        public Updater email(String email) {
            NaturalPerson.this.email = email;
            return this;
        }

        public Updater nickname(String nickname) {
            NaturalPerson.this.nickname = nickname;
            return this;
        }

        public Updater cpf(String cpf) {
            NaturalPerson.this.cpf = cpf;
            return this;
        }

        public Updater rg(String rg) {
            NaturalPerson.this.rg = rg;
            return this;
        }

        public Updater birthDate(LocalDate birthDate) {
            NaturalPerson.this.birthDate = birthDate;
            return this;
        }

        public Updater gender(Gender gender) {
            NaturalPerson.this.gender = gender;
            return this;
        }

        public Updater phone(String phone) {
            NaturalPerson.this.phone = phone;
            return this;
        }

        public Updater cellphone(String cellphone) {
            NaturalPerson.this.cellphone = cellphone;
            return this;
        }

        public Updater address(Address address) {
            NaturalPerson.this.address = address;
            return this;
        }

        public NaturalPerson apply() {
            NaturalPerson.this.validate();
            return NaturalPerson.this;
        }
    }

    public UUID getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public String getNickname() {
        return nickname;
    }

    public String getCpf() {
        return cpf;
    }

    public String getRg() {
        return rg;
    }

    public LocalDate getBirthDate() {
        return birthDate;
    }

    public Gender getGender() {
        return gender;
    }

    public String getPhone() {
        return phone;
    }

    public String getCellphone() {
        return cellphone;
    }

    public boolean isEmailVerified() {
        return isEmailVerified;
    }

    public Address getAddress() {
        return address;
    }
}
