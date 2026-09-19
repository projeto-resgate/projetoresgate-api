package com.projetoresgate.projetoresgate_api.core.academic.program.service;

import com.projetoresgate.projetoresgate_api.core.academic.program.api.dto.ProgramNameResponse;
import com.projetoresgate.projetoresgate_api.core.academic.program.domain.Program;
import com.projetoresgate.projetoresgate_api.core.academic.program.repository.ProgramRepository;
import com.projetoresgate.projetoresgate_api.core.academic.program.usecase.FindProgramNamesUseCase;
import com.projetoresgate.projetoresgate_api.core.academic.program.usecase.query.FindProgramNamesQuery;
import com.projetoresgate.projetoresgate_api.shared.specification.SpecificationBuilder;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class FindProgramNamesService implements FindProgramNamesUseCase {

    private final ProgramRepository repository;

    public FindProgramNamesService(ProgramRepository repository) {
        this.repository = repository;
    }

    @Override
    public List<ProgramNameResponse> handle(FindProgramNamesQuery query) {

        Specification<Program> nameFilter = new SpecificationBuilder<Program>()
                .with("name", ":", query.name())
                .build();

        return repository.findAll(nameFilter).stream()
                .map(ProgramNameResponse::fromEntity)
                .toList();
    }
}