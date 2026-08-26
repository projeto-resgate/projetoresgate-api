package com.projetoresgate.projetoresgate_api.core.identity.legalperson.service;

import com.projetoresgate.projetoresgate_api.core.identity.address.domain.Address;
import com.projetoresgate.projetoresgate_api.core.identity.legalperson.domain.LegalPerson;
import com.projetoresgate.projetoresgate_api.core.identity.legalperson.domain.Representative;
import com.projetoresgate.projetoresgate_api.core.identity.legalperson.domain.enums.CompanyStatus;
import com.projetoresgate.projetoresgate_api.core.identity.legalperson.domain.enums.RegistrationStatus;
import com.projetoresgate.projetoresgate_api.core.identity.legalperson.repository.LegalPersonRepository;
import com.projetoresgate.projetoresgate_api.core.identity.legalperson.usecase.command.UpdateLegalPersonCommand;
import com.projetoresgate.projetoresgate_api.infrastructure.exception.InternalException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("UpdateLegalPersonService - Test")
class UpdateLegalPersonServiceTest {

    @Mock
    private LegalPersonRepository repository;

    @InjectMocks
    private UpdateLegalPersonService service;

    private LegalPerson person;
    private UUID personId;
    private Address address;

    @BeforeEach
    void setUp() {
        address = Address.create("01310-100", "1000", null, "Bela Vista", "São Paulo", "SP");
        person = LegalPerson.create("12345678000195", "Razão Social LTDA", null, null, null,
                RegistrationStatus.ACTIVE, CompanyStatus.ACTIVE, address, null);
        personId = person.getId();
    }

    @Test
    @DisplayName("Deve atualizar todos os campos com sucesso")
    void handle_ShouldUpdateAllFieldsSuccessfully() {
        Address newAddress = Address.create("20040-020", "200", null, "Centro", "Rio de Janeiro", "RJ");
        Representative representative = Representative.create("Jane Doe", null, null, "jane@doe.com");

        UpdateLegalPersonCommand command = new UpdateLegalPersonCommand(
                personId, "Nova Razão Social", "Novo Nome Fantasia", "Novo Display", "98765432000198",
                "8599-6/04", RegistrationStatus.SUSPENDED, CompanyStatus.INACTIVE, newAddress, representative);

        when(repository.findByIdOrThrow(personId)).thenReturn(person);
        when(repository.existsByCnpjAndIdNot("98765432000198", personId)).thenReturn(false);
        when(repository.save(any())).thenReturn(person);

        service.handle(command);

        assertEquals("98765432000198", person.getCnpj());
        assertEquals("Nova Razão Social", person.getCorporateName());
        assertEquals("Novo Nome Fantasia", person.getTradeName());
        assertEquals("Novo Display", person.getDisplayName());
        assertEquals("8599-6/04", person.getMainCnaeCode());
        assertEquals(RegistrationStatus.SUSPENDED, person.getRegistrationStatus());
        assertEquals(CompanyStatus.INACTIVE, person.getCompanyStatus());
        assertEquals("Rio de Janeiro", person.getAddress().getCity());
        assertEquals("Jane Doe", person.getRepresentative().getName());
        verify(repository).save(person);
    }

    @Test
    @DisplayName("Deve lançar exceção ao tentar atualizar com CNPJ já existente em outra pessoa")
    void handle_ShouldFailWhenCnpjExists() {
        UpdateLegalPersonCommand command = new UpdateLegalPersonCommand(
                personId, null, null, null, "98765432000198",
                null, null, null, address);

        when(repository.findByIdOrThrow(personId)).thenReturn(person);
        when(repository.existsByCnpjAndIdNot("98765432000198", personId)).thenReturn(true);

        InternalException exception = assertThrows(InternalException.class, () -> service.handle(command));
        assertEquals("Já existe uma empresa cadastrada com este CNPJ.", exception.getMessage());

        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("Deve permitir atualizar mantendo o mesmo CNPJ")
    void handle_ShouldAllowUpdateWithSameCnpj() {
        UpdateLegalPersonCommand command = new UpdateLegalPersonCommand(
                personId, "Nova Razão Social", null, null, "12345678000195",
                null, RegistrationStatus.ACTIVE, CompanyStatus.ACTIVE, address);

        when(repository.findByIdOrThrow(personId)).thenReturn(person);
        when(repository.existsByCnpjAndIdNot("12345678000195", personId)).thenReturn(false);
        when(repository.save(any())).thenReturn(person);

        service.handle(command);

        assertEquals("Nova Razão Social", person.getCorporateName());
        assertEquals("12345678000195", person.getCnpj());
        verify(repository).existsByCnpjAndIdNot("12345678000195", personId);
        verify(repository).save(person);
    }

    @Test
    @DisplayName("Deve lançar exceção na validação de domínio se a razão social for apagada")
    void handle_ShouldThrowExceptionWhenCorporateNameIsBlanked() {
        UpdateLegalPersonCommand command = new UpdateLegalPersonCommand(
                personId, "", null, null, "12345678000195",
                null, null, null, address);

        when(repository.findByIdOrThrow(personId)).thenReturn(person);

        InternalException exception = assertThrows(InternalException.class, () -> service.handle(command));
        assertEquals("A razão social não pode ser vazia.", exception.getMessage());

        verify(repository, never()).save(any());
    }
}
