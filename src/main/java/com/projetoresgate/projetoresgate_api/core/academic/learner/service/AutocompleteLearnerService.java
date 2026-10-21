package com.projetoresgate.projetoresgate_api.core.academic.learner.service;

import com.projetoresgate.projetoresgate_api.core.academic.learner.api.dto.LearnerAutocompleteResponse;
import com.projetoresgate.projetoresgate_api.core.academic.learner.domain.Learner;
import com.projetoresgate.projetoresgate_api.core.academic.learner.repository.LearnerRepository;
import com.projetoresgate.projetoresgate_api.core.academic.learner.usecase.AutocompleteLearnerUseCase;
import com.projetoresgate.projetoresgate_api.core.academic.learner.usecase.query.AutocompleteLearnerQuery;
import com.projetoresgate.projetoresgate_api.core.identity.naturalperson.domain.NaturalPerson;
import com.projetoresgate.projetoresgate_api.core.identity.naturalperson.repository.NaturalPersonRepository;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Subquery;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class AutocompleteLearnerService implements AutocompleteLearnerUseCase {

    private final LearnerRepository repository;
    private final NaturalPersonRepository naturalPersonRepository;

    public AutocompleteLearnerService(LearnerRepository repository,
                                      NaturalPersonRepository naturalPersonRepository) {
        this.repository = repository;
        this.naturalPersonRepository = naturalPersonRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<LearnerAutocompleteResponse> handle(AutocompleteLearnerQuery query) {
        Pageable pageable = PageRequest.of(0, query.limit(), Sort.by("dateCreated").descending());

        List<Learner> learners = repository.findAll(hasSearchTerm(query.searchTerm()), pageable).getContent();

        Set<UUID> personIds = learners.stream()
                .map(Learner::getNaturalPersonId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        Map<UUID, NaturalPerson> persons = personIds.isEmpty() ? Map.of()
                : naturalPersonRepository.findAllById(personIds).stream()
                .collect(Collectors.toMap(NaturalPerson::getId, person -> person));

        return learners.stream()
                .map(learner -> {
                    NaturalPerson person = persons.get(learner.getNaturalPersonId());
                    return LearnerAutocompleteResponse.fromEntity(
                            learner,
                            person == null ? null : person.getName(),
                            person == null ? null : person.getCpf());
                })
                .toList();
    }

    private Specification<Learner> hasSearchTerm(String searchTerm) {
        if (!StringUtils.hasText(searchTerm)) {
            return null;
        }

        return (root, criteriaQuery, cb) -> {
            List<Predicate> orPredicates = new ArrayList<>();

            String textSearch = "%" + searchTerm.toLowerCase() + "%";

            String digits = searchTerm.replaceAll("\\D", "");

            Subquery<UUID> subquery = criteriaQuery.subquery(UUID.class);
            var person = subquery.from(NaturalPerson.class);

            List<Predicate> personPredicates = new ArrayList<>();
            personPredicates.add(cb.like(cb.lower(person.get("name")), textSearch));
            personPredicates.add(cb.like(cb.lower(person.get("nickname")), textSearch));
            if (StringUtils.hasText(digits)) {
                personPredicates.add(cb.like(person.get("cpf"), "%" + digits + "%"));
            }

            subquery.select(person.get("id"))
                    .where(cb.or(personPredicates.toArray(new Predicate[0])));

            orPredicates.add(root.get("naturalPersonId").in(subquery));

            return cb.or(orPredicates.toArray(new Predicate[0]));
        };
    }
}
