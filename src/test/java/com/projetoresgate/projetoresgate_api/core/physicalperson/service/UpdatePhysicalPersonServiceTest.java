package com.projetoresgate.projetoresgate_api.core.physicalperson.service;

import com.projetoresgate.projetoresgate_api.core.physicalperson.domain.PhysicalPerson;
import com.projetoresgate.projetoresgate_api.core.physicalperson.domain.enums.Gender;
import com.projetoresgate.projetoresgate_api.core.physicalperson.domain.vo.Cpf;
import com.projetoresgate.projetoresgate_api.core.physicalperson.repository.PhysicalPersonRepository;
import com.projetoresgate.projetoresgate_api.core.physicalperson.usecase.command.UpdatePhysicalPersonCommand;
import com.projetoresgate.projetoresgate_api.core.user.domain.User;
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("UpdatePhysicalPersonService - Test")
class UpdatePhysicalPersonServiceTest {

    @Mock
    private PhysicalPersonRepository repository;

    @InjectMocks
    private UpdatePhysicalPersonService service;

    private User user;
    private PhysicalPerson person;
    private UUID personId;

    @BeforeEach
    void setUp() {
        personId = UUID.randomUUID();
        user = User.create("email@test.com", "password", "Old Name", "nick");
        person = PhysicalPerson.create(user, new Cpf("51086174968"), null, null, null, null, null);
    }

    @Test
    @DisplayName("Deve atualizar nome do usuário e todos os campos de pessoa física com sucesso")
    void handle_ShouldUpdateAllFieldsSuccessfully() {
        LocalDate birthDate = LocalDate.of(1990, 1, 1);
        UpdatePhysicalPersonCommand command = new UpdatePhysicalPersonCommand(
                personId, "New Name", "newnick", "1234567", "30274973081", birthDate, "1122223333", "11999998888", Gender.MALE);

        when(repository.findByIdOrThrow(personId)).thenReturn(person);
        when(repository.existsByCpfAndIdNot(any(Cpf.class), eq(personId))).thenReturn(false);
        when(repository.save(any())).thenReturn(person);

        service.handle(command);

        assertEquals("New Name", user.getName());
        assertEquals("newnick", user.getNickname());
        assertEquals("30274973081", person.getCpf().getValue());
        assertEquals("1234567", person.getRg().getValue());
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
        UpdatePhysicalPersonCommand command = new UpdatePhysicalPersonCommand(
                personId, null, null, null, existingCpfValue, null, null, null, null);

        when(repository.findByIdOrThrow(personId)).thenReturn(person);
        when(repository.existsByCpfAndIdNot(any(Cpf.class), eq(personId))).thenReturn(true);

        assertThrows(InternalException.class, () -> service.handle(command));
        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("Deve permitir atualizar quando o CPF informado é o mesmo já cadastrado para a pessoa")
    void handle_ShouldAllowUpdateWhenCpfIsSame() {
        UpdatePhysicalPersonCommand command = new UpdatePhysicalPersonCommand(
                personId, null, null, null, "51086174968", null, null, null, null);

        when(repository.findByIdOrThrow(personId)).thenReturn(person);
        when(repository.save(any())).thenReturn(person);

        service.handle(command);

        assertEquals("51086174968", person.getCpf().getValue());
        verify(repository, never()).existsByCpfAndIdNot(any(), any());
        verify(repository).save(person);
    }

    @Test
    @DisplayName("Não deve alterar os campos do usuário se forem nulos ou vazios no comando")
    void handle_ShouldNotUpdateUserFieldsWhenNullOrBlank() {
        UpdatePhysicalPersonCommand command = new UpdatePhysicalPersonCommand(
                personId, "", null, null, null, null, null, null, null);

        when(repository.findByIdOrThrow(personId)).thenReturn(person);
        when(repository.save(any())).thenReturn(person);

        service.handle(command);

        assertEquals("Old Name", user.getName());
        assertEquals("nick", user.getNickname());
    }
}
