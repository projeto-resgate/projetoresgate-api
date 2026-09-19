package com.projetoresgate.projetoresgate_api.core.academic.program.service;

import com.projetoresgate.projetoresgate_api.core.academic.program.api.dto.EducatorCategoryItemNameResponse;
import com.projetoresgate.projetoresgate_api.core.academic.program.domain.EducatorCategoryItem;
import com.projetoresgate.projetoresgate_api.core.academic.program.repository.EducatorCategoryItemRepository;
import com.projetoresgate.projetoresgate_api.core.academic.program.repository.ProgramRepository;
import com.projetoresgate.projetoresgate_api.core.academic.program.usecase.FindEducatorCategoryItemNamesUseCase;
import com.projetoresgate.projetoresgate_api.core.academic.program.usecase.query.FindEducatorCategoryItemNamesQuery;
import com.projetoresgate.projetoresgate_api.shared.specification.SpecificationBuilder;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class FindEducatorCategoryItemNamesService implements FindEducatorCategoryItemNamesUseCase {

    private final ProgramRepository programRepository;
    private final EducatorCategoryItemRepository itemRepository;

    public FindEducatorCategoryItemNamesService(ProgramRepository programRepository,
                                                EducatorCategoryItemRepository itemRepository) {
        this.programRepository = programRepository;
        this.itemRepository = itemRepository;
    }

    @Override
    public List<EducatorCategoryItemNameResponse> handle(FindEducatorCategoryItemNamesQuery query) {
        programRepository.findByIdOrThrow(query.programId());

        Specification<EducatorCategoryItem> filter = new SpecificationBuilder<EducatorCategoryItem>()
                .with("program.id", ":", query.programId())
                .with("name", ":", query.name())
                .build();

        return itemRepository.findAll(filter).stream()
                .map(EducatorCategoryItemNameResponse::fromEntity)
                .toList();
    }
}