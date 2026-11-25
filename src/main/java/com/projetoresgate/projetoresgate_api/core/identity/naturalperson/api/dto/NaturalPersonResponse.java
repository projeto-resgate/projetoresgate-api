package com.projetoresgate.projetoresgate_api.core.identity.naturalperson.api.dto;

import com.projetoresgate.projetoresgate_api.core.identity.naturalperson.domain.NaturalPerson;
import com.projetoresgate.projetoresgate_api.core.identity.naturalperson.domain.enums.Gender;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public record NaturalPersonResponse(
        UUID id,
        String name,
        String email,
        boolean emailVerified,
        String nickname,
        String rg,
        String cpf,
        LocalDate birthDate,
        String phone,
        String cellphone,
        Gender gender,
        UUID workplaceId,
        String workplaceName,
        LocalDateTime dateCreated
) {
    public static NaturalPersonResponse fromEntity(NaturalPerson entity) {
        return fromEntity(entity, null);
    }

    public static NaturalPersonResponse fromEntity(NaturalPerson entity, String workplaceName) {
        return new NaturalPersonResponse(
                entity.getId(),
                entity.getName(),
                entity.getEmail(),
                entity.isEmailVerified(),
                entity.getNickname(),
                entity.getRg(),
                entity.getCpf(),
                entity.getBirthDate(),
                entity.getPhone(),
                entity.getCellphone(),
                entity.getGender(),
                entity.getWorkplaceId(),
                workplaceName,
                entity.getDateCreated()
        );
    }
}
