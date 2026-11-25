package com.projetoresgate.projetoresgate_api.core.academic.classgroup.service;

import com.projetoresgate.projetoresgate_api.core.academic.classgroup.api.dto.ClassCollaboratorResponse;
import com.projetoresgate.projetoresgate_api.core.academic.classgroup.repository.ClassCollaboratorRepository;
import com.projetoresgate.projetoresgate_api.core.academic.classgroup.repository.ClassGroupRepository;
import com.projetoresgate.projetoresgate_api.core.academic.classgroup.usecase.FindAllClassCollaboratorsUseCase;
import com.projetoresgate.projetoresgate_api.core.academic.classgroup.usecase.query.FindAllClassCollaboratorsQuery;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
public class FindAllClassCollaboratorsService implements FindAllClassCollaboratorsUseCase {

    private final ClassCollaboratorRepository repository;
    private final ClassGroupRepository classGroupRepository;

    public FindAllClassCollaboratorsService(ClassCollaboratorRepository repository,
                                            ClassGroupRepository classGroupRepository) {
        this.repository = repository;
        this.classGroupRepository = classGroupRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ClassCollaboratorResponse> handle(FindAllClassCollaboratorsQuery query) {
        classGroupRepository.findByIdOrThrow(query.classGroupId());

        String searchTerm = StringUtils.hasText(query.searchTerm()) ? query.searchTerm() : null;

        return repository.findRowsByClassGroupId(query.classGroupId(), query.active(),
                        query.educatorCategoryItemId(), searchTerm,
                        PageRequest.of(query.pageable().getPageNumber(), query.pageable().getPageSize()))
                .map(ClassCollaboratorResponse::fromRow);
    }
}
