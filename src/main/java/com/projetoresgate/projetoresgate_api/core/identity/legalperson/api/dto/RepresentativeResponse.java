package com.projetoresgate.projetoresgate_api.core.identity.legalperson.api.dto;

import com.projetoresgate.projetoresgate_api.core.identity.legalperson.domain.Representative;

public record RepresentativeResponse(
        String name,
        String cellphone,
        String phone,
        String email
) {
    public static RepresentativeResponse fromEntity(Representative representative) {
        if (representative == null) {
            return null;
        }

        return new RepresentativeResponse(
                representative.getName(),
                representative.getCellphone(),
                representative.getPhone(),
                representative.getEmail()
        );
    }
}