package com.projetoresgate.projetoresgate_api.core.academic.collaborator.domain;

import com.projetoresgate.projetoresgate_api.infrastructure.exception.InternalException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("Collaborator - Test")
class CollaboratorTest {

    @Test
    @DisplayName("Deve criar um colaborador com pessoa física e sem pessoa jurídica")
    void create_ShouldCreateWithNaturalPersonAndNoLegalPerson() {
        UUID naturalPersonId = UUID.randomUUID();

        Collaborator collaborator = Collaborator.create(true, naturalPersonId, null);

        assertEquals(naturalPersonId, collaborator.getNaturalPersonId());
        assertNull(collaborator.getLegalPersonId());
        assertTrue(collaborator.isVoluntary());
    }

    @Test
    @DisplayName("Deve exigir a pessoa física do colaborador")
    void create_ShouldRejectWithoutNaturalPerson() {
        assertThrows(InternalException.class, () -> Collaborator.create(false, null, null));
    }

    @Test
    @DisplayName("Deve permitir pessoa jurídica opcional")
    void create_ShouldAllowLegalPerson() {
        UUID legalPersonId = UUID.randomUUID();

        Collaborator collaborator = Collaborator.create(false, UUID.randomUUID(), legalPersonId);

        assertEquals(legalPersonId, collaborator.getLegalPersonId());
        assertFalse(collaborator.isVoluntary());
    }

    @Test
    @DisplayName("Deve limpar a pessoa jurídica ao atualizar com nulo")
    void update_ShouldClearLegalPerson() {
        Collaborator collaborator = Collaborator.create(false, UUID.randomUUID(), UUID.randomUUID());

        collaborator.update().legalPersonId(null).apply();

        assertNull(collaborator.getLegalPersonId());
    }

    @Test
    @DisplayName("Deve atualizar a pessoa física, a voluntariedade e a pessoa jurídica")
    void update_ShouldUpdateFields() {
        Collaborator collaborator = Collaborator.create(false, UUID.randomUUID(), null);
        UUID newNaturalPersonId = UUID.randomUUID();
        UUID legalPersonId = UUID.randomUUID();

        collaborator.update()
                .naturalPersonId(newNaturalPersonId)
                .legalPersonId(legalPersonId)
                .isVoluntary(true)
                .apply();

        assertEquals(newNaturalPersonId, collaborator.getNaturalPersonId());
        assertEquals(legalPersonId, collaborator.getLegalPersonId());
        assertTrue(collaborator.isVoluntary());
    }

    @Test
    @DisplayName("Deve ignorar a tentativa de zerar a pessoa física na atualização")
    void update_ShouldIgnoreNullNaturalPerson() {
        UUID naturalPersonId = UUID.randomUUID();
        Collaborator collaborator = Collaborator.create(false, naturalPersonId, null);

        collaborator.update().naturalPersonId(null).apply();

        assertEquals(naturalPersonId, collaborator.getNaturalPersonId());
    }
}
