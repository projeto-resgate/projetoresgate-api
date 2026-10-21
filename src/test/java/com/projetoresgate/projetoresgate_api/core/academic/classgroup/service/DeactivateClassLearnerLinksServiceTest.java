package com.projetoresgate.projetoresgate_api.core.academic.classgroup.service;

import com.projetoresgate.projetoresgate_api.core.academic.classgroup.repository.ClassLearnerRepository;
import com.projetoresgate.projetoresgate_api.core.academic.classgroup.usecase.command.DeactivateClassLearnerLinksCommand;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("DeactivateClassLearnerLinksService - Test")
class DeactivateClassLearnerLinksServiceTest {

    @Mock
    private ClassLearnerRepository repository;

    @InjectMocks
    private DeactivateClassLearnerLinksService service;

    @Test
    @DisplayName("Deve inativar os vínculos ativos do educando e devolver a quantidade")
    void handle_ShouldDeactivateLinksAndReturnCount() {
        UUID learnerId = UUID.randomUUID();
        when(repository.deactivateByLearnerId(learnerId)).thenReturn(3);

        int result = service.handle(new DeactivateClassLearnerLinksCommand(learnerId));

        assertEquals(3, result);
        verify(repository).deactivateByLearnerId(learnerId);
    }
}
