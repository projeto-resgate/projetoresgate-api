package com.projetoresgate.projetoresgate_api.core.identity.naturalperson.service;

import com.projetoresgate.projetoresgate_api.core.identity.legalperson.repository.LegalPersonRepository;
import com.projetoresgate.projetoresgate_api.core.identity.naturalperson.domain.NaturalPerson;
import com.projetoresgate.projetoresgate_api.core.identity.naturalperson.repository.NaturalPersonRepository;
import com.projetoresgate.projetoresgate_api.core.identity.naturalperson.usecase.RequestEmailConfirmationUseCase;
import com.projetoresgate.projetoresgate_api.core.identity.naturalperson.usecase.command.CreateNaturalPersonCommand;
import com.projetoresgate.projetoresgate_api.infrastructure.exception.InternalException;
import com.projetoresgate.projetoresgate_api.infrastructure.exception.ResourceNotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("CreateNaturalPersonService - Test")
class CreateNaturalPersonServiceTest {

    @Mock
    private NaturalPersonRepository naturalPersonRepository;

    @Mock
    private RequestEmailConfirmationUseCase requestEmailConfirmationUseCase;

    @Mock
    private LegalPersonRepository legalPersonRepository;

    @InjectMocks
    private CreateNaturalPersonService service;

    @Test
    @DisplayName("Deve criar pessoa física com sucesso e disparar confirmação de e-mail")
    void handle_ShouldCreateSuccessfully() {
        CreateNaturalPersonCommand command = new CreateNaturalPersonCommand(
                "Test Name", "test@test.com", "Test Nickname", "1234567", "51086174968", LocalDate.now(), "11999999999", "11988888888", null, null);

        when(naturalPersonRepository.save(any(NaturalPerson.class))).thenAnswer(i -> i.getArgument(0));

        NaturalPerson created = service.handle(command);

        assertNotNull(created);
        assertNotNull(created.getId());
        assertEquals("Test Name", created.getName());
        assertEquals("test@test.com", created.getEmail());
        assertEquals("Test Nickname", created.getNickname());
        assertEquals("51086174968", created.getCpf());

        verify(naturalPersonRepository).save(any(NaturalPerson.class));
        verify(requestEmailConfirmationUseCase).handle(created);
    }

    @Test
    @DisplayName("Deve lançar exceção quando o CPF já estiver cadastrado")
    void handle_ShouldFailWhenCpfAlreadyInUse() {
        CreateNaturalPersonCommand command = new CreateNaturalPersonCommand(
                "Test Name", "test@test.com", "Test Nickname", "1234567", "51086174968", LocalDate.now(), "11999999999", "11988888888", null, null);

        when(naturalPersonRepository.existsByCpf(anyString())).thenReturn(true);

        assertThrows(InternalException.class, () -> service.handle(command));

        verify(naturalPersonRepository, never()).save(any());
        verify(requestEmailConfirmationUseCase, never()).handle(any(NaturalPerson.class));
    }

    @Test
    @DisplayName("Deve gravar o local de trabalho quando a pessoa jurídica existe")
    void handle_ShouldSaveWorkplace() {
        UUID workplaceId = UUID.randomUUID();
        CreateNaturalPersonCommand command = new CreateNaturalPersonCommand(
                "Test Name", "test@test.com", null, null, null, null, null, null, null, workplaceId);

        when(legalPersonRepository.findByIdOrThrow(workplaceId)).thenReturn(null);
        when(naturalPersonRepository.save(any(NaturalPerson.class))).thenAnswer(i -> i.getArgument(0));

        NaturalPerson created = service.handle(command);

        assertEquals(workplaceId, created.getWorkplaceId());
        verify(legalPersonRepository).findByIdOrThrow(workplaceId);
    }

    @Test
    @DisplayName("Não deve salvar quando o local de trabalho não existe")
    void handle_ShouldFailWhenWorkplaceNotFound() {
        UUID workplaceId = UUID.randomUUID();
        CreateNaturalPersonCommand command = new CreateNaturalPersonCommand(
                "Test Name", "test@test.com", null, null, null, null, null, null, null, workplaceId);

        when(legalPersonRepository.findByIdOrThrow(workplaceId))
                .thenThrow(new ResourceNotFoundException("não encontrada"));

        assertThrows(ResourceNotFoundException.class,
                () -> service.handle(command));

        verify(naturalPersonRepository, never()).save(any());
    }

    @Test
    @DisplayName("Sem local de trabalho não consulta pessoas jurídicas")
    void handle_ShouldNotLookUpWorkplaceWhenNotSet() {
        CreateNaturalPersonCommand command = new CreateNaturalPersonCommand(
                "Test Name", "test@test.com", null, null, null, null, null, null, null, null);

        when(naturalPersonRepository.save(any(NaturalPerson.class))).thenAnswer(i -> i.getArgument(0));

        NaturalPerson created = service.handle(command);

        assertNull(created.getWorkplaceId());
        verifyNoInteractions(legalPersonRepository);
    }

    @Test
    @DisplayName("Deve lançar exceção quando o RG já estiver cadastrado")
    void handle_ShouldFailWhenRgAlreadyInUse() {
        CreateNaturalPersonCommand command = new CreateNaturalPersonCommand(
                "Test Name", "test@test.com", null, "1234567", null, null, null, null, null, null);

        when(naturalPersonRepository.existsByRg("1234567")).thenReturn(true);

        InternalException exception = assertThrows(InternalException.class, () -> service.handle(command));

        assertEquals("Já existe uma pessoa cadastrada com este RG.", exception.getMessage());
        verify(naturalPersonRepository, never()).save(any());
    }
}
