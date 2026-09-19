package com.projetoresgate.projetoresgate_api.core.academic.program.service;

import com.projetoresgate.projetoresgate_api.core.academic.program.api.dto.ProgramResponse;
import com.projetoresgate.projetoresgate_api.core.academic.program.domain.EducatorCategoryItem;
import com.projetoresgate.projetoresgate_api.core.academic.program.domain.Program;
import com.projetoresgate.projetoresgate_api.core.academic.program.repository.EducatorCategoryItemRepository;
import com.projetoresgate.projetoresgate_api.core.academic.program.repository.ProgramRepository;
import com.projetoresgate.projetoresgate_api.core.academic.program.usecase.UpdateProgramUseCase;
import com.projetoresgate.projetoresgate_api.core.academic.program.usecase.command.UpdateProgramCommand;
import com.projetoresgate.projetoresgate_api.core.identity.legalperson.api.dto.LegalPersonSummaryResponse;
import com.projetoresgate.projetoresgate_api.core.identity.legalperson.usecase.FindLegalPersonSummariesUseCase;
import com.projetoresgate.projetoresgate_api.infrastructure.exception.InternalException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Service
public class UpdateProgramService implements UpdateProgramUseCase {

    private final ProgramRepository repository;
    private final EducatorCategoryItemRepository itemRepository;
    private final FindLegalPersonSummariesUseCase findLegalPersonSummariesUseCase;

    public UpdateProgramService(ProgramRepository repository,
                                EducatorCategoryItemRepository itemRepository,
                                FindLegalPersonSummariesUseCase findLegalPersonSummariesUseCase) {
        this.repository = repository;
        this.itemRepository = itemRepository;
        this.findLegalPersonSummariesUseCase = findLegalPersonSummariesUseCase;
    }

    @Override
    @Transactional
    public ProgramResponse handle(UpdateProgramCommand command) {
        Program program = repository.findByIdOrThrow(command.id());

        if (StringUtils.hasText(command.name()) && repository.existsByNameAndIdNot(command.name(), program.getId())) {
            throw new InternalException("Já existe um programa cadastrado com este nome.");
        }

        reconcileEducatorCategoryItems(program, command.educatorCategoryItems());

        Program updated = repository.save(
                program.update()
                        .name(command.name())
                        .webSiteUrl(command.webSiteUrl())
                        .status(command.status())
                        .apply()
        );

        return toResponse(updated);
    }

    private void reconcileEducatorCategoryItems(Program program, List<UpdateProgramCommand.UpdateEducatorCategoryItemCommand> items) {
        List<UpdateProgramCommand.UpdateEducatorCategoryItemCommand> requested = items == null ? List.of() : items;

        Map<UUID, EducatorCategoryItem> existingById = new HashMap<>();
        for (EducatorCategoryItem existing : program.getEducatorCategoryItemList()) {
            existingById.put(existing.getId(), existing);
        }

        Set<UUID> requestedIds = new HashSet<>();
        for (UpdateProgramCommand.UpdateEducatorCategoryItemCommand itemCommand : requested) {
            if (itemCommand.id() == null) {
                EducatorCategoryItem created = EducatorCategoryItem.create(itemCommand.name(), program);
                program.getEducatorCategoryItemList().add(created);
                requestedIds.add(created.getId());
                continue;
            }

            EducatorCategoryItem existing = existingById.get(itemCommand.id());
            if (existing == null) {
                throw new InternalException(
                        "Categoria de educador com ID " + itemCommand.id() + " não pertence ao programa " + program.getId() + ".");
            }

            requestedIds.add(itemCommand.id());
            if (!existing.getName().equals(itemCommand.name())) {
                existing.updateName(itemCommand.name());
            }
        }

        List<EducatorCategoryItem> removed = program.getEducatorCategoryItemList().stream()
                .filter(item -> !requestedIds.contains(item.getId()))
                .toList();

        program.getEducatorCategoryItemList().removeIf(item -> !requestedIds.contains(item.getId()));

        removed.forEach(itemRepository::delete);
    }

    private ProgramResponse toResponse(Program program) {
        LegalPersonSummaryResponse institution = resolveInstitution(program.getInstitutionId());
        return ProgramResponse.fromEntity(program, institution);
    }

    private LegalPersonSummaryResponse resolveInstitution(UUID institutionId) {
        if (institutionId == null) {
            return null;
        }
        return findLegalPersonSummariesUseCase.handle(Set.of(institutionId))
                .stream()
                .findFirst()
                .orElse(null);
    }
}