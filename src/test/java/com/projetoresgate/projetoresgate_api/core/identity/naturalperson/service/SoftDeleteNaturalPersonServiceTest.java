package com.projetoresgate.projetoresgate_api.core.identity.naturalperson.service;

import com.projetoresgate.projetoresgate_api.core.identity.naturalperson.domain.NaturalPerson;
import com.projetoresgate.projetoresgate_api.core.identity.naturalperson.repository.NaturalPersonRepository;
import com.projetoresgate.projetoresgate_api.core.identity.naturalperson.usecase.command.SoftDeleteNaturalPersonCommand;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.mockito.Mockito.never;
import static org.mockito.Mockito.mock;
import static org.mockito.ArgumentMatchers.any;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertEquals;
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
        when(naturalPersonRepository.countDependencies(person.getId()))
                .thenReturn(mock(NaturalPersonRepository.NaturalPersonDependencies.class));

        service.handle(command);

        verify(naturalPersonRepository).delete(person);
    }

    @Test
    @DisplayName("Não deve excluir a pessoa física que é educando, responsável, colaborador ou membro de família")
    void handle_ShouldBlockWhenNaturalPersonIsInUse() {
        UUID id = UUID.randomUUID();
        NaturalPerson person = NaturalPerson.create("Name", "email@test.com", "nick", "51086174968", null, null, null, null, null);
        NaturalPersonRepository.NaturalPersonDependencies dependencies = mock(NaturalPersonRepository.NaturalPersonDependencies.class);
        when(dependencies.getLearners()).thenReturn(1L);
        when(dependencies.getResponsibilities()).thenReturn(2L);
        when(dependencies.getCollaborators()).thenReturn(1L);
        when(dependencies.getFamilyGroups()).thenReturn(1L);
        when(naturalPersonRepository.findByIdOrThrow(id)).thenReturn(person);
        when(naturalPersonRepository.countDependencies(person.getId())).thenReturn(dependencies);

        IllegalStateException exception = assertThrows(IllegalStateException.class,
                () -> service.handle(new SoftDeleteNaturalPersonCommand(id)));

        assertEquals("Não é possível excluir a pessoa física porque existem vínculos com: 1 cadastro de educando, "
                + "2 educandos como responsável, 1 cadastro de colaborador e 1 grupo familiar. "
                + "Remova os vínculos antes de excluir.", exception.getMessage());
        verify(naturalPersonRepository, never()).delete(any(NaturalPerson.class));
    }
}
