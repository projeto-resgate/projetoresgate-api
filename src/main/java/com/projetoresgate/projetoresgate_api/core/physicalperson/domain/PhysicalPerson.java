package com.projetoresgate.projetoresgate_api.core.physicalperson.domain;

import com.projetoresgate.projetoresgate_api.core.physicalperson.domain.enums.Gender;
import com.projetoresgate.projetoresgate_api.core.physicalperson.domain.vo.Cpf;
import com.projetoresgate.projetoresgate_api.core.physicalperson.domain.vo.Rg;
import com.projetoresgate.projetoresgate_api.core.user.domain.User;
import com.projetoresgate.projetoresgate_api.infrastructure.baseentities.BaseModel;
import com.projetoresgate.projetoresgate_api.infrastructure.exception.InternalException;
import jakarta.persistence.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

import java.time.LocalDate;

@Entity
@Table(name = "physical_persons")
@SQLDelete(sql = "UPDATE physical_persons SET deleted_at = now() WHERE id = ?")
@Where(clause = "deleted_at is null")
public class PhysicalPerson extends BaseModel {

    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name = "user_id", unique = true, nullable = false, updatable = false)
    private User user;

    @Embedded
    private Cpf cpf;

    @Embedded
    private Rg rg;

    @Column(name = "birth_date")
    private LocalDate birthDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "gender")
    private Gender gender;

    @Column(name = "phone")
    private String phone;

    @Column(name = "cellphone")
    private String cellphone;

    protected PhysicalPerson() {
    }

    private PhysicalPerson(User user, Cpf cpf, Rg rg, LocalDate birthDate, Gender gender, String phone, String cellphone) {
        this.user = user;
        this.cpf = cpf;
        this.rg = rg;
        this.birthDate = birthDate;
        this.gender = gender;
        this.phone = phone;
        this.cellphone = cellphone;
        validate();
    }

    public static PhysicalPerson create(User user, Cpf cpf, Rg rg, LocalDate birthDate, Gender gender, String phone, String cellphone) {
        return new PhysicalPerson(user, cpf, rg, birthDate, gender, phone, cellphone);
    }

    public void setCpf(Cpf cpf) {
        this.cpf = cpf;
    }

    public void setRg(Rg rg) {
        this.rg = rg;
    }

    public void setBirthDate(LocalDate birthDate) {
        this.birthDate = birthDate;
    }

    public void setGender(Gender gender) {
        this.gender = gender;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public void setCellphone(String cellphone) {
        this.cellphone = cellphone;
    }

    public void validate() {
        if (this.cpf == null || this.cpf.getValue() == null) {
            throw new InternalException("O CPF é obrigatório.");
        }
        if (this.user == null) {
            throw new InternalException("O usuário é obrigatório.");
        }
    }

    public User getUser() {
        return user;
    }

    public Cpf getCpf() {
        return cpf;
    }

    public Rg getRg() {
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
