package com.projetoresgate.projetoresgate_api.core.academic.classgroup.service;

import com.projetoresgate.projetoresgate_api.core.academic.classgroup.api.dto.ClassScheduleResponse;
import com.projetoresgate.projetoresgate_api.core.academic.classgroup.domain.ClassSchedule;
import com.projetoresgate.projetoresgate_api.core.academic.classgroup.repository.ClassGroupRepository;
import com.projetoresgate.projetoresgate_api.core.academic.classgroup.repository.ClassScheduleRepository;
import com.projetoresgate.projetoresgate_api.core.academic.classgroup.usecase.FindAllClassSchedulesUseCase;
import com.projetoresgate.projetoresgate_api.core.academic.classgroup.usecase.query.FindAllClassSchedulesQuery;
import com.projetoresgate.projetoresgate_api.shared.specification.SpecificationBuilder;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class FindAllClassSchedulesService implements FindAllClassSchedulesUseCase {

    private final ClassScheduleRepository repository;
    private final ClassGroupRepository classGroupRepository;

    public FindAllClassSchedulesService(ClassScheduleRepository repository,
                                        ClassGroupRepository classGroupRepository) {
        this.repository = repository;
        this.classGroupRepository = classGroupRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ClassScheduleResponse> handle(FindAllClassSchedulesQuery query) {
        classGroupRepository.findByIdOrThrow(query.classGroupId());

        Specification<ClassSchedule> filters = new SpecificationBuilder<ClassSchedule>()
                .with("classGroup.id", ":", query.classGroupId())
                .with("weekDay", ":", query.weekDay())
                .with("active", ":", query.active())
                .build();

        return repository.findAll(filters, query.pageable()).map(ClassScheduleResponse::fromEntity);
    }
}
