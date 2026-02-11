package com.projetoresgate.projetoresgate_api.modules.user.usecase.command;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record UpdateUserCommand(
        @JsonIgnore
        UUID id,

        String name,

        @Size(min = 6, message = "{validation.user.password.size}")
        String password,

        String currentPassword
) {
    public UpdateUserCommand withId(UUID id) {
        return new UpdateUserCommand(id, this.name, this.password, this.currentPassword);
    }
}
