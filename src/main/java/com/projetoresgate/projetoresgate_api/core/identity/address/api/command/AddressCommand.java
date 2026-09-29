package com.projetoresgate.projetoresgate_api.core.identity.address.api.command;

public record AddressCommand(
        String streetType,
        String streetName,
        String zipCode,
        String number,
        String referencePoint,
        String complement,
        String neighborhood,
        String city,
        String state
) {
}
