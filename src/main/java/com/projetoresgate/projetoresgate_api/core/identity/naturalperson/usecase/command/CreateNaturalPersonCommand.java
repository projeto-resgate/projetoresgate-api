package com.projetoresgate.projetoresgate_api.core.identity.naturalperson.usecase.command;

import com.projetoresgate.projetoresgate_api.core.identity.naturalperson.domain.enums.Gender;
import com.projetoresgate.projetoresgate_api.shared.domain.Address;
import com.projetoresgate.projetoresgate_api.shared.validation.annotation.Phone;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import org.hibernate.validator.constraints.br.CPF;

import java.time.LocalDate;

public record CreateNaturalPersonCommand(
        @NotBlank(message = "O nome é obrigatório")
        String name,
        @NotBlank(message = "O e-mail é obrigatório")
        @Email(message = "E-mail inválido")
        String email,
        String nickname,
        String rg,
        @CPF(message = "Cpf inválido!")
        String cpf,
        LocalDate birthDate,
        @Phone(message = "Telefone inválido!")
        String phone,
        String cellphone,
        Gender gender,
        @Valid
        Address address
) {
    public CreateNaturalPersonCommand(String name, String email, String nickname, String rg, String cpf, LocalDate birthDate, String phone, String cellphone, Gender gender) {
        this(name, email, nickname, rg, cpf, birthDate, phone, cellphone, gender, null);
    }
}
