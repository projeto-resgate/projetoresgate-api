package com.projetoresgate.projetoresgate_api.core.naturalperson.service;

import com.projetoresgate.projetoresgate_api.core.identity.naturalperson.domain.NaturalPerson;
import com.projetoresgate.projetoresgate_api.core.identity.naturalperson.repository.NaturalPersonRepository;
import com.projetoresgate.projetoresgate_api.core.identity.naturalperson.service.SoftDeleteNaturalPersonService;
import com.projetoresgate.projetoresgate_api.core.identity.naturalperson.usecase.command.SoftDeleteNaturalPersonCommand;
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

    @InjectMocks
    private SoftDeleteNaturalPersonService service;

    @Test
    @DisplayName("Deve deletar pessoa física com sucesso")
    void handle_ShouldDeleteSuccessfully() {
        UUID id = UUID.randomUUID();
        NaturalPerson person = NaturalPerson.create("Name", "email@test.com", "nick", "51086174968", null, null, null, null, null);
        SoftDeleteNaturalPersonCommand command = new SoftDeleteNaturalPersonCommand(id);

        when(naturalPersonRepository.findByIdOrThrow(id)).thenReturn(person);

        service.handle(command);

        verify(naturalPersonRepository).delete(person);
    }
}
