package com.projetoresgate.projetoresgate_api.core.physicalperson.usecase.command;

import com.projetoresgate.projetoresgate_api.core.physicalperson.domain.enums.Gender;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import java.time.LocalDate;

public record CreatePhysicalPersonCommand(
        @NotBlank(message = "O nome é obrigatório")
        String name,
        @NotBlank(message = "O e-mail é obrigatório")
        @Email(message = "E-mail inválido")
        String email,
        String nickname,
        String rg,
        String cpf,
        LocalDate birthDate,
        String phone,
        String cellphone,
        Gender gender
) {}
