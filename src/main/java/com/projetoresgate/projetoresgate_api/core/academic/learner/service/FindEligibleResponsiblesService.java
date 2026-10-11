package com.projetoresgate.projetoresgate_api.core.academic.learner.service;

import com.projetoresgate.projetoresgate_api.core.academic.learner.api.dto.EligibleResponsibleResponse;
import com.projetoresgate.projetoresgate_api.core.academic.learner.usecase.FindEligibleResponsiblesUseCase;
import com.projetoresgate.projetoresgate_api.core.academic.learner.usecase.query.FindEligibleResponsiblesQuery;
import com.projetoresgate.projetoresgate_api.core.identity.familygroup.repository.FamilyGroupRepository;
import com.projetoresgate.projetoresgate_api.core.identity.naturalperson.domain.NaturalPerson;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;

@Service
public class FindEligibleResponsiblesService implements FindEligibleResponsiblesUseCase {

    private final FamilyGroupRepository repository;

    public FindEligibleResponsiblesService(FamilyGroupRepository repository) {
        this.repository = repository;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<EligibleResponsibleResponse> handle(FindEligibleResponsiblesQuery query) {
        repository.findByIdOrThrow(query.familyGroupId());

        Page<NaturalPerson> page = repository.findEligibleResponsiblesByFamilyGroupId(
                query.familyGroupId(), query.excludeNaturalPersonId(), query.pageable());

        return new PageImpl<>(
                toResponses(page.getContent()),
                page.getPageable(),
                page.getTotalElements()
        );
    }

    private List<EligibleResponsibleResponse> toResponses(List<NaturalPerson> naturalPersons) {
        if (naturalPersons.isEmpty()) {
            return Collections.emptyList();
        }

        return naturalPersons.stream().map(EligibleResponsibleResponse::fromEntity).toList();
    }
}
