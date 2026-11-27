package com.projetoresgate.projetoresgate_api.core.academic.learner.service;

import com.projetoresgate.projetoresgate_api.core.academic.learner.repository.ResponsibleRepository;
import com.projetoresgate.projetoresgate_api.core.academic.learner.repository.LearnerRepository;
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
import java.util.UUID;
import java.util.Set;
import java.util.HashSet;

@Service
public class FindEligibleResponsiblesService implements FindEligibleResponsiblesUseCase {

    private final FamilyGroupRepository repository;
    private final LearnerRepository learnerRepository;
    private final ResponsibleRepository responsibleRepository;

    public FindEligibleResponsiblesService(FamilyGroupRepository repository, LearnerRepository learnerRepository,
                                           ResponsibleRepository responsibleRepository) {
        this.repository = repository;
        this.learnerRepository = learnerRepository;
        this.responsibleRepository = responsibleRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<EligibleResponsibleResponse> handle(FindEligibleResponsiblesQuery query) {
        repository.findByIdOrThrow(query.familyGroupId());

        Page<NaturalPerson> page = repository.findEligibleResponsiblesByFamilyGroupId(
                query.familyGroupId(), excludedNaturalPersonIds(query.excludeNaturalPersonId()), query.pageable());

        return new PageImpl<>(
                toResponses(page.getContent()),
                page.getPageable(),
                page.getTotalElements()
        );
    }

    // A própria pessoa do educando e quem já é responsável dele não voltam como opção
    private Set<UUID> excludedNaturalPersonIds(UUID learnerNaturalPersonId) {
        Set<UUID> excluded = new HashSet<>();
        excluded.add(learnerNaturalPersonId);
        learnerRepository.findByNaturalPersonId(learnerNaturalPersonId)
                .map(learner -> responsibleRepository.findNaturalPersonIdsByLearnerId(learner.getId()))
                .ifPresent(excluded::addAll);
        return excluded;
    }

    private List<EligibleResponsibleResponse> toResponses(List<NaturalPerson> naturalPersons) {
        if (naturalPersons.isEmpty()) {
            return Collections.emptyList();
        }

        return naturalPersons.stream().map(EligibleResponsibleResponse::fromEntity).toList();
    }
}
