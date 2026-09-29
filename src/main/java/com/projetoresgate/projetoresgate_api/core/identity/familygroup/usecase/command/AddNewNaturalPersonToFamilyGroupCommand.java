package com.projetoresgate.projetoresgate_api.core.identity.familygroup.usecase.command;

import com.projetoresgate.projetoresgate_api.core.identity.naturalperson.usecase.command.CreateNaturalPersonCommand;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record AddNewNaturalPersonToFamilyGroupCommand(
        @NotNull(message = "O grupo familiar é obrigatório")
        UUID familyGroupId,
        @NotNull(message = "Os dados da pessoa física são obrigatórios")
        @Valid
        CreateNaturalPersonCommand naturalPerson
) {
    public AddNewNaturalPersonToFamilyGroupCommand withFamilyGroupId(UUID familyGroupId) {
        return new AddNewNaturalPersonToFamilyGroupCommand(familyGroupId, naturalPerson);
    }
}
