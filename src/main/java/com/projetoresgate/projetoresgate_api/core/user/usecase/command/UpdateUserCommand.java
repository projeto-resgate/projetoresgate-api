package com.projetoresgate.projetoresgate_api.core.user.usecase.command;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record UpdateUserCommand(
        @JsonIgnore
        UUID id,

        String name,

        @Size(min = 6, message = "A senha deve ter no mínimo 6 caracteres.")
        String password,

        String currentPassword
) {
    public UpdateUserCommand withId(UUID id) {
        return new UpdateUserCommand(id, this.name, this.password, this.currentPassword);
    }
}
