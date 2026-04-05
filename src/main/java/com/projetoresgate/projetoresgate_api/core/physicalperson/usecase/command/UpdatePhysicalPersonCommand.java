package com.projetoresgate.projetoresgate_api.core.physicalperson.usecase.command;

import com.projetoresgate.projetoresgate_api.core.physicalperson.domain.enums.Gender;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.UUID;

public record UpdatePhysicalPersonCommand(
        @NotNull(message = "O ID é obrigatório")
        UUID id,
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
                this.rg,
                this.cpf,
                this.birthDate,
                this.phone,
                this.cellphone,
                this.gender
        );
    }
}
