package com.projetoresgate.projetoresgate_api.core.identity.naturalperson.domain;

import com.projetoresgate.projetoresgate_api.core.identity.naturalperson.domain.enums.Gender;
import com.projetoresgate.projetoresgate_api.core.identity.user.domain.User;
import com.projetoresgate.projetoresgate_api.infrastructure.exception.InternalException;
import com.projetoresgate.projetoresgate_api.shared.entity.AuditableEntity;
import jakarta.persistence.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.util.UUID;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

@Entity
@Table(name = "natural_person")
@SQLDelete(sql = "UPDATE natural_person SET deleted_at = now() WHERE id = ?")
@SQLRestriction("deleted_at IS NULL")
public class NaturalPerson extends AuditableEntity {

    @Id
    private UUID id;

    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name = "user_id", unique = true, nullable = false, updatable = false)
    private User user;

    private String cpf;

    private String rg;

    private LocalDate birthDate;

    @Enumerated(EnumType.STRING)
    private Gender gender;

    private String phone;

    private String cellphone;

    protected NaturalPerson() {
    }

    private NaturalPerson(UUID id, User user, String cpf, String rg, LocalDate birthDate, Gender gender, String phone, String cellphone) {
        this.id = id;
        this.user = user;
        this.cpf = cpf;
        this.rg = rg;
        this.birthDate = birthDate;
        this.gender = gender;
        this.phone = phone;
        this.cellphone = cellphone;
        validate();
    }

    public static NaturalPerson create(User user, String cpf, String rg, LocalDate birthDate, Gender gender, String phone, String cellphone) {
        return new NaturalPerson(UUID.randomUUID(), user, cpf, rg, birthDate, gender, phone, cellphone);
    }

    public void validate() {
        if (isNull(this.user)) {
            throw new InternalException("O usuário é obrigatório.");
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
    }

    public Updater update() {
        return new Updater();
    }

    public class Updater {

        public Updater name(String name) {
            NaturalPerson.this.user.update().name(name).apply();
            return this;
        }

        public Updater nickname(String nickname) {
            NaturalPerson.this.user.update().nickname(nickname).apply();
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

        public NaturalPerson apply() {
            NaturalPerson.this.validate();
            return NaturalPerson.this;
        }
    }

    public UUID getId() {
        return id;
    }

    public User getUser() {
        return user;
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
}