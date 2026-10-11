package com.projetoresgate.projetoresgate_api.core.academic.learner.service;

import com.projetoresgate.projetoresgate_api.core.academic.learner.api.dto.LearnerSummaryResponse;
import com.projetoresgate.projetoresgate_api.core.academic.learner.domain.Learner;
import com.projetoresgate.projetoresgate_api.core.academic.learner.repository.LearnerRepository;
import com.projetoresgate.projetoresgate_api.core.academic.learner.usecase.FindAllLearnersUseCase;
import com.projetoresgate.projetoresgate_api.core.academic.learner.usecase.query.FindAllLearnersQuery;
import com.projetoresgate.projetoresgate_api.core.identity.legalperson.api.dto.LegalPersonSummaryResponse;
import com.projetoresgate.projetoresgate_api.core.identity.legalperson.usecase.FindLegalPersonSummariesUseCase;
import com.projetoresgate.projetoresgate_api.core.identity.naturalperson.domain.NaturalPerson;
import com.projetoresgate.projetoresgate_api.core.identity.naturalperson.repository.NaturalPersonRepository;
import com.projetoresgate.projetoresgate_api.shared.specification.SpecificationBuilder;
import jakarta.persistence.criteria.Subquery;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import static java.util.Objects.isNull;

@Service
public class FindAllLearnersService implements FindAllLearnersUseCase {

    private final LearnerRepository repository;
    private final NaturalPersonRepository naturalPersonRepository;
    private final FindLegalPersonSummariesUseCase findLegalPersonSummariesUseCase;

    public FindAllLearnersService(LearnerRepository repository,
                                  NaturalPersonRepository naturalPersonRepository,
                                  FindLegalPersonSummariesUseCase findLegalPersonSummariesUseCase) {
        this.repository = repository;
        this.naturalPersonRepository = naturalPersonRepository;
        this.findLegalPersonSummariesUseCase = findLegalPersonSummariesUseCase;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<LearnerSummaryResponse> handle(FindAllLearnersQuery query) {

        Specification<Learner> genericFilters = new SpecificationBuilder<Learner>()
                .with("status", ":", query.status())
                .with("institutionId", ":", query.institutionId())
                .with("schoolGradeId", ":", query.schoolGradeId())
                .with("academicYear", ":", query.academicYear())
                .build();

        Specification<Learner> finalSpec = Specification
                .where(hasSearchTerm(query.searchTerm()))
                .and(genericFilters);

        Page<Learner> page = repository.findAll(finalSpec, query.pageable());

        Map<UUID, String> personNames = resolvePersonNames(page.getContent());
        Map<UUID, String> institutionNames = resolveInstitutionNames(page.getContent());

        return page.map(learner -> LearnerSummaryResponse.fromEntity(
                learner,
                isNull(learner.getNaturalPersonId()) ? null : personNames.get(learner.getNaturalPersonId()),
                isNull(learner.getInstitutionId()) ? null : institutionNames.get(learner.getInstitutionId())));
    }

    private Specification<Learner> hasSearchTerm(String searchTerm) {
        if (!StringUtils.hasText(searchTerm)) {
            return null;
        }

        return (root, criteriaQuery, cb) -> {
            String pattern = "%" + searchTerm.toLowerCase(Locale.ROOT) + "%";

            Subquery<UUID> subquery = criteriaQuery.subquery(UUID.class);
            var person = subquery.from(NaturalPerson.class);
            subquery.select(person.get("id"))
                    .where(cb.or(
                            cb.like(cb.lower(person.get("name")), pattern),
                            cb.like(cb.lower(person.get("nickname")), pattern)));

            return root.get("naturalPersonId").in(subquery);
        };
    }

    private Map<UUID, String> resolvePersonNames(List<Learner> learners) {
        Set<UUID> ids = learners.stream()
                .map(Learner::getNaturalPersonId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        if (ids.isEmpty()) {
            return Map.of();
        }

        return naturalPersonRepository.findAllById(ids).stream()
                .collect(Collectors.toMap(NaturalPerson::getId, NaturalPerson::getName));
    }

    private Map<UUID, String> resolveInstitutionNames(List<Learner> learners) {
        Set<UUID> ids = learners.stream()
                .map(Learner::getInstitutionId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        if (ids.isEmpty()) {
            return Map.of();
        }

        return findLegalPersonSummariesUseCase.handle(ids).stream()
                .collect(Collectors.toMap(LegalPersonSummaryResponse::id, LegalPersonSummaryResponse::displayName));
    }
}
