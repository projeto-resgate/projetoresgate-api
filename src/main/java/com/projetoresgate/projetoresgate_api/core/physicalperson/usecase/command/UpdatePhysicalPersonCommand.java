package com.projetoresgate.projetoresgate_api.core.physicalperson.usecase.command;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.projetoresgate.projetoresgate_api.core.physicalperson.domain.enums.Gender;

import java.time.LocalDate;
import java.util.UUID;

public record UpdatePhysicalPersonCommand(

        @JsonIgnore
        UUID id,

        String name,
        String nickname,
        String rg,
        String cpf,
        LocalDate birthDate,
        String phone,
        String cellphone,
        Gender gender
) {
    public UpdatePhysicalPersonCommand withId(UUID id) {
        return new UpdatePhysicalPersonCommand(
                id,
                this.name,
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
