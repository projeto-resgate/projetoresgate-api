package com.projetoresgate.projetoresgate_api.core.identity.naturalperson.usecase.command;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.projetoresgate.projetoresgate_api.core.identity.naturalperson.domain.enums.Gender;
import com.projetoresgate.projetoresgate_api.shared.validation.annotation.Cellphone;
import com.projetoresgate.projetoresgate_api.shared.validation.annotation.Phone;
import com.projetoresgate.projetoresgate_api.shared.validation.annotation.RG;
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
        Gender gender
) {
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
                this.gender
        );
    }
}
