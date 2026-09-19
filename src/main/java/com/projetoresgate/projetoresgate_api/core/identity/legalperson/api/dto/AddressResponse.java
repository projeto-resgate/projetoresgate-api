package com.projetoresgate.projetoresgate_api.core.identity.legalperson.api.dto;

import com.projetoresgate.projetoresgate_api.core.identity.address.domain.Address;

import java.util.UUID;

public record AddressResponse(
        UUID id,
        String zipCode,
        String number,
        String complement,
        String neighborhood,
        String city,
        String state
) {
    public static AddressResponse fromEntity(Address address) {
        if (address == null) {
            return null;
        }

        return new AddressResponse(
                address.getId(),
                address.getZipCode(),
                address.getNumber(),
                address.getComplement(),
                address.getNeighborhood(),
                address.getCity(),
                address.getState()
        );
    }
}