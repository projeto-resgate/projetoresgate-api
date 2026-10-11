package com.projetoresgate.projetoresgate_api.core.identity.legalperson.api.dto;

import com.projetoresgate.projetoresgate_api.core.identity.legalperson.domain.Representative;

import static java.util.Objects.isNull;

public record RepresentativeResponse(
        String name,
        String cellphone,
        String phone,
        String email
) {
    public static RepresentativeResponse fromEntity(Representative representative) {
        if (isNull(representative)) {
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
