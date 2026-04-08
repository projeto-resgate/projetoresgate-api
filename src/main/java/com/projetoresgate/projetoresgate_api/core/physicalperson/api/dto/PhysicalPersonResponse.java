package com.projetoresgate.projetoresgate_api.core.physicalperson.api.dto;

import com.projetoresgate.projetoresgate_api.core.physicalperson.domain.PhysicalPerson;
import com.projetoresgate.projetoresgate_api.core.physicalperson.domain.enums.Gender;
import com.projetoresgate.projetoresgate_api.core.physicalperson.domain.vo.Cpf;
import com.projetoresgate.projetoresgate_api.core.physicalperson.domain.vo.Rg;

import java.time.LocalDate;
import java.util.UUID;

public record PhysicalPersonResponse(
        UUID id,
        UUID userId,
        String name,
        String email,
        String nickname,
        Rg rg,
        Cpf cpf,
        LocalDate birthDate,
        String phone,
        String cellphone,
        Gender gender
) {
    public static PhysicalPersonResponse fromEntity(PhysicalPerson entity) {
        return new PhysicalPersonResponse(
                entity.getId(),
                entity.getUser().getId(),
                entity.getUser().getName(),
                entity.getUser().getEmail(),
                entity.getUser().getNickname(),
                entity.getRg(),
                entity.getCpf(),
                entity.getBirthDate(),
                entity.getPhone(),
                entity.getCellphone(),
                entity.getGender()
        );
    }
}
