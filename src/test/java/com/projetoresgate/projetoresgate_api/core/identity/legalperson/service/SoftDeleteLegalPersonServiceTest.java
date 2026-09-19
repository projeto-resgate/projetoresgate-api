package com.projetoresgate.projetoresgate_api.core.identity.legalperson.service;

import com.projetoresgate.projetoresgate_api.core.identity.address.domain.Address;
import com.projetoresgate.projetoresgate_api.core.identity.legalperson.domain.LegalPerson;
import com.projetoresgate.projetoresgate_api.core.identity.legalperson.domain.enums.CompanyStatus;
import com.projetoresgate.projetoresgate_api.core.identity.legalperson.domain.enums.RegistrationStatus;
import com.projetoresgate.projetoresgate_api.core.identity.legalperson.repository.LegalPersonRepository;
import com.projetoresgate.projetoresgate_api.core.identity.legalperson.usecase.command.SoftDeleteLegalPersonCommand;
import com.projetoresgate.projetoresgate_api.infrastructure.exception.ResourceNotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("SoftDeleteLegalPersonService - Test")
class SoftDeleteLegalPersonServiceTest {

    @Mock
    private LegalPersonRepository repository;

    @InjectMocks
    private SoftDeleteLegalPersonService service;

    @Test
    @DisplayName("Deve marcar pessoa jurídica como deletada com sucesso")
    void handle_ShouldDeleteSuccessfully() {
        UUID id = UUID.randomUUID();
        LegalPerson person = LegalPerson.create(
                "11222333000181", "Razão Social LTDA", null, null, null,
                RegistrationStatus.ACTIVE, CompanyStatus.ACTIVE,
                Address.create("01310-100", "1000", null, "Bela Vista", "São Paulo", "SP"), null);
        SoftDeleteLegalPersonCommand command = new SoftDeleteLegalPersonCommand(id);

        when(repository.findByIdOrThrow(id)).thenReturn(person);

        service.handle(command);

        verify(repository).delete(person);
    }

    @Test
    @DisplayName("Deve lançar exceção quando pessoa jurídica não for encontrada")
    void handle_ShouldThrowWhenNotFound() {
        UUID id = UUID.randomUUID();
        SoftDeleteLegalPersonCommand command = new SoftDeleteLegalPersonCommand(id);

        when(repository.findByIdOrThrow(id)).thenThrow(new ResourceNotFoundException("Pessoa jurídica não encontrada com ID: " + id));

        assertThrows(ResourceNotFoundException.class, () -> service.handle(command));
        verify(repository, never()).delete(any(LegalPerson.class));
    }
}