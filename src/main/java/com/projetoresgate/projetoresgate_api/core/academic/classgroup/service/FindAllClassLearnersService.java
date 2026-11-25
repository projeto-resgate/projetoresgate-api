package com.projetoresgate.projetoresgate_api.core.academic.classgroup.service;

import com.projetoresgate.projetoresgate_api.core.academic.classgroup.api.dto.ClassLearnerResponse;
import com.projetoresgate.projetoresgate_api.core.academic.classgroup.repository.ClassGroupRepository;
import com.projetoresgate.projetoresgate_api.core.academic.classgroup.repository.ClassLearnerRepository;
import com.projetoresgate.projetoresgate_api.core.academic.classgroup.usecase.FindAllClassLearnersUseCase;
import com.projetoresgate.projetoresgate_api.core.academic.classgroup.usecase.query.FindAllClassLearnersQuery;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
public class FindAllClassLearnersService implements FindAllClassLearnersUseCase {

    private final ClassLearnerRepository repository;
    private final ClassGroupRepository classGroupRepository;

    public FindAllClassLearnersService(ClassLearnerRepository repository,
                                       ClassGroupRepository classGroupRepository) {
        this.repository = repository;
        this.classGroupRepository = classGroupRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ClassLearnerResponse> handle(FindAllClassLearnersQuery query) {
        classGroupRepository.findByIdOrThrow(query.classGroupId());

        String searchTerm = StringUtils.hasText(query.searchTerm()) ? query.searchTerm() : null;

        return repository.findRowsByClassGroupId(query.classGroupId(), query.active(), searchTerm,
                        PageRequest.of(query.pageable().getPageNumber(), query.pageable().getPageSize()))
                .map(ClassLearnerResponse::fromRow);
    }
}
