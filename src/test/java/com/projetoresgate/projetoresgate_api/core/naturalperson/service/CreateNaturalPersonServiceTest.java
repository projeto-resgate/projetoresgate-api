package com.projetoresgate.projetoresgate_api.core.naturalperson.service;

import com.projetoresgate.projetoresgate_api.core.identity.naturalperson.domain.NaturalPerson;
import com.projetoresgate.projetoresgate_api.core.identity.naturalperson.repository.NaturalPersonRepository;
import com.projetoresgate.projetoresgate_api.core.identity.naturalperson.service.CreateNaturalPersonService;
import com.projetoresgate.projetoresgate_api.core.identity.naturalperson.usecase.RequestEmailConfirmationUseCase;
import com.projetoresgate.projetoresgate_api.core.identity.naturalperson.usecase.command.CreateNaturalPersonCommand;
import com.projetoresgate.projetoresgate_api.infrastructure.exception.InternalException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;

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

    @InjectMocks
    private CreateNaturalPersonService service;

    @Test
    @DisplayName("Deve criar pessoa física com sucesso e disparar confirmação de e-mail")
    void handle_ShouldCreateSuccessfully() {
        CreateNaturalPersonCommand command = new CreateNaturalPersonCommand(
                "Test Name", "test@test.com", "Test Nickname", "1234567", "51086174968", LocalDate.now(), "11999999999", "11988888888", null);

        when(naturalPersonRepository.save(any(NaturalPerson.class))).thenAnswer(i -> i.getArgument(0));

        NaturalPerson created = service.handle(command);

        assertNotNull(created);
        assertNotNull(created.getId());
        assertEquals("Test Name", created.getName());
        assertEquals("test@test.com", created.getEmail());
        assertEquals("Test Nickname", created.getNickname());
        assertEquals("51086174968", created.getCpf());

        verify(naturalPersonRepository).save(any(NaturalPerson.class));
        verify(requestEmailConfirmationUseCase).handle("test@test.com");
    }

    @Test
    @DisplayName("Deve lançar exceção quando o CPF já estiver cadastrado")
    void handle_ShouldFailWhenCpfAlreadyInUse() {
        CreateNaturalPersonCommand command = new CreateNaturalPersonCommand(
                "Test Name", "test@test.com", "Test Nickname", "1234567", "51086174968", LocalDate.now(), "11999999999", "11988888888", null);

        when(naturalPersonRepository.existsByCpf(anyString())).thenReturn(true);

        assertThrows(InternalException.class, () -> service.handle(command));

        verify(naturalPersonRepository, never()).save(any());
        verify(requestEmailConfirmationUseCase, never()).handle(anyString());
    }
}
