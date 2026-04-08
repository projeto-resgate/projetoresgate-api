package com.projetoresgate.projetoresgate_api.core.physicalperson.service;

import com.projetoresgate.projetoresgate_api.core.physicalperson.domain.PhysicalPerson;
import com.projetoresgate.projetoresgate_api.core.physicalperson.domain.vo.Cpf;
import com.projetoresgate.projetoresgate_api.core.physicalperson.repository.PhysicalPersonRepository;
import com.projetoresgate.projetoresgate_api.core.physicalperson.usecase.command.SoftDeletePhysicalPersonCommand;
import com.projetoresgate.projetoresgate_api.core.user.domain.User;
import com.projetoresgate.projetoresgate_api.core.user.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("SoftDeletePhysicalPersonService - Test")
class SoftDeletePhysicalPersonServiceTest {

    @Mock
    private PhysicalPersonRepository physicalPersonRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private SoftDeletePhysicalPersonService service;

    @Test
    @DisplayName("Deve deletar pessoa física e usuário associado com sucesso")
    void handle_ShouldDeleteBothSuccessfully() {
        UUID id = UUID.randomUUID();
        User user = User.create("email@test.com", "password123", "Name", "nick");
        PhysicalPerson person = PhysicalPerson.create(user, new Cpf("51086174968"), null, null, null, null, null);
        SoftDeletePhysicalPersonCommand command = new SoftDeletePhysicalPersonCommand(id);

        when(physicalPersonRepository.findByIdOrThrow(id)).thenReturn(person);

        service.handle(command);

        verify(userRepository).delete(user);
        verify(physicalPersonRepository).delete(person);
    }
}
