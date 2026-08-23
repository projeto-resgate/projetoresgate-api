package com.projetoresgate.projetoresgate_api.core.identity.naturalperson.usecase.command;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.projetoresgate.projetoresgate_api.core.identity.naturalperson.domain.enums.Gender;
import com.projetoresgate.projetoresgate_api.shared.domain.Address;
import com.projetoresgate.projetoresgate_api.shared.validation.annotation.Cellphone;
import com.projetoresgate.projetoresgate_api.shared.validation.annotation.Phone;
import com.projetoresgate.projetoresgate_api.shared.validation.annotation.RG;
import jakarta.validation.Valid;
import org.hibernate.validator.constraints.br.CPF;

import java.time.LocalDate;
import java.util.UUID;

public record UpdateNaturalPersonCommand(

        @JsonIgnore
        UUID id,
        String name,
        String email,
        String nickname,
        @RG
        String rg,
        @CPF(message = "Cpf inválido!")
        String cpf,
        LocalDate birthDate,
        @Phone
        String phone,
        @Cellphone
        String cellphone,
        Gender gender,
        @Valid
        Address address
) {
    public UpdateNaturalPersonCommand(UUID id, String name, String email, String nickname, String rg, String cpf, LocalDate birthDate, String phone, String cellphone, Gender gender) {
        this(id, name, email, nickname, rg, cpf, birthDate, phone, cellphone, gender, null);
    }

    public UpdateNaturalPersonCommand withId(UUID id) {
        return new UpdateNaturalPersonCommand(
                id,
                this.name,
                this.email,
                this.nickname,
                this.rg,
                this.cpf,
                this.birthDate,
                this.phone,
                this.cellphone,
                this.gender,
                this.address
        );
    }
}
