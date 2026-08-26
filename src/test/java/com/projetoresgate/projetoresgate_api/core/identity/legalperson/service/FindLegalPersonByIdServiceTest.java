package com.projetoresgate.projetoresgate_api.core.identity.legalperson.service;

import com.projetoresgate.projetoresgate_api.core.identity.address.domain.Address;
import com.projetoresgate.projetoresgate_api.core.identity.legalperson.domain.LegalPerson;
import com.projetoresgate.projetoresgate_api.core.identity.legalperson.domain.Representative;
import com.projetoresgate.projetoresgate_api.core.identity.legalperson.domain.enums.CompanyStatus;
import com.projetoresgate.projetoresgate_api.core.identity.legalperson.domain.enums.RegistrationStatus;
import com.projetoresgate.projetoresgate_api.core.identity.legalperson.repository.LegalPersonRepository;
import com.projetoresgate.projetoresgate_api.core.identity.legalperson.usecase.query.FindLegalPersonByIdQuery;
import com.projetoresgate.projetoresgate_api.infrastructure.exception.ResourceNotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("FindLegalPersonByIdService - Test")
class FindLegalPersonByIdServiceTest {

    @Mock
    private LegalPersonRepository repository;

    @InjectMocks
    private FindLegalPersonByIdService service;

    @Test
    @DisplayName("Deve encontrar pessoa jurídica por ID com sucesso e retornar todos os campos")
    void handle_ShouldFindByIdSuccessfully() {
        UUID id = UUID.randomUUID();
        LocalDateTime dateCreated = LocalDateTime.of(2025, 6, 15, 14, 30, 0);

        Address address = Address.create("01310-100", "1000", "Apto 101", "Bela Vista", "São Paulo", "SP");
        Representative representative = Representative.create("John Doe", "11988887777", "1133334444", "john@doe.com");

        LegalPerson person = LegalPerson.create(
                "12345678000195", "Razão Social LTDA", "Nome Fantasia", "Display Name", "6201-5/00",
                RegistrationStatus.ACTIVE, CompanyStatus.ACTIVE, address, representative);
        person.setDateCreated(dateCreated);

        FindLegalPersonByIdQuery query = new FindLegalPersonByIdQuery(id);
        when(repository.findByIdOrThrow(id)).thenReturn(person);

        LegalPerson found = service.handle(query);

        assertNotNull(found);
        assertEquals("12345678000195", found.getCnpj());
        assertEquals("Razão Social LTDA", found.getCorporateName());
        assertEquals("Nome Fantasia", found.getTradeName());
        assertEquals("Display Name", found.getDisplayName());
        assertEquals("6201-5/00", found.getMainCnaeCode());
        assertEquals(RegistrationStatus.ACTIVE, found.getRegistrationStatus());
        assertEquals(CompanyStatus.ACTIVE, found.getCompanyStatus());
        assertEquals("São Paulo", found.getAddress().getCity());
        assertEquals("01310-100", found.getAddress().getZipCode());
        assertEquals("John Doe", found.getRepresentative().getName());
        assertEquals("john@doe.com", found.getRepresentative().getEmail());
        assertEquals(dateCreated, found.getDateCreated());
        verify(repository).findByIdOrThrow(id);
    }

    @Test
    @DisplayName("Deve lançar exceção quando pessoa jurídica não encontrada por ID")
    void handle_ShouldThrowExceptionWhenNotFound() {
        UUID id = UUID.randomUUID();
        FindLegalPersonByIdQuery query = new FindLegalPersonByIdQuery(id);

        when(repository.findByIdOrThrow(id)).thenThrow(new ResourceNotFoundException("Pessoa jurídica não encontrada com ID: " + id));

        assertThrows(ResourceNotFoundException.class, () -> service.handle(query));
        verify(repository).findByIdOrThrow(id);
    }
}
