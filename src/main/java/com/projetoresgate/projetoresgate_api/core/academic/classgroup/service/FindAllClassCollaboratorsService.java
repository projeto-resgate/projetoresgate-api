package com.projetoresgate.projetoresgate_api.core.academic.classgroup.service;

import com.projetoresgate.projetoresgate_api.core.academic.classgroup.api.dto.ClassCollaboratorResponse;
import com.projetoresgate.projetoresgate_api.core.academic.classgroup.domain.ClassCollaborator;
import com.projetoresgate.projetoresgate_api.core.academic.classgroup.repository.ClassCollaboratorRepository;
import com.projetoresgate.projetoresgate_api.core.academic.classgroup.repository.ClassGroupRepository;
import com.projetoresgate.projetoresgate_api.core.academic.classgroup.usecase.FindAllClassCollaboratorsUseCase;
import com.projetoresgate.projetoresgate_api.core.academic.classgroup.usecase.query.FindAllClassCollaboratorsQuery;
import com.projetoresgate.projetoresgate_api.core.academic.collaborator.domain.Collaborator;
import com.projetoresgate.projetoresgate_api.core.academic.collaborator.repository.CollaboratorRepository;
import com.projetoresgate.projetoresgate_api.core.academic.program.domain.EducatorCategoryItem;
import com.projetoresgate.projetoresgate_api.core.academic.program.repository.EducatorCategoryItemRepository;
import com.projetoresgate.projetoresgate_api.core.identity.naturalperson.domain.NaturalPerson;
import com.projetoresgate.projetoresgate_api.core.identity.naturalperson.repository.NaturalPersonRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class FindAllClassCollaboratorsService implements FindAllClassCollaboratorsUseCase {

    private final ClassCollaboratorRepository repository;
    private final ClassGroupRepository classGroupRepository;
    private final CollaboratorRepository collaboratorRepository;
    private final NaturalPersonRepository naturalPersonRepository;
    private final EducatorCategoryItemRepository educatorCategoryItemRepository;

    public FindAllClassCollaboratorsService(ClassCollaboratorRepository repository,
                                            ClassGroupRepository classGroupRepository,
                                            CollaboratorRepository collaboratorRepository,
                                            NaturalPersonRepository naturalPersonRepository,
                                            EducatorCategoryItemRepository educatorCategoryItemRepository) {
        this.repository = repository;
        this.classGroupRepository = classGroupRepository;
        this.collaboratorRepository = collaboratorRepository;
        this.naturalPersonRepository = naturalPersonRepository;
        this.educatorCategoryItemRepository = educatorCategoryItemRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ClassCollaboratorResponse> handle(FindAllClassCollaboratorsQuery query) {
        classGroupRepository.findByIdOrThrow(query.classGroupId());

        String searchTerm = StringUtils.hasText(query.searchTerm()) ? query.searchTerm() : null;

        Page<ClassCollaborator> page = repository.findAllByClassGroupId(
                query.classGroupId(), query.active(), query.educatorCategoryItemId(), searchTerm, query.pageable());

        List<ClassCollaborator> collaborators = page.getContent();

        Map<UUID, String> collaboratorNames = resolveCollaboratorNames(collaborators);
        Map<UUID, String> categoryNames = resolveCategoryNames(collaborators);

        List<ClassCollaboratorResponse> content = collaborators.stream()
                .map(collaborator -> ClassCollaboratorResponse.fromEntity(
                        collaborator,
                        collaboratorNames.get(collaborator.getCollaboratorId()),
                        categoryNames.get(collaborator.getEducatorCategoryItemId())))
                .toList();

        return new PageImpl<>(content, page.getPageable(), page.getTotalElements());
    }

    private Map<UUID, String> resolveCollaboratorNames(List<ClassCollaborator> links) {
        Set<UUID> ids = links.stream()
                .map(ClassCollaborator::getCollaboratorId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        if (ids.isEmpty()) {
            return Map.of();
        }

        Map<UUID, Collaborator> collaborators = collaboratorRepository.findAllById(ids).stream()
                .collect(Collectors.toMap(Collaborator::getId, collaborator -> collaborator));

        Set<UUID> naturalPersonIds = collaborators.values().stream()
                .map(Collaborator::getNaturalPersonId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        Map<UUID, NaturalPerson> persons = naturalPersonIds.isEmpty() ? Map.of()
                : naturalPersonRepository.findAllById(naturalPersonIds).stream()
                .collect(Collectors.toMap(NaturalPerson::getId, person -> person));

        return collaborators.values().stream()
                .filter(collaborator -> persons.containsKey(collaborator.getNaturalPersonId()))
                .collect(Collectors.toMap(
                        Collaborator::getId,
                        collaborator -> persons.get(collaborator.getNaturalPersonId()).getName()));
    }

    private Map<UUID, String> resolveCategoryNames(List<ClassCollaborator> links) {
        Set<UUID> ids = links.stream()
                .map(ClassCollaborator::getEducatorCategoryItemId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        if (ids.isEmpty()) {
            return Map.of();
        }

        return educatorCategoryItemRepository.findAllById(ids).stream()
                .collect(Collectors.toMap(EducatorCategoryItem::getId, EducatorCategoryItem::getName));
    }
}
