package com.projetoresgate.projetoresgate_api.core.academic.learner.service;

import com.projetoresgate.projetoresgate_api.core.academic.classgroup.domain.ClassLearner;
import com.projetoresgate.projetoresgate_api.core.academic.classgroup.repository.ClassLearnerRepository;
import com.projetoresgate.projetoresgate_api.core.academic.classgroup.repository.LearnerProgramRow;
import com.projetoresgate.projetoresgate_api.core.academic.learner.api.dto.LearnerSummaryResponse;
import com.projetoresgate.projetoresgate_api.core.academic.learner.domain.Learner;
import com.projetoresgate.projetoresgate_api.core.academic.learner.domain.enums.LearnerParticipationStatus;
import com.projetoresgate.projetoresgate_api.core.academic.learner.domain.enums.LearnerStatus;
import com.projetoresgate.projetoresgate_api.core.academic.learner.repository.LearnerRepository;
import com.projetoresgate.projetoresgate_api.core.academic.learner.usecase.FindAllLearnersUseCase;
import com.projetoresgate.projetoresgate_api.core.academic.learner.usecase.query.FindAllLearnersQuery;
import com.projetoresgate.projetoresgate_api.core.identity.legalperson.api.dto.LegalPersonSummaryResponse;
import com.projetoresgate.projetoresgate_api.core.identity.legalperson.usecase.FindLegalPersonSummariesUseCase;
import com.projetoresgate.projetoresgate_api.core.identity.naturalperson.domain.NaturalPerson;
import com.projetoresgate.projetoresgate_api.core.identity.naturalperson.repository.NaturalPersonRepository;
import com.projetoresgate.projetoresgate_api.shared.specification.SpecificationBuilder;
import com.projetoresgate.projetoresgate_api.shared.specification.Specs;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Subquery;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static java.util.Objects.isNull;

@Service
public class FindAllLearnersService implements FindAllLearnersUseCase {

    private final LearnerRepository repository;
    private final NaturalPersonRepository naturalPersonRepository;
    private final FindLegalPersonSummariesUseCase findLegalPersonSummariesUseCase;
    private final ClassLearnerRepository classLearnerRepository;

    public FindAllLearnersService(LearnerRepository repository,
                                  NaturalPersonRepository naturalPersonRepository,
                                  FindLegalPersonSummariesUseCase findLegalPersonSummariesUseCase,
                                  ClassLearnerRepository classLearnerRepository) {
        this.repository = repository;
        this.naturalPersonRepository = naturalPersonRepository;
        this.findLegalPersonSummariesUseCase = findLegalPersonSummariesUseCase;
        this.classLearnerRepository = classLearnerRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<LearnerSummaryResponse> handle(FindAllLearnersQuery query) {

        Specification<Learner> genericFilters = new SpecificationBuilder<Learner>()
                .with("institutionId", ":", query.institutionId())
                .with("schoolGradeId", ":", query.schoolGradeId())
                .with("academicYear", ":", query.academicYear())
                .build();

        LocalDateTime now = LocalDateTime.now();

        Specification<Learner> finalSpec = Specs.allOf(
                hasSearchTerm(query.searchTerm()),
                hasParticipationStatus(query.status(), now),
                genericFilters);

        Page<Learner> page = repository.findAll(finalSpec, query.pageable());

        Map<UUID, String> personNames = resolvePersonNames(page.getContent());
        Map<UUID, String> institutionNames = resolveInstitutionNames(page.getContent());
        Map<UUID, List<String>> programNames = resolveProgramNames(page.getContent());

        Set<UUID> inProgress = resolveLearnersInProgress(page.getContent(), now);

        return page.map(learner -> LearnerSummaryResponse.fromEntity(
                learner,
                isNull(learner.getNaturalPersonId()) ? null : personNames.get(learner.getNaturalPersonId()),
                programNames.getOrDefault(learner.getId(), List.of()),
                isNull(learner.getInstitutionId()) ? null : institutionNames.get(learner.getInstitutionId()),
                inProgress.contains(learner.getId())));
    }

    private Set<UUID> resolveLearnersInProgress(List<Learner> learners, LocalDateTime now) {
        List<UUID> ids = learners.stream().map(Learner::getId).toList();

        if (ids.isEmpty()) {
            return Set.of();
        }

        return new HashSet<>(classLearnerRepository.findLearnerIdsInProgress(ids, now));
    }

    // uma consulta para a página inteira; a query já devolve os nomes distintos e em ordem alfabética
    private Map<UUID, List<String>> resolveProgramNames(List<Learner> learners) {
        List<UUID> ids = learners.stream().map(Learner::getId).toList();

        if (ids.isEmpty()) {
            return Map.of();
        }

        return classLearnerRepository.findActiveProgramNamesByLearnerIds(ids).stream()
                .collect(Collectors.groupingBy(LearnerProgramRow::getLearnerId, LinkedHashMap::new,
                        Collectors.mapping(LearnerProgramRow::getProgramName, Collectors.toList())));
    }

    private Specification<Learner> hasParticipationStatus(LearnerParticipationStatus status, LocalDateTime now) {
        if (isNull(status)) {
            return null;
        }

        return (root, criteriaQuery, cb) -> {
            if (status == LearnerParticipationStatus.DROPPED_OUT) {
                return cb.equal(root.get("status"), LearnerStatus.DROPPED_OUT);
            }

            Subquery<UUID> inProgress = criteriaQuery.subquery(UUID.class);
            var link = inProgress.from(ClassLearner.class);
            var classGroup = link.join("classGroup");
            inProgress.select(link.get("learnerId"))
                    .where(cb.equal(link.get("learnerId"), root.get("id")),
                            cb.isTrue(link.get("active")),
                            cb.isTrue(classGroup.get("active")),
                            cb.lessThanOrEqualTo(classGroup.<LocalDateTime>get("startDate"), now),
                            cb.greaterThanOrEqualTo(classGroup.<LocalDateTime>get("endDate"), now));

            var notDroppedOut = cb.equal(root.get("status"), LearnerStatus.ACTIVE);
            return status == LearnerParticipationStatus.ACTIVE
                    ? cb.and(notDroppedOut, cb.exists(inProgress))
                    : cb.and(notDroppedOut, cb.not(cb.exists(inProgress)));
        };
    }

    private Specification<Learner> hasSearchTerm(String searchTerm) {
        if (!StringUtils.hasText(searchTerm)) {
            return null;
        }

        return (root, criteriaQuery, cb) -> {
            String pattern = "%" + searchTerm.toLowerCase(Locale.ROOT) + "%";

            Subquery<UUID> subquery = criteriaQuery.subquery(UUID.class);
            var person = subquery.from(NaturalPerson.class);
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.like(cb.lower(person.get("name")), pattern));
            predicates.add(cb.like(cb.lower(person.get("nickname")), pattern));
            predicates.add(cb.like(person.get("cpf"), pattern));
            subquery.select(person.get("id"))
                    .where(cb.or(predicates.toArray(Predicate[]::new)));

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

        return naturalPersonRepository.findAllByIdIncludingDeleted(ids).stream()
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
