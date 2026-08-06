package com.projetoresgate.projetoresgate_api.core.identity.naturalperson.api.dto;

import com.projetoresgate.projetoresgate_api.core.identity.naturalperson.domain.NaturalPerson;
import com.projetoresgate.projetoresgate_api.core.identity.naturalperson.domain.enums.Gender;

import java.time.LocalDate;
import java.util.UUID;

public record NaturalPersonResponse(
        UUID id,
        String name,
        String email,
        String nickname,
        String rg,
        String cpf,
        LocalDate birthDate,
        String phone,
        String cellphone,
        Gender gender
) {
    public static NaturalPersonResponse fromEntity(NaturalPerson entity) {
        return new NaturalPersonResponse(
                entity.getId(),
                entity.getName(),
                entity.getEmail(),
                entity.getNickname(),
                entity.getRg(),
                entity.getCpf(),
                entity.getBirthDate(),
                entity.getPhone(),
                entity.getCellphone(),
                entity.getGender()
        );
    }
}
