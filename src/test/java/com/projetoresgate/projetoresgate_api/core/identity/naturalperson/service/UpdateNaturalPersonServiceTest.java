package com.projetoresgate.projetoresgate_api.core.identity.naturalperson.service;

import com.projetoresgate.projetoresgate_api.core.identity.legalperson.repository.LegalPersonRepository;
import com.projetoresgate.projetoresgate_api.core.identity.naturalperson.domain.NaturalPerson;
import com.projetoresgate.projetoresgate_api.core.identity.naturalperson.domain.enums.Gender;
import com.projetoresgate.projetoresgate_api.core.identity.naturalperson.repository.EmailConfirmationTokenRepository;
import com.projetoresgate.projetoresgate_api.core.identity.naturalperson.repository.NaturalPersonRepository;
import com.projetoresgate.projetoresgate_api.core.identity.naturalperson.usecase.RequestEmailConfirmationUseCase;
import com.projetoresgate.projetoresgate_api.core.identity.naturalperson.usecase.command.UpdateNaturalPersonCommand;
import com.projetoresgate.projetoresgate_api.infrastructure.exception.InternalException;
import com.projetoresgate.projetoresgate_api.infrastructure.exception.ResourceNotFoundException;
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

    @Mock
    private LegalPersonRepository legalPersonRepository;

    @Mock
    private RequestEmailConfirmationUseCase requestEmailConfirmationUseCase;

    @Mock
    private EmailConfirmationTokenRepository emailConfirmationTokenRepository;

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
                personId, "New Name", "new@test.com", "newnick", "1234567", "30274973081", birthDate, "1122223333", "11999998888", Gender.MALE, null);

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
                personId, "Old Name", "email@test.com", "nick", null, existingCpfValue, null, null, null, null, null);

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
                personId, "Old Name", "email@test.com", "nick", null, "51086174968", null, null, null, null, null);

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
                personId, "", "email@test.com", "nick", null, "51086174968", null, null, null, null, null);

        when(repository.findByIdOrThrow(personId)).thenReturn(person);

        InternalException exception = assertThrows(InternalException.class, () -> service.handle(command));
        assertEquals("O nome não pode ser vazio.", exception.getMessage());

        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("Deve atualizar campos não obrigatórios (como CPF, RG e Nickname) para null com sucesso")
    void handle_ShouldUpdateNonMandatoryFieldsToNull() {
        UpdateNaturalPersonCommand command = new UpdateNaturalPersonCommand(
                personId, "Old Name", "email@test.com", null, null, null, null, null, null, null, null);

        when(repository.findByIdOrThrow(personId)).thenReturn(person);
        when(repository.save(any())).thenReturn(person);

        service.handle(command);

        assertNull(person.getNickname());
        assertNull(person.getCpf());
        assertNull(person.getRg());
        verify(repository).save(person);
    }

    @Test
    @DisplayName("Deve alterar o local de trabalho validando a nova pessoa jurídica")
    void handle_ShouldChangeWorkplace() {
        UUID workplaceId = UUID.randomUUID();
        UpdateNaturalPersonCommand command = new UpdateNaturalPersonCommand(
                personId, "Old Name", "email@test.com", null, null, null, null, null, null, null, workplaceId);

        when(repository.findByIdOrThrow(personId)).thenReturn(person);
        when(legalPersonRepository.findByIdOrThrow(workplaceId)).thenReturn(null);
        when(repository.save(any())).thenAnswer(i -> i.getArgument(0));

        NaturalPerson result = service.handle(command);

        assertEquals(workplaceId, result.getWorkplaceId());
        verify(legalPersonRepository).findByIdOrThrow(workplaceId);
    }

    @Test
    @DisplayName("Deve remover o local de trabalho quando vier nulo")
    void handle_ShouldClearWorkplace() {
        UUID workplaceId = UUID.randomUUID();
        person.update().workplaceId(workplaceId).apply();
        UpdateNaturalPersonCommand command = new UpdateNaturalPersonCommand(
                personId, "Old Name", "email@test.com", null, null, null, null, null, null, null, null);

        when(repository.findByIdOrThrow(personId)).thenReturn(person);
        when(repository.save(any())).thenAnswer(i -> i.getArgument(0));

        assertNull(service.handle(command).getWorkplaceId());
        verifyNoInteractions(legalPersonRepository);
    }

    @Test
    @DisplayName("Não deve revalidar o local de trabalho que não mudou")
    void handle_ShouldNotRevalidateUnchangedWorkplace() {
        UUID workplaceId = UUID.randomUUID();
        person.update().workplaceId(workplaceId).apply();
        UpdateNaturalPersonCommand command = new UpdateNaturalPersonCommand(
                personId, "Old Name", "email@test.com", null, null, null, null, null, null, null, workplaceId);

        when(repository.findByIdOrThrow(personId)).thenReturn(person);
        when(repository.save(any())).thenAnswer(i -> i.getArgument(0));

        service.handle(command);

        verifyNoInteractions(legalPersonRepository);
    }

    @Test
    @DisplayName("Não deve salvar quando o novo local de trabalho não existe")
    void handle_ShouldFailWhenWorkplaceNotFound() {
        UUID workplaceId = UUID.randomUUID();
        UpdateNaturalPersonCommand command = new UpdateNaturalPersonCommand(
                personId, "Old Name", "email@test.com", null, null, null, null, null, null, null, workplaceId);

        when(repository.findByIdOrThrow(personId)).thenReturn(person);
        when(legalPersonRepository.findByIdOrThrow(workplaceId))
                .thenThrow(new ResourceNotFoundException("não encontrada"));

        assertThrows(ResourceNotFoundException.class,
                () -> service.handle(command));

        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("Trocar o e-mail deve desfazer a verificação e enviar nova confirmação")
    void handle_ShouldResetVerificationWhenEmailChanges() {
        person.confirmEmail();
        UpdateNaturalPersonCommand command = new UpdateNaturalPersonCommand(
                personId, "Old Name", "novo@test.com", "nick", null, null, null, null, null, null, null);

        when(repository.findByIdOrThrow(personId)).thenReturn(person);
        when(repository.save(any())).thenReturn(person);

        service.handle(command);

        assertFalse(person.isEmailVerified());
        verify(requestEmailConfirmationUseCase).handle(person);
    }

    @Test
    @DisplayName("Tirar o e-mail deve desfazer a verificação e apagar os links de confirmação pendentes")
    void handle_ShouldDiscardPendingConfirmationsWhenEmailIsRemoved() {
        person.confirmEmail();
        UpdateNaturalPersonCommand command = new UpdateNaturalPersonCommand(
                personId, "Old Name", null, "nick", null, null, null, null, null, null, null);

        when(repository.findByIdOrThrow(personId)).thenReturn(person);
        when(repository.save(any())).thenReturn(person);

        service.handle(command);

        assertNull(person.getEmail());
        assertFalse(person.isEmailVerified());
        verify(emailConfirmationTokenRepository).deleteByNaturalPerson(person);
        verify(requestEmailConfirmationUseCase, never()).handle(any(NaturalPerson.class));
    }

    @Test
    @DisplayName("Manter o e-mail, mesmo com outra caixa, não deve desfazer a verificação")
    void handle_ShouldKeepVerificationWhenEmailIsTheSame() {
        person.confirmEmail();
        UpdateNaturalPersonCommand command = new UpdateNaturalPersonCommand(
                personId, "New Name", "EMAIL@test.com", "nick", null, null, null, null, null, null, null);

        when(repository.findByIdOrThrow(personId)).thenReturn(person);
        when(repository.save(any())).thenReturn(person);

        service.handle(command);

        assertTrue(person.isEmailVerified());
        verify(requestEmailConfirmationUseCase, never()).handle(any(NaturalPerson.class));
    }

    @Test
    @DisplayName("Deve lançar exceção quando o RG pertencer a outra pessoa")
    void handle_ShouldFailWhenRgExists() {
        UpdateNaturalPersonCommand command = new UpdateNaturalPersonCommand(
                personId, "Old Name", "email@test.com", "nick", "1234567", null, null, null, null, null, null);

        when(repository.findByIdOrThrow(personId)).thenReturn(person);
        when(repository.existsByRgAndIdNot("1234567", personId)).thenReturn(true);

        assertThrows(InternalException.class, () -> service.handle(command));
        verify(repository, never()).save(any());
    }
}
