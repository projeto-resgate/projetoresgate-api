package com.projetoresgate.projetoresgate_api.core.physicalperson.service;

import com.projetoresgate.projetoresgate_api.core.physicalperson.domain.PhysicalPerson;
import com.projetoresgate.projetoresgate_api.core.physicalperson.domain.vo.Cpf;
import com.projetoresgate.projetoresgate_api.core.physicalperson.repository.PhysicalPersonRepository;
import com.projetoresgate.projetoresgate_api.core.physicalperson.usecase.command.CreatePhysicalPersonCommand;
import com.projetoresgate.projetoresgate_api.core.user.domain.User;
import com.projetoresgate.projetoresgate_api.core.user.repository.UserRepository;
import com.projetoresgate.projetoresgate_api.core.user.usecase.CreateUserUseCase;
import com.projetoresgate.projetoresgate_api.core.user.usecase.command.CreateUserCommand;
import com.projetoresgate.projetoresgate_api.infrastructure.exception.InternalException;
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
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("CreatePhysicalPersonService - Test")
class CreatePhysicalPersonServiceTest {

    @Mock
    private PhysicalPersonRepository physicalPersonRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private CreateUserUseCase createUserUseCase;

    @InjectMocks
    private CreatePhysicalPersonService service;

    @Test
    @DisplayName("Deve criar pessoa física e usuário associado com sucesso")
    void handle_ShouldCreateSuccessfully() {
        CreatePhysicalPersonCommand command = new CreatePhysicalPersonCommand(
                "Test Name", "test@test.com", "Test Nickname", "1234567", "51086174968", LocalDate.now(), "11999999999", "11988888888", null);

        User newUser = User.create(command.email(), "password123", command.name(), command.nickname());
        newUser.setId(UUID.randomUUID());

        when(createUserUseCase.handle(any(CreateUserCommand.class))).thenReturn(newUser);

        when(physicalPersonRepository.save(any(PhysicalPerson.class))).thenAnswer(i -> {
            PhysicalPerson p = i.getArgument(0);
            p.setId(UUID.randomUUID());
            return p;
        });

        PhysicalPerson created = service.handle(command);

        assertNotNull(created);
        assertNotNull(created.getId());
        assertEquals(newUser, created.getUser());
        assertEquals("51086174968", created.getCpf().getValue());

        verify(createUserUseCase).handle(any(CreateUserCommand.class));
        verify(userRepository).save(newUser);
        verify(physicalPersonRepository).save(any(PhysicalPerson.class));
    }

    @Test
    @DisplayName("Deve lançar exceção quando o CPF já estiver cadastrado")
    void handle_ShouldFailWhenCpfAlreadyInUse() {
        CreatePhysicalPersonCommand command = new CreatePhysicalPersonCommand(
                "Test Name", "test@test.com", "Test Nickname", "1234567", "51086174968", LocalDate.now(), "11999999999", "11988888888", null);

        when(physicalPersonRepository.existsByCpf(any(Cpf.class))).thenReturn(true);

        assertThrows(InternalException.class, () -> service.handle(command));

        verify(createUserUseCase, never()).handle(any());
        verify(physicalPersonRepository, never()).save(any());
    }
}
