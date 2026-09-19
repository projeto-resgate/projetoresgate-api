package com.projetoresgate.projetoresgate_api.core.identity.legalperson.usecase.command;

public record AddressCommand(
        String zipCode,
        String number,
        String complement,
        String neighborhood,
        String city,
        String state
) {
}