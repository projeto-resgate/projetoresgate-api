package com.projetoresgate.projetoresgate_api.core.academic.program.service;

import com.projetoresgate.projetoresgate_api.core.academic.program.domain.Program;
import com.projetoresgate.projetoresgate_api.core.academic.program.repository.ProgramRepository;
import com.projetoresgate.projetoresgate_api.core.academic.program.usecase.SoftDeleteProgramUseCase;
import com.projetoresgate.projetoresgate_api.core.academic.program.usecase.command.SoftDeleteProgramCommand;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SoftDeleteProgramService implements SoftDeleteProgramUseCase {

    private final ProgramRepository repository;

    public SoftDeleteProgramService(ProgramRepository repository) {
        this.repository = repository;
    }

    @Override
    @Transactional
    public void handle(SoftDeleteProgramCommand command) {
        Program program = repository.findByIdOrThrow(command.id());
        repository.delete(program);
    }
}