package com.projetoresgate.projetoresgate_api.core.identity.familygroup.service;

import com.projetoresgate.projetoresgate_api.core.identity.familygroup.api.dto.FamilyGroupSummaryResponse;
import com.projetoresgate.projetoresgate_api.core.identity.familygroup.domain.FamilyGroup;
import com.projetoresgate.projetoresgate_api.core.identity.familygroup.repository.FamilyGroupRepository;
import com.projetoresgate.projetoresgate_api.core.identity.familygroup.usecase.SearchFamilyGroupUseCase;
import com.projetoresgate.projetoresgate_api.core.identity.familygroup.usecase.query.SearchFamilyGroupQuery;
import com.projetoresgate.projetoresgate_api.shared.specification.SpecificationBuilder;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static java.util.Objects.nonNull;

@Service
public class SearchFamilyGroupService implements SearchFamilyGroupUseCase {

    private final FamilyGroupRepository repository;

    public SearchFamilyGroupService(FamilyGroupRepository repository) {
        this.repository = repository;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<FamilyGroupSummaryResponse> handle(SearchFamilyGroupQuery query) {

        Specification<FamilyGroup> genericFilters = new SpecificationBuilder<FamilyGroup>()
                .with("name", ":", query.name())
                .with("numberOfResidents", ":", query.numberOfResidents())
                .build();

        Specification<FamilyGroup> finalSpec = Specification
                .where(hasSearchTerm(query.searchTerm()))
                .and(genericFilters)
                .and(hasHouseholdIncomeRange(query.minHouseholdIncome(), query.maxHouseholdIncome()))
                .and(hasPerCapitaIncomeRange(query.minPerCapitaIncome(), query.maxPerCapitaIncome()));

        Page<FamilyGroup> page = repository.findAll(finalSpec, query.pageable());

        return new PageImpl<>(
                toSummaries(page.getContent()),
                page.getPageable(),
                page.getTotalElements()
        );
    }

    private List<FamilyGroupSummaryResponse> toSummaries(List<FamilyGroup> familyGroups) {
        if (familyGroups.isEmpty()) {
            return Collections.emptyList();
        }

        Map<UUID, Long> countsById = new HashMap<>();
        repository.countRegisteredPeopleByIds(familyGroups.stream().map(FamilyGroup::getId).toList())
                .forEach(projection -> countsById.put(projection.getFamilyGroupId(),
                        projection.getRegisteredPeopleCount()));

        return familyGroups.stream()
                .map(familyGroup -> FamilyGroupSummaryResponse.fromEntity(familyGroup,
                        countsById.getOrDefault(familyGroup.getId(), 0L)))
                .toList();
    }

    private Specification<FamilyGroup> hasSearchTerm(String searchTerm) {
        if (!StringUtils.hasText(searchTerm)) return null;

        return (root, criteriaQuery, cb) -> {
            String textSearch = "%" + searchTerm.toLowerCase() + "%";

            List<Predicate> orPredicates = new ArrayList<>();
            orPredicates.add(cb.like(cb.lower(root.get("name")), textSearch));

            return cb.or(orPredicates.toArray(new Predicate[0]));
        };
    }

    private Specification<FamilyGroup> hasHouseholdIncomeRange(BigDecimal min, BigDecimal max) {
        if (!nonNull(min) && !nonNull(max)) return null;

        return (root, criteriaQuery, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (nonNull(min)) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("householdIncome"), min));
            }
            if (nonNull(max)) {
                predicates.add(cb.lessThanOrEqualTo(root.get("householdIncome"), max));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    private Specification<FamilyGroup> hasPerCapitaIncomeRange(BigDecimal min, BigDecimal max) {
        if (!nonNull(min) && !nonNull(max)) return null;

        return (root, criteriaQuery, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (nonNull(min)) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("perCapitaIncome"), min));
            }
            if (nonNull(max)) {
                predicates.add(cb.lessThanOrEqualTo(root.get("perCapitaIncome"), max));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
