package com.projetoresgate.projetoresgate_api.core.academic.learner.service;

import com.projetoresgate.projetoresgate_api.core.academic.learner.usecase.command.ResponsibleCommand;
import com.projetoresgate.projetoresgate_api.core.academic.learner.api.dto.LearnerResponse;
import com.projetoresgate.projetoresgate_api.core.academic.learner.domain.Learner;
import com.projetoresgate.projetoresgate_api.core.academic.learner.domain.Responsible;
import com.projetoresgate.projetoresgate_api.core.academic.learner.repository.LearnerRepository;
import com.projetoresgate.projetoresgate_api.core.academic.learner.usecase.FindLearnerByIdUseCase;
import com.projetoresgate.projetoresgate_api.core.academic.learner.usecase.UpdateLearnerUseCase;
import com.projetoresgate.projetoresgate_api.core.academic.learner.usecase.command.UpdateLearnerCommand;
import com.projetoresgate.projetoresgate_api.core.academic.learner.usecase.query.FindLearnerByIdQuery;
import com.projetoresgate.projetoresgate_api.core.academic.schoolgrade.repository.SchoolGradeRepository;
import com.projetoresgate.projetoresgate_api.core.identity.familygroup.repository.FamilyGroupRepository;
import com.projetoresgate.projetoresgate_api.core.identity.legalperson.repository.LegalPersonRepository;
import com.projetoresgate.projetoresgate_api.core.identity.naturalperson.repository.NaturalPersonRepository;
import com.projetoresgate.projetoresgate_api.infrastructure.exception.InternalException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static java.util.Objects.nonNull;


@Service
public class UpdateLearnerService implements UpdateLearnerUseCase {

    private final LearnerRepository repository;
    private final FamilyGroupRepository familyGroupRepository;
    private final NaturalPersonRepository naturalPersonRepository;
    private final LegalPersonRepository legalPersonRepository;
    private final SchoolGradeRepository schoolGradeRepository;
    private final FindLearnerByIdUseCase findLearnerByIdUseCase;
    private final LearnerRoleSynchronizer roleSynchronizer;

    public UpdateLearnerService(LearnerRepository repository,
                                FamilyGroupRepository familyGroupRepository,
                                NaturalPersonRepository naturalPersonRepository,
                                LegalPersonRepository legalPersonRepository,
                                SchoolGradeRepository schoolGradeRepository,
                                FindLearnerByIdUseCase findLearnerByIdUseCase,
                                LearnerRoleSynchronizer roleSynchronizer) {
        this.repository = repository;
        this.familyGroupRepository = familyGroupRepository;
        this.naturalPersonRepository = naturalPersonRepository;
        this.legalPersonRepository = legalPersonRepository;
        this.schoolGradeRepository = schoolGradeRepository;
        this.findLearnerByIdUseCase = findLearnerByIdUseCase;
        this.roleSynchronizer = roleSynchronizer;
    }

    @Override
    @Transactional
    public LearnerResponse handle(UpdateLearnerCommand command) {
        Learner learner = repository.findByIdOrThrow(command.id());

        validateNaturalPersonOwnership(learner, command.naturalPersonId());

        validateLearnerBelongsToFamilyGroup(command.familyGroupId(), command.naturalPersonId());
        if (nonNull(command.institutionId())) {
            legalPersonRepository.findByIdOrThrow(command.institutionId());
        }

        if (nonNull(command.schoolGradeId())) {
            schoolGradeRepository.findByIdOrThrow(command.schoolGradeId());
        }

        learner.update()
                .institutionId(command.institutionId())
                .schoolGradeId(command.schoolGradeId())
                .interestArts(Boolean.TRUE.equals(command.interestArts()))
                .scholarshipCondition(command.scholarshipCondition())
                .academicYear(command.academicYear())
                .motherName(command.motherName())
                .schoolEnrollment(command.schoolEnrollment())
                .apply();

        List<Responsible> removed = learner.syncResponsibleList(
                toResponsibles(learner, command.familyGroupId(), command.responsibleList()));

        Learner saved = repository.save(learner);

        roleSynchronizer.forgetRemoved(removed);
        roleSynchronizer.declare(saved);

        return findLearnerByIdUseCase.handle(new FindLearnerByIdQuery(saved.getId()));
    }

    private void validateNaturalPersonOwnership(Learner learner, UUID naturalPersonId) {
        if (!learner.getNaturalPersonId().equals(naturalPersonId)) {
            throw new InternalException("O educando não pode ser transferido para outra pessoa física.");
        }
    }

    private void validateLearnerBelongsToFamilyGroup(UUID familyGroupId, UUID naturalPersonId) {
        familyGroupRepository.findByIdOrThrow(familyGroupId);
        validatePersonBelongsToFamilyGroup(familyGroupId, naturalPersonId);
    }

    private void validatePersonBelongsToFamilyGroup(UUID familyGroupId, UUID naturalPersonId) {
        naturalPersonRepository.findByIdOrThrow(naturalPersonId);

        if (!familyGroupRepository.existsNaturalPersonMember(familyGroupId, naturalPersonId)) {
            throw new InternalException("A pessoa física informada não pertence a este grupo familiar.");
        }
    }

    private List<Responsible> toResponsibles(Learner learner, UUID familyGroupId, List<ResponsibleCommand> commands) {
        if (!nonNull(commands)) {
            return new ArrayList<>();
        }

        List<Responsible> responsibles = new ArrayList<>(commands.size());

        for (ResponsibleCommand command : commands) {
            validatePersonBelongsToFamilyGroup(familyGroupId, command.naturalPersonId());

            if (nonNull(command.schoolGradeId())) {
                schoolGradeRepository.findByIdOrThrow(command.schoolGradeId());
            }

            responsibles.add(command.toEntity(learner));
        }

        return responsibles;
    }
}