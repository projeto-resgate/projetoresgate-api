package com.projetoresgate.projetoresgate_api.core.academic.classgroup.service;

import com.projetoresgate.projetoresgate_api.core.academic.classgroup.api.dto.ClassLearnerResponse;
import com.projetoresgate.projetoresgate_api.core.academic.classgroup.domain.ClassLearner;
import com.projetoresgate.projetoresgate_api.core.academic.classgroup.repository.ClassGroupRepository;
import com.projetoresgate.projetoresgate_api.core.academic.classgroup.repository.ClassLearnerRepository;
import com.projetoresgate.projetoresgate_api.core.academic.classgroup.usecase.FindAllClassLearnersUseCase;
import com.projetoresgate.projetoresgate_api.core.academic.classgroup.usecase.query.FindAllClassLearnersQuery;
import com.projetoresgate.projetoresgate_api.core.academic.learner.domain.Learner;
import com.projetoresgate.projetoresgate_api.core.academic.learner.repository.LearnerRepository;
import com.projetoresgate.projetoresgate_api.core.identity.naturalperson.domain.NaturalPerson;
import com.projetoresgate.projetoresgate_api.core.identity.naturalperson.repository.NaturalPersonRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class FindAllClassLearnersService implements FindAllClassLearnersUseCase {

    private final ClassLearnerRepository repository;
    private final ClassGroupRepository classGroupRepository;
    private final LearnerRepository learnerRepository;
    private final NaturalPersonRepository naturalPersonRepository;

    public FindAllClassLearnersService(ClassLearnerRepository repository,
                                       ClassGroupRepository classGroupRepository,
                                       LearnerRepository learnerRepository,
                                       NaturalPersonRepository naturalPersonRepository) {
        this.repository = repository;
        this.classGroupRepository = classGroupRepository;
        this.learnerRepository = learnerRepository;
        this.naturalPersonRepository = naturalPersonRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ClassLearnerResponse> handle(FindAllClassLearnersQuery query) {
        classGroupRepository.findByIdOrThrow(query.classGroupId());

        String searchTerm = StringUtils.hasText(query.searchTerm()) ? query.searchTerm() : null;

        Page<ClassLearner> page = repository.findAllByClassGroupId(
                query.classGroupId(), query.active(), searchTerm, query.pageable());

        List<ClassLearner> links = page.getContent();

        Map<UUID, Learner> learners = resolveLearners(links);
        Map<UUID, NaturalPerson> persons = resolvePersons(learners.values());

        List<ClassLearnerResponse> content = links.stream()
                .map(link -> {
                    Learner learner = learners.get(link.getLearnerId());
                    NaturalPerson person = learner == null ? null : persons.get(learner.getNaturalPersonId());
                    return ClassLearnerResponse.fromEntity(
                            link,
                            person == null ? null : person.getName(),
                            person == null ? null : person.getCpf(),
                            learner == null ? null : learner.getStatus());
                })
                .toList();

        return new PageImpl<>(content, page.getPageable(), page.getTotalElements());
    }

    private Map<UUID, Learner> resolveLearners(List<ClassLearner> links) {
        Set<UUID> ids = links.stream()
                .map(ClassLearner::getLearnerId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        if (ids.isEmpty()) {
            return Map.of();
        }

        return learnerRepository.findAllById(ids).stream()
                .collect(Collectors.toMap(Learner::getId, learner -> learner));
    }

    private Map<UUID, NaturalPerson> resolvePersons(Collection<Learner> learners) {
        Set<UUID> ids = learners.stream()
                .map(Learner::getNaturalPersonId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        if (ids.isEmpty()) {
            return Map.of();
        }

        return naturalPersonRepository.findAllById(ids).stream()
                .collect(Collectors.toMap(NaturalPerson::getId, person -> person));
    }
}
