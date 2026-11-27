package com.projetoresgate.projetoresgate_api.core.identity.naturalperson.usecase.command;

import com.projetoresgate.projetoresgate_api.core.identity.naturalperson.domain.enums.Gender;
import com.projetoresgate.projetoresgate_api.shared.util.MaskUtils;
import com.projetoresgate.projetoresgate_api.shared.validation.annotation.Cellphone;
import com.projetoresgate.projetoresgate_api.shared.validation.annotation.Phone;
import com.projetoresgate.projetoresgate_api.shared.validation.annotation.RG;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import org.hibernate.validator.constraints.br.CPF;

import java.time.LocalDate;
import java.util.UUID;

import static com.projetoresgate.projetoresgate_api.shared.util.TextUtils.blankToNull;

public record CreateNaturalPersonCommand(
        @NotBlank(message = "O nome é obrigatório")
        String name,
        @Email(message = "E-mail inválido")
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
        UUID workplaceId
) {
    public CreateNaturalPersonCommand {
        nickname = blankToNull(nickname);
        rg = MaskUtils.unmaskUpperCase(rg);
        cpf = MaskUtils.unmask(cpf);
        phone = MaskUtils.unmask(phone);
        cellphone = MaskUtils.unmask(cellphone);
    }
}
