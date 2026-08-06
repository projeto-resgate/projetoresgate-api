package com.projetoresgate.projetoresgate_api.core.naturalperson.service;

import com.projetoresgate.projetoresgate_api.core.identity.naturalperson.domain.NaturalPerson;
import com.projetoresgate.projetoresgate_api.core.identity.naturalperson.domain.enums.Gender;
import com.projetoresgate.projetoresgate_api.core.identity.naturalperson.repository.NaturalPersonRepository;
import com.projetoresgate.projetoresgate_api.core.identity.naturalperson.service.UpdateNaturalPersonService;
import com.projetoresgate.projetoresgate_api.core.identity.naturalperson.usecase.command.UpdateNaturalPersonCommand;
import com.projetoresgate.projetoresgate_api.infrastructure.exception.InternalException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("UpdateNaturalPersonService - Test")
class UpdateNaturalPersonServiceTest {

    @Mock
    private NaturalPersonRepository repository;

    @InjectMocks
    private UpdateNaturalPersonService service;

    private NaturalPerson person;
    private UUID personId;

    @BeforeEach
    void setUp() {
        person = NaturalPerson.create("Old Name", "email@test.com", "nick", "51086174968", null, null, null, null, null);
        personId = person.getId();
    }

    @Test
    @DisplayName("Deve atualizar nome, e-mail, nickname e todos os campos de pessoa física com sucesso")
    void handle_ShouldUpdateAllFieldsSuccessfully() {
        LocalDate birthDate = LocalDate.of(1990, 1, 1);
        UpdateNaturalPersonCommand command = new UpdateNaturalPersonCommand(
                personId, "New Name", "new@test.com", "newnick", "1234567", "30274973081", birthDate, "1122223333", "11999998888", Gender.MALE);

        when(repository.findByIdOrThrow(personId)).thenReturn(person);
        when(repository.existsByCpfAndIdNot(anyString(), eq(personId))).thenReturn(false);
        when(repository.save(any())).thenReturn(person);

        service.handle(command);

        assertEquals("New Name", person.getName());
        assertEquals("new@test.com", person.getEmail());
        assertEquals("newnick", person.getNickname());
        assertEquals("30274973081", person.getCpf());
        assertEquals("1234567", person.getRg());
        assertEquals(birthDate, person.getBirthDate());
        assertEquals(Gender.MALE, person.getGender());
        assertEquals("1122223333", person.getPhone());
        assertEquals("11999998888", person.getCellphone());
        verify(repository).save(person);
    }

    @Test
    @DisplayName("Deve lançar exceção ao tentar atualizar com CPF já existente em outra pessoa")
    void handle_ShouldFailWhenCpfExists() {
        String existingCpfValue = "12345678909";
        UpdateNaturalPersonCommand command = new UpdateNaturalPersonCommand(
                personId, "Old Name", "email@test.com", "nick", null, existingCpfValue, null, null, null, null);

        when(repository.findByIdOrThrow(personId)).thenReturn(person);
        when(repository.existsByCpfAndIdNot(anyString(), eq(personId))).thenReturn(true);

        InternalException exception = assertThrows(InternalException.class, () -> service.handle(command));
        assertEquals("Já existe uma pessoa cadastrada com este CPF.", exception.getMessage());

        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("Deve permitir atualizar quando o CPF informado é o mesmo já cadastrado para a pessoa")
    void handle_ShouldAllowUpdateWhenCpfIsSame() {
        UpdateNaturalPersonCommand command = new UpdateNaturalPersonCommand(
                personId, "Old Name", "email@test.com", "nick", null, "51086174968", null, null, null, null);

        when(repository.findByIdOrThrow(personId)).thenReturn(person);
        when(repository.existsByCpfAndIdNot(anyString(), eq(personId))).thenReturn(false);
        when(repository.save(any())).thenReturn(person);

        service.handle(command);

        assertEquals("51086174968", person.getCpf());
        verify(repository).existsByCpfAndIdNot("51086174968", personId);
        verify(repository).save(person);
    }

    @Test
    @DisplayName("Deve lançar exceção na validação de domínio se o nome for apagado no comando")
    void handle_ShouldThrowExceptionWhenNameIsBlanked() {
        UpdateNaturalPersonCommand command = new UpdateNaturalPersonCommand(
                personId, "", "email@test.com", "nick", null, "51086174968", null, null, null, null);

        when(repository.findByIdOrThrow(personId)).thenReturn(person);

        InternalException exception = assertThrows(InternalException.class, () -> service.handle(command));
        assertEquals("O nome não pode ser vazio.", exception.getMessage());

        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("Deve atualizar campos não obrigatórios (como CPF, RG e Nickname) para null com sucesso")
    void handle_ShouldUpdateNonMandatoryFieldsToNull() {
        UpdateNaturalPersonCommand command = new UpdateNaturalPersonCommand(
                personId, "Old Name", "email@test.com", null, null, null, null, null, null, null);

        when(repository.findByIdOrThrow(personId)).thenReturn(person);
        when(repository.save(any())).thenReturn(person);

        service.handle(command);

        assertNull(person.getNickname());
        assertNull(person.getCpf());
        assertNull(person.getRg());
        verify(repository).save(person);
    }
}
