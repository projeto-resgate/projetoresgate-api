package com.projetoresgate.projetoresgate_api.core.naturalperson.service;

import com.projetoresgate.projetoresgate_api.core.identity.naturalperson.domain.NaturalPerson;
import com.projetoresgate.projetoresgate_api.core.identity.naturalperson.repository.NaturalPersonRepository;
import com.projetoresgate.projetoresgate_api.core.identity.naturalperson.service.SoftDeleteNaturalPersonService;
import com.projetoresgate.projetoresgate_api.core.identity.naturalperson.usecase.command.SoftDeleteNaturalPersonCommand;
import com.projetoresgate.projetoresgate_api.core.identity.user.domain.User;
import com.projetoresgate.projetoresgate_api.core.identity.user.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("SoftDeleteNaturalPersonService - Test")
class SoftDeleteNaturalPersonServiceTest {

    @Mock
    private NaturalPersonRepository naturalPersonRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private SoftDeleteNaturalPersonService service;

    @Test
    @DisplayName("Deve deletar pessoa física e usuário associado com sucesso")
    void handle_ShouldDeleteBothSuccessfully() {
        UUID id = UUID.randomUUID();
        User user = User.create("email@test.com", "password123", "Name", "nick");
        NaturalPerson person = NaturalPerson.create(user, "51086174968", null, null, null, null, null);
        SoftDeleteNaturalPersonCommand command = new SoftDeleteNaturalPersonCommand(id);

        when(naturalPersonRepository.findByIdOrThrow(id)).thenReturn(person);

        service.handle(command);

        verify(userRepository).delete(user);
        verify(naturalPersonRepository).delete(person);
    }
}