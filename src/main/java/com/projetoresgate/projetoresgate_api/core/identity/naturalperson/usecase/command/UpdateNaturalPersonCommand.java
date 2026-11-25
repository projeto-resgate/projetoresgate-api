package com.projetoresgate.projetoresgate_api.core.identity.naturalperson.usecase.command;

import com.fasterxml.jackson.annotation.JsonIgnore;
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

public record UpdateNaturalPersonCommand(
        @JsonIgnore
        UUID id,
        @NotBlank(message = "O nome é obrigatório")
        String name,
        @NotBlank(message = "O e-mail é obrigatório")
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
    public UpdateNaturalPersonCommand {
        nickname = blankToNull(nickname);
        rg = MaskUtils.unmaskUpperCase(rg);
        cpf = MaskUtils.unmask(cpf);
        phone = MaskUtils.unmask(phone);
        cellphone = MaskUtils.unmask(cellphone);
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
                this.workplaceId
        );
    }
}
