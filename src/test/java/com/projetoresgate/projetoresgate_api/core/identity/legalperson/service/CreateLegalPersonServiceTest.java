package com.projetoresgate.projetoresgate_api.core.identity.legalperson.service;

import com.projetoresgate.projetoresgate_api.core.identity.address.domain.Address;
import com.projetoresgate.projetoresgate_api.core.identity.legalperson.domain.LegalPerson;
import com.projetoresgate.projetoresgate_api.core.identity.legalperson.domain.Representative;
import com.projetoresgate.projetoresgate_api.core.identity.legalperson.domain.enums.CompanyStatus;
import com.projetoresgate.projetoresgate_api.core.identity.legalperson.domain.enums.RegistrationStatus;
import com.projetoresgate.projetoresgate_api.core.identity.legalperson.repository.LegalPersonRepository;
import com.projetoresgate.projetoresgate_api.core.identity.legalperson.usecase.command.CreateLegalPersonCommand;
import com.projetoresgate.projetoresgate_api.infrastructure.exception.InternalException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("CreateLegalPersonService - Test")
class CreateLegalPersonServiceTest {

    @Mock
    private LegalPersonRepository repository;

    @InjectMocks
    private CreateLegalPersonService service;

    private Address buildAddress() {
        return Address.create("01310-100", "1000", null, "Bela Vista", "São Paulo", "SP");
    }

    @Test
    @DisplayName("Deve criar pessoa jurídica com sucesso")
    void handle_ShouldCreateSuccessfully() {
        CreateLegalPersonCommand command = new CreateLegalPersonCommand(
                "Razão Social LTDA", "Nome Fantasia", "Display Name", "12345678000195", "6201-5/00",
                RegistrationStatus.ACTIVE, CompanyStatus.ACTIVE, buildAddress());

        when(repository.existsByCnpj(anyString())).thenReturn(false);
        when(repository.save(any(LegalPerson.class))).thenAnswer(i -> i.getArgument(0));

        LegalPerson created = service.handle(command);

        assertNotNull(created);
        assertNotNull(created.getId());
        assertEquals("12345678000195", created.getCnpj());
        assertEquals("Razão Social LTDA", created.getCorporateName());
        assertEquals("Nome Fantasia", created.getTradeName());
        assertEquals("Display Name", created.getDisplayName());
        assertEquals("6201-5/00", created.getMainCnaeCode());
        assertEquals(RegistrationStatus.ACTIVE, created.getRegistrationStatus());
        assertEquals(CompanyStatus.ACTIVE, created.getCompanyStatus());
        assertNotNull(created.getAddress());
        assertEquals("São Paulo", created.getAddress().getCity());
        assertNull(created.getRepresentative());

        verify(repository).existsByCnpj("12345678000195");
        verify(repository).save(any(LegalPerson.class));
    }

    @Test
    @DisplayName("Deve criar pessoa jurídica com representante com sucesso")
    void handle_ShouldCreateWithRepresentative() {
        Representative representative = Representative.create("John Doe", "11988887777", "1133334444", "john@doe.com");
        CreateLegalPersonCommand command = new CreateLegalPersonCommand(
                "Razão Social LTDA", null, null, "12345678000195", null,
                RegistrationStatus.ACTIVE, CompanyStatus.ACTIVE, buildAddress(), representative);

        when(repository.existsByCnpj(anyString())).thenReturn(false);
        when(repository.save(any(LegalPerson.class))).thenAnswer(i -> i.getArgument(0));

        LegalPerson created = service.handle(command);

        assertNotNull(created);
        assertNotNull(created.getRepresentative());
        assertEquals("John Doe", created.getRepresentative().getName());
        assertEquals("john@doe.com", created.getRepresentative().getEmail());

        verify(repository).save(any(LegalPerson.class));
    }

    @Test
    @DisplayName("Deve lançar exceção quando o CNPJ já estiver cadastrado")
    void handle_ShouldFailWhenCnpjAlreadyInUse() {
        CreateLegalPersonCommand command = new CreateLegalPersonCommand(
                "Razão Social LTDA", null, null, "12345678000195", null,
                RegistrationStatus.ACTIVE, CompanyStatus.ACTIVE, buildAddress());

        when(repository.existsByCnpj("12345678000195")).thenReturn(true);

        InternalException exception = assertThrows(InternalException.class, () -> service.handle(command));
        assertEquals("Já existe uma empresa cadastrada com este CNPJ.", exception.getMessage());

        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("Deve lançar exceção quando o CNPJ não estiver cadastrado mas a validação de domínio falhar")
    void handle_ShouldFailWhenDomainValidationFails() {
        CreateLegalPersonCommand command = new CreateLegalPersonCommand(
                "", null, null, "12345678000195", null,
                RegistrationStatus.ACTIVE, CompanyStatus.ACTIVE, buildAddress());

        InternalException exception = assertThrows(InternalException.class, () -> service.handle(command));
        assertEquals("A razão social não pode ser vazia.", exception.getMessage());

        verify(repository, never()).save(any());
    }
}
