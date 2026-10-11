package com.projetoresgate.projetoresgate_api.core.academic.learner.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.projetoresgate.projetoresgate_api.config.security.WithMockCustomUser;
import com.projetoresgate.projetoresgate_api.core.academic.learner.usecase.command.ResponsibleCommand;
import com.projetoresgate.projetoresgate_api.core.academic.learner.api.dto.EligibleResponsibleResponse;
import com.projetoresgate.projetoresgate_api.core.academic.learner.api.dto.LearnerResponse;
import com.projetoresgate.projetoresgate_api.core.academic.learner.api.dto.LearnerSummaryResponse;
import com.projetoresgate.projetoresgate_api.core.academic.learner.api.dto.ResponsibleResponse;
import com.projetoresgate.projetoresgate_api.core.academic.learner.domain.enums.Kinship;
import com.projetoresgate.projetoresgate_api.core.academic.learner.domain.enums.LearnerStatus;
import com.projetoresgate.projetoresgate_api.core.academic.learner.domain.enums.ResponsibleType;
import com.projetoresgate.projetoresgate_api.core.academic.learner.domain.enums.ScholarshipCondition;
import com.projetoresgate.projetoresgate_api.core.academic.learner.usecase.*;
import com.projetoresgate.projetoresgate_api.core.academic.learner.usecase.command.CreateLearnerCommand;
import com.projetoresgate.projetoresgate_api.core.academic.learner.usecase.command.DropoutLearnerCommand;
import com.projetoresgate.projetoresgate_api.core.academic.learner.usecase.command.ReactivateLearnerCommand;
import com.projetoresgate.projetoresgate_api.core.academic.learner.usecase.command.SoftDeleteLearnerCommand;
import com.projetoresgate.projetoresgate_api.core.academic.learner.usecase.command.UpdateLearnerCommand;
import com.projetoresgate.projetoresgate_api.core.academic.learner.usecase.query.FindAllLearnersQuery;
import com.projetoresgate.projetoresgate_api.core.academic.learner.usecase.query.FindEligibleResponsiblesQuery;
import com.projetoresgate.projetoresgate_api.core.academic.learner.usecase.query.FindLearnerByIdQuery;
import com.projetoresgate.projetoresgate_api.core.identity.user.repository.UserRepository;
import com.projetoresgate.projetoresgate_api.infrastructure.exception.InternalException;
import com.projetoresgate.projetoresgate_api.infrastructure.exception.ResourceNotFoundException;
import com.projetoresgate.projetoresgate_api.infrastructure.security.SecurityConfigurations;
import com.projetoresgate.projetoresgate_api.infrastructure.services.ITokenService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = LearnerController.class)
@Import(SecurityConfigurations.class)
@DisplayName("LearnerController - Test")
class LearnerControllerTest {

    private static final Pageable PAGEABLE = PageRequest.of(0, 10, Sort.by("name").ascending());

    private static final UUID FAMILY_GROUP_ID = UUID.randomUUID();
    private static final UUID NATURAL_PERSON_ID = UUID.randomUUID();
    private static final UUID RESPONSIBLE_PERSON_ID = UUID.randomUUID();
    private static final UUID SCHOOL_GRADE_ID = UUID.randomUUID();
    private static final UUID INSTITUTION_ID = UUID.randomUUID();
    private static final UUID LEARNER_ID = UUID.randomUUID();

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private CreateLearnerUseCase createUseCase;

    @MockitoBean
    private UpdateLearnerUseCase updateUseCase;

    @MockitoBean
    private SoftDeleteLearnerUseCase softDeleteUseCase;

    @MockitoBean
    private FindLearnerByIdUseCase findByIdUseCase;

    @MockitoBean
    private FindAllLearnersUseCase findAllUseCase;

    @MockitoBean
    private FindEligibleResponsiblesUseCase findEligibleResponsiblesUseCase;

    @MockitoBean
    private DropoutLearnerUseCase dropoutUseCase;

    @MockitoBean
    private ReactivateLearnerUseCase reactivateUseCase;

    @MockitoBean
    private UserDetailsService userDetailsService;

    @MockitoBean
    private ITokenService tokenService;

    @MockitoBean
    private UserRepository userRepository;

    private static CreateLearnerCommand givenCreateCommand() {
        return new CreateLearnerCommand(
                NATURAL_PERSON_ID,
                FAMILY_GROUP_ID,
                INSTITUTION_ID,
                SCHOOL_GRADE_ID,
                true,
                ScholarshipCondition.PARTIAL,
                2026,
                "Maria da Silva",
                "MAT-2026-0001",
                List.of(new ResponsibleCommand(RESPONSIBLE_PERSON_ID, ResponsibleType.FINANCIAL,
                        Kinship.FATHER, null)));
    }

    private static UpdateLearnerCommand givenUpdateCommand(List<ResponsibleCommand> responsibleList) {
        return new UpdateLearnerCommand(null, NATURAL_PERSON_ID, FAMILY_GROUP_ID, INSTITUTION_ID,
                SCHOOL_GRADE_ID, true, ScholarshipCondition.PARTIAL, 2026, "Maria da Silva",
                "MAT-2026-0001", responsibleList);
    }

    private static LearnerResponse givenResponse() {
        return new LearnerResponse(
                LEARNER_ID,
                NATURAL_PERSON_ID,
                INSTITUTION_ID,
                SCHOOL_GRADE_ID,
                true,
                ScholarshipCondition.PARTIAL,
                2026,
                "Maria da Silva",
                "MAT-2026-0001",
                LearnerStatus.ACTIVE,
                List.of(new ResponsibleResponse(UUID.randomUUID(), RESPONSIBLE_PERSON_ID, "João Pereira",
                        ResponsibleType.FINANCIAL, Kinship.FATHER, SCHOOL_GRADE_ID)),
                null);
    }

    @Test
    @WithMockCustomUser
    @DisplayName("POST /learner - Deve retornar 201 Created ao tornar a pessoa física em educando")
    void create_ShouldReturn201() throws Exception {
        when(createUseCase.handle(any(CreateLearnerCommand.class))).thenReturn(givenResponse());

        mockMvc.perform(post("/learner")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(givenCreateCommand())))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", containsString("/learner/" + LEARNER_ID)))
                .andExpect(jsonPath("$.id").value(LEARNER_ID.toString()))
                .andExpect(jsonPath("$.naturalPersonId").value(NATURAL_PERSON_ID.toString()))
                .andExpect(jsonPath("$.institutionId").value(INSTITUTION_ID.toString()))
                .andExpect(jsonPath("$.schoolGradeId").value(SCHOOL_GRADE_ID.toString()))
                .andExpect(jsonPath("$.academicYear").value(2026))
                .andExpect(jsonPath("$.scholarshipCondition").value("PARTIAL"))
                .andExpect(jsonPath("$.interestArts").value(true))
                .andExpect(jsonPath("$.schoolEnrollment").value("MAT-2026-0001"));
    }

    @Test
    @WithMockCustomUser
    @DisplayName("POST /learner - Deve devolver os responsáveis com nome, tipo e parentesco")
    void create_ShouldReturnResponsiblesWithNames() throws Exception {
        when(createUseCase.handle(any(CreateLearnerCommand.class))).thenReturn(givenResponse());

        mockMvc.perform(post("/learner")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(givenCreateCommand())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.responsibleList.length()").value(1))
                .andExpect(jsonPath("$.responsibleList[0].naturalPersonId")
                        .value(RESPONSIBLE_PERSON_ID.toString()))
                .andExpect(jsonPath("$.responsibleList[0].name").value("João Pereira"))
                .andExpect(jsonPath("$.responsibleList[0].type").value("FINANCIAL"))
                .andExpect(jsonPath("$.responsibleList[0].kinship").value("FATHER"));
    }

    @Test
    @WithMockCustomUser
    @DisplayName("POST /learner - Deve devolver a lista de responsáveis vazia quando não houver nenhum")
    void create_ShouldReturnEmptyResponsibleList() throws Exception {
        when(createUseCase.handle(any(CreateLearnerCommand.class)))
                .thenReturn(new LearnerResponse(LEARNER_ID, NATURAL_PERSON_ID, null, null,
                        false, null, 2026, null, null, LearnerStatus.ACTIVE, List.of(), null));

        mockMvc.perform(post("/learner")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(givenCreateCommand())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.responsibleList").isEmpty());
    }

    @Test
    @WithMockCustomUser
    @DisplayName("POST /learner - Deve retornar 400 quando a pessoa física do educando não for informada")
    void create_ShouldReturn400WhenNaturalPersonIsMissing() throws Exception {
        CreateLearnerCommand command = new CreateLearnerCommand(null, FAMILY_GROUP_ID, null, null,
                false, null, 2026, null, null, List.of());

        mockMvc.perform(post("/learner")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(command)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString("A pessoa física é obrigatória")))
                .andExpect(jsonPath("$.code").value("Bad Request"));

        verify(createUseCase, never()).handle(any());
    }

    @Test
    @WithMockCustomUser
    @DisplayName("POST /learner - Deve retornar 400 quando o grupo familiar não for informado")
    void create_ShouldReturn400WhenFamilyGroupIsMissing() throws Exception {
        CreateLearnerCommand command = new CreateLearnerCommand(NATURAL_PERSON_ID, null, null, null,
                false, null, 2026, null, null, List.of());

        mockMvc.perform(post("/learner")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(command)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString("O grupo familiar é obrigatório")));

        verify(createUseCase, never()).handle(any());
    }

    @Test
    @WithMockCustomUser
    @DisplayName("POST /learner - Deve retornar 400 quando um responsável não tiver tipo")
    void create_ShouldReturn400WhenResponsibleTypeIsMissing() throws Exception {
        CreateLearnerCommand command = new CreateLearnerCommand(NATURAL_PERSON_ID, FAMILY_GROUP_ID,
                null, null, false, null, 2026, null, null,
                List.of(new ResponsibleCommand(RESPONSIBLE_PERSON_ID, null, Kinship.FATHER, null)));

        mockMvc.perform(post("/learner")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(command)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString("O tipo do responsável é obrigatório")));

        verify(createUseCase, never()).handle(any());
    }

    @Test
    @WithMockCustomUser
    @DisplayName("POST /learner - Deve retornar 400 quando um responsável não tiver parentesco")
    void create_ShouldReturn400WhenResponsibleKinshipIsMissing() throws Exception {
        CreateLearnerCommand command = new CreateLearnerCommand(NATURAL_PERSON_ID, FAMILY_GROUP_ID,
                null, null, false, null, 2026, null, null,
                List.of(new ResponsibleCommand(RESPONSIBLE_PERSON_ID, ResponsibleType.FINANCIAL, null, null)));

        mockMvc.perform(post("/learner")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(command)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString("O parentesco do responsável é obrigatório")));

        verify(createUseCase, never()).handle(any());
    }

    @Test
    @WithMockCustomUser
    @DisplayName("POST /learner - Deve retornar 400 quando a matrícula escolar exceder o limite")
    void create_ShouldReturn400WhenEnrollmentIsTooLong() throws Exception {
        CreateLearnerCommand command = new CreateLearnerCommand(NATURAL_PERSON_ID, FAMILY_GROUP_ID,
                null, null, false, null, 2026, null, "a".repeat(256), List.of());

        mockMvc.perform(post("/learner")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(command)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString("A matrícula escolar não pode exceder 255 caracteres")));

        verify(createUseCase, never()).handle(any());
    }

    @Test
    @WithMockCustomUser
    @DisplayName("POST /learner - Deve retornar 409 quando a pessoa física já for educando")
    void create_ShouldReturn409WhenAlreadyLearner() throws Exception {
        when(createUseCase.handle(any(CreateLearnerCommand.class)))
                .thenThrow(new IllegalStateException("Esta pessoa física já está cadastrada como educando."));

        mockMvc.perform(post("/learner")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(givenCreateCommand())))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("Conflict"))
                .andExpect(jsonPath("$.message").value("Esta pessoa física já está cadastrada como educando."));
    }

    @Test
    @WithMockCustomUser
    @DisplayName("POST /learner - Deve retornar 404 quando o grupo familiar não existir")
    void create_ShouldReturn404WhenFamilyGroupNotFound() throws Exception {
        when(createUseCase.handle(any(CreateLearnerCommand.class)))
                .thenThrow(new ResourceNotFoundException("Grupo familiar não encontrado com ID: " + FAMILY_GROUP_ID));

        mockMvc.perform(post("/learner")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(givenCreateCommand())))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("Not Found"));
    }

    @Test
    @WithMockCustomUser
    @DisplayName("POST /learner - Deve retornar 400 quando o educando vier na própria lista de responsáveis")
    void create_ShouldReturn400WhenLearnerIsItsOwnResponsible() throws Exception {
        when(createUseCase.handle(any(CreateLearnerCommand.class)))
                .thenThrow(new InternalException("A pessoa física do educando não pode ser responsável de si mesma."));

        mockMvc.perform(post("/learner")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(givenCreateCommand())))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message")
                        .value("A pessoa física do educando não pode ser responsável de si mesma."));
    }

    @Test
    @WithMockCustomUser
    @DisplayName("POST /learner - Deve retornar 400 quando um responsável não pertencer ao grupo familiar")
    void create_ShouldReturn400WhenResponsibleIsNotFamilyMember() throws Exception {
        when(createUseCase.handle(any(CreateLearnerCommand.class)))
                .thenThrow(new InternalException("A pessoa física informada não pertence a este grupo familiar."));

        mockMvc.perform(post("/learner")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(givenCreateCommand())))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message")
                        .value("A pessoa física informada não pertence a este grupo familiar."));
    }

    @Test
    @WithMockCustomUser
    @DisplayName("PUT /learner/{id} - Deve retornar 200 ao atualizar o educando")
    void update_ShouldReturn200() throws Exception {
        when(updateUseCase.handle(any(UpdateLearnerCommand.class))).thenReturn(givenResponse());

        mockMvc.perform(put("/learner/{id}", LEARNER_ID)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(givenUpdateCommand(List.of()))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(LEARNER_ID.toString()))
                .andExpect(jsonPath("$.schoolEnrollment").value("MAT-2026-0001"));
    }

    @Test
    @WithMockCustomUser
    @DisplayName("PUT /learner/{id} - Deve usar o id da rota e não o id do corpo")
    void update_ShouldUseIdFromRoute() throws Exception {
        when(updateUseCase.handle(any(UpdateLearnerCommand.class))).thenReturn(givenResponse());

        mockMvc.perform(put("/learner/{id}", LEARNER_ID)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(givenUpdateCommand(List.of()))))
                .andExpect(status().isOk());

        ArgumentCaptor<UpdateLearnerCommand> captor = ArgumentCaptor.forClass(UpdateLearnerCommand.class);
        verify(updateUseCase).handle(captor.capture());

        assertEquals(LEARNER_ID, captor.getValue().id());
    }

    @Test
    @WithMockCustomUser
    @DisplayName("PUT /learner/{id} - Deve devolver a lista de responsáveis enviada, sem ids de vínculo")
    void update_ShouldSendResponsibleListWithoutBondIds() throws Exception {
        when(updateUseCase.handle(any(UpdateLearnerCommand.class))).thenReturn(givenResponse());

        ResponsibleCommand responsible = new ResponsibleCommand(RESPONSIBLE_PERSON_ID, ResponsibleType.FINANCIAL,
                Kinship.FATHER, SCHOOL_GRADE_ID);

        mockMvc.perform(put("/learner/{id}", LEARNER_ID)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(givenUpdateCommand(List.of(responsible)))))
                .andExpect(status().isOk());

        ArgumentCaptor<UpdateLearnerCommand> captor = ArgumentCaptor.forClass(UpdateLearnerCommand.class);
        verify(updateUseCase).handle(captor.capture());

        assertEquals(1, captor.getValue().responsibleList().size());
        assertEquals(RESPONSIBLE_PERSON_ID, captor.getValue().responsibleList().get(0).naturalPersonId());
        assertEquals(Kinship.FATHER, captor.getValue().responsibleList().get(0).kinship());
    }

    @Test
    @WithMockCustomUser
    @DisplayName("PUT /learner/{id} - Deve retornar 400 quando a lista de responsáveis não for informada")
    void update_ShouldReturn400WhenResponsibleListIsMissing() throws Exception {
        String body = """
                {
                  "naturalPersonId": "%s",
                  "familyGroupId": "%s",
                  "academicYear": 2026
                }
                """.formatted(NATURAL_PERSON_ID, FAMILY_GROUP_ID);

        mockMvc.perform(put("/learner/{id}", LEARNER_ID)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString("A lista de responsáveis é obrigatória")));

        verify(updateUseCase, never()).handle(any());
    }

    @Test
    @WithMockCustomUser
    @DisplayName("PUT /learner/{id} - Deve retornar 400 quando o educando já for responsável de si mesmo")
    void update_ShouldReturn400WhenLearnerIsItsOwnResponsible() throws Exception {
        when(updateUseCase.handle(any(UpdateLearnerCommand.class)))
                .thenThrow(new InternalException("A pessoa física do educando não pode ser responsável de si mesma."));

        mockMvc.perform(put("/learner/{id}", LEARNER_ID)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(givenUpdateCommand(List.of()))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message")
                        .value("A pessoa física do educando não pode ser responsável de si mesma."));
    }

    @Test
    @WithMockCustomUser
    @DisplayName("PUT /learner/{id} - Deve retornar 400 ao tentar trocar o educando de pessoa física")
    void update_ShouldReturn400WhenNaturalPersonChanges() throws Exception {
        when(updateUseCase.handle(any(UpdateLearnerCommand.class)))
                .thenThrow(new InternalException("O educando não pode ser transferido para outra pessoa física."));

        mockMvc.perform(put("/learner/{id}", LEARNER_ID)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(givenUpdateCommand(List.of()))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message")
                        .value("O educando não pode ser transferido para outra pessoa física."));
    }

    @Test
    @WithMockCustomUser
    @DisplayName("PUT /learner/{id} - Deve retornar 404 quando o educando não existir")
    void update_ShouldReturn404WhenNotFound() throws Exception {
        when(updateUseCase.handle(any(UpdateLearnerCommand.class)))
                .thenThrow(new ResourceNotFoundException("Educando não encontrado com ID: " + LEARNER_ID));

        mockMvc.perform(put("/learner/{id}", LEARNER_ID)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(givenUpdateCommand(List.of()))))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockCustomUser
    @DisplayName("GET /learner/{id} - Deve retornar 200 com o educando e seus responsáveis")
    void findById_ShouldReturn200() throws Exception {
        when(findByIdUseCase.handle(any(FindLearnerByIdQuery.class))).thenReturn(givenResponse());

        mockMvc.perform(get("/learner/{id}", LEARNER_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(LEARNER_ID.toString()))
                .andExpect(jsonPath("$.naturalPersonId").value(NATURAL_PERSON_ID.toString()))
                .andExpect(jsonPath("$.motherName").value("Maria da Silva"))
                .andExpect(jsonPath("$.responsibleList[0].naturalPersonId")
                        .value(RESPONSIBLE_PERSON_ID.toString()));
    }

    @Test
    @WithMockCustomUser
    @DisplayName("GET /learner/{id} - Deve retornar 404 quando o educando não existir")
    void findById_ShouldReturn404WhenNotFound() throws Exception {
        when(findByIdUseCase.handle(any(FindLearnerByIdQuery.class)))
                .thenThrow(new ResourceNotFoundException("Educando não encontrado com ID: " + LEARNER_ID));

        mockMvc.perform(get("/learner/{id}", LEARNER_ID))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("Not Found"))
                .andExpect(jsonPath("$.message").value("Educando não encontrado com ID: " + LEARNER_ID));
    }

    @Test
    @DisplayName("GET /learner/{id} - Deve retornar 401 Unauthorized sem autenticação")
    void findById_ShouldReturn401Unauthorized() throws Exception {
        mockMvc.perform(get("/learner/{id}", LEARNER_ID))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockCustomUser
    @DisplayName("GET /learner/eligible-responsibles - Deve retornar 200 com os responsáveis elegíveis")
    void findEligibleResponsibles_ShouldReturn200() throws Exception {
        EligibleResponsibleResponse responsible = new EligibleResponsibleResponse(
                RESPONSIBLE_PERSON_ID, "João Pereira", "123456789", "52998224725", "11911112222");

        when(findEligibleResponsiblesUseCase.handle(any(FindEligibleResponsiblesQuery.class)))
                .thenReturn(new PageImpl<>(List.of(responsible), PAGEABLE, 1));

        mockMvc.perform(get("/learner/eligible-responsibles")
                        .param("familyGroupId", FAMILY_GROUP_ID.toString())
                        .param("excludeNaturalPersonId", NATURAL_PERSON_ID.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].naturalPersonId").value(RESPONSIBLE_PERSON_ID.toString()))
                .andExpect(jsonPath("$.content[0].name").value("João Pereira"))
                .andExpect(jsonPath("$.content[0].cpf").value("52998224725"))
                .andExpect(jsonPath("$.content[0].cellphone").value("11911112222"));
    }

    @Test
    @WithMockCustomUser
    @DisplayName("GET /learner/eligible-responsibles - Deve repassar familyGroupId, excludeNaturalPersonId e paginação")
    void findEligibleResponsibles_ShouldPassQueryParams() throws Exception {
        when(findEligibleResponsiblesUseCase.handle(any(FindEligibleResponsiblesQuery.class)))
                .thenReturn(new PageImpl<>(List.of(), PAGEABLE, 0));

        mockMvc.perform(get("/learner/eligible-responsibles")
                        .param("familyGroupId", FAMILY_GROUP_ID.toString())
                        .param("excludeNaturalPersonId", NATURAL_PERSON_ID.toString())
                        .param("page", "2")
                        .param("size", "5"))
                .andExpect(status().isOk());

        ArgumentCaptor<FindEligibleResponsiblesQuery> captor =
                ArgumentCaptor.forClass(FindEligibleResponsiblesQuery.class);
        verify(findEligibleResponsiblesUseCase).handle(captor.capture());

        assertEquals(FAMILY_GROUP_ID, captor.getValue().familyGroupId());
        assertEquals(NATURAL_PERSON_ID, captor.getValue().excludeNaturalPersonId());
        assertEquals(2, captor.getValue().pageable().getPageNumber());
        assertEquals(5, captor.getValue().pageable().getPageSize());
        assertEquals(Sort.by("name").ascending(), captor.getValue().pageable().getSort());
    }

    @Test
    @WithMockCustomUser
    @DisplayName("GET /learner/eligible-responsibles - Deve usar paginação padrão quando não informada")
    void findEligibleResponsibles_ShouldUseDefaultPagination() throws Exception {
        when(findEligibleResponsiblesUseCase.handle(any(FindEligibleResponsiblesQuery.class)))
                .thenReturn(new PageImpl<>(List.of(), PAGEABLE, 0));

        mockMvc.perform(get("/learner/eligible-responsibles")
                        .param("familyGroupId", FAMILY_GROUP_ID.toString())
                        .param("excludeNaturalPersonId", NATURAL_PERSON_ID.toString()))
                .andExpect(status().isOk());

        ArgumentCaptor<FindEligibleResponsiblesQuery> captor =
                ArgumentCaptor.forClass(FindEligibleResponsiblesQuery.class);
        verify(findEligibleResponsiblesUseCase).handle(captor.capture());

        assertEquals(0, captor.getValue().pageable().getPageNumber());
        assertEquals(10, captor.getValue().pageable().getPageSize());
        assertEquals(Sort.by("name").ascending(), captor.getValue().pageable().getSort());
    }

    @Test
    @WithMockCustomUser
    @DisplayName("GET /learner/eligible-responsibles - Deve devolver lista vazia quando não houver candidatos")
    void findEligibleResponsibles_ShouldReturnEmptyList() throws Exception {
        when(findEligibleResponsiblesUseCase.handle(any(FindEligibleResponsiblesQuery.class)))
                .thenReturn(new PageImpl<>(List.of(), PAGEABLE, 0));

        mockMvc.perform(get("/learner/eligible-responsibles")
                        .param("familyGroupId", FAMILY_GROUP_ID.toString())
                        .param("excludeNaturalPersonId", NATURAL_PERSON_ID.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isEmpty());
    }

    @Test
    @WithMockCustomUser
    @DisplayName("GET /learner/eligible-responsibles - Deve retornar 404 quando o grupo familiar não existir")
    void findEligibleResponsibles_ShouldReturn404WhenFamilyGroupNotFound() throws Exception {
        when(findEligibleResponsiblesUseCase.handle(any(FindEligibleResponsiblesQuery.class)))
                .thenThrow(new ResourceNotFoundException("Grupo familiar não encontrado com ID: " + FAMILY_GROUP_ID));

        mockMvc.perform(get("/learner/eligible-responsibles")
                        .param("familyGroupId", FAMILY_GROUP_ID.toString())
                        .param("excludeNaturalPersonId", NATURAL_PERSON_ID.toString()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("Not Found"));
    }

    @Test
    @DisplayName("GET /learner/eligible-responsibles - Deve retornar 401 Unauthorized sem autenticação")
    void findEligibleResponsibles_ShouldReturn401Unauthorized() throws Exception {
        mockMvc.perform(get("/learner/eligible-responsibles")
                        .param("familyGroupId", FAMILY_GROUP_ID.toString())
                        .param("excludeNaturalPersonId", NATURAL_PERSON_ID.toString()))
                .andExpect(status().isUnauthorized());

        verify(findEligibleResponsiblesUseCase, never()).handle(any());
    }

    @Test
    @DisplayName("POST /learner - Deve retornar 401 Unauthorized sem autenticação")
    void create_ShouldReturn401Unauthorized() throws Exception {
        mockMvc.perform(post("/learner")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(givenCreateCommand())))
                .andExpect(status().isUnauthorized());

        verify(createUseCase, never()).handle(any());
    }

    @Test
    @WithMockCustomUser
    @DisplayName("DELETE /learner/{id} - Deve retornar 204 ao excluir o educando")
    void delete_ShouldReturn204() throws Exception {
        mockMvc.perform(delete("/learner/{id}", LEARNER_ID).with(csrf()))
                .andExpect(status().isNoContent());

        ArgumentCaptor<SoftDeleteLearnerCommand> captor = ArgumentCaptor.forClass(SoftDeleteLearnerCommand.class);
        verify(softDeleteUseCase).handle(captor.capture());

        assertEquals(LEARNER_ID, captor.getValue().id());
    }

    @Test
    @WithMockCustomUser
    @DisplayName("DELETE /learner/{id} - Deve retornar 404 quando o educando não existir")
    void delete_ShouldReturn404WhenNotFound() throws Exception {
        doThrow(new ResourceNotFoundException("Educando não encontrado com ID: " + LEARNER_ID))
                .when(softDeleteUseCase).handle(any());

        mockMvc.perform(delete("/learner/{id}", LEARNER_ID).with(csrf()))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockCustomUser
    @DisplayName("POST /learner/{id}/dropout - Deve retornar 200 com o educando desistente")
    void dropOut_ShouldReturn200() throws Exception {
        LearnerResponse dropped = new LearnerResponse(LEARNER_ID, NATURAL_PERSON_ID, INSTITUTION_ID,
                SCHOOL_GRADE_ID, true, ScholarshipCondition.PARTIAL, 2026, "Maria da Silva", "MAT-2026-0001",
                LearnerStatus.DROPPED_OUT, List.of(), null);

        when(dropoutUseCase.handle(any(DropoutLearnerCommand.class))).thenReturn(dropped);

        mockMvc.perform(post("/learner/{id}/dropout", LEARNER_ID)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"reason":"Mudou de cidade"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(LEARNER_ID.toString()))
                .andExpect(jsonPath("$.status").value("DROPPED_OUT"));
    }

    @Test
    @WithMockCustomUser
    @DisplayName("POST /learner/{id}/dropout - Deve usar o id da rota e repassar motivo e data")
    void dropOut_ShouldUseIdFromRouteAndPassFields() throws Exception {
        when(dropoutUseCase.handle(any(DropoutLearnerCommand.class))).thenReturn(givenResponse());

        mockMvc.perform(post("/learner/{id}/dropout", LEARNER_ID)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"id":"%s","reason":"Dificuldade de transporte","eventDate":"2026-05-01"}
                                """.formatted(UUID.randomUUID())))
                .andExpect(status().isOk());

        ArgumentCaptor<DropoutLearnerCommand> captor = ArgumentCaptor.forClass(DropoutLearnerCommand.class);
        verify(dropoutUseCase).handle(captor.capture());

        assertEquals(LEARNER_ID, captor.getValue().id());
        assertEquals("Dificuldade de transporte", captor.getValue().reason());
        assertEquals(LocalDate.of(2026, 5, 1), captor.getValue().eventDate());
    }

    @Test
    @WithMockCustomUser
    @DisplayName("POST /learner/{id}/dropout - Deve retornar 400 quando o motivo não for informado")
    void dropOut_ShouldReturn400WhenReasonIsMissing() throws Exception {
        mockMvc.perform(post("/learner/{id}/dropout", LEARNER_ID)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString("O motivo da desistência é obrigatório.")));

        verify(dropoutUseCase, never()).handle(any());
    }

    @Test
    @WithMockCustomUser
    @DisplayName("POST /learner/{id}/dropout - Deve retornar 409 quando o educando já estiver desistente")
    void dropOut_ShouldReturn409WhenAlreadyDropped() throws Exception {
        when(dropoutUseCase.handle(any(DropoutLearnerCommand.class)))
                .thenThrow(new IllegalStateException("O educando já está desistente."));

        mockMvc.perform(post("/learner/{id}/dropout", LEARNER_ID)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"reason":"Mudou de cidade"}
                                """))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("O educando já está desistente."));
    }

    @Test
    @WithMockCustomUser
    @DisplayName("POST /learner/{id}/dropout - Deve retornar 404 quando o educando não existir")
    void dropOut_ShouldReturn404WhenNotFound() throws Exception {
        when(dropoutUseCase.handle(any(DropoutLearnerCommand.class)))
                .thenThrow(new ResourceNotFoundException("Educando não encontrado com ID: " + LEARNER_ID));

        mockMvc.perform(post("/learner/{id}/dropout", LEARNER_ID)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"reason":"Mudou de cidade"}
                                """))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("POST /learner/{id}/dropout - Deve retornar 401 Unauthorized sem autenticação")
    void dropOut_ShouldReturn401Unauthorized() throws Exception {
        mockMvc.perform(post("/learner/{id}/dropout", LEARNER_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"reason":"Mudou de cidade"}
                                """))
                .andExpect(status().isUnauthorized());

        verify(dropoutUseCase, never()).handle(any());
    }

    @Test
    @WithMockCustomUser
    @DisplayName("POST /learner/{id}/reactivate - Deve retornar 200 com o educando ativo")
    void reactivate_ShouldReturn200() throws Exception {
        when(reactivateUseCase.handle(any(ReactivateLearnerCommand.class))).thenReturn(givenResponse());

        mockMvc.perform(post("/learner/{id}/reactivate", LEARNER_ID)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"note":"Retornou no próximo ano letivo"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(LEARNER_ID.toString()))
                .andExpect(jsonPath("$.status").value("ACTIVE"));
    }

    @Test
    @WithMockCustomUser
    @DisplayName("POST /learner/{id}/reactivate - Deve usar o id da rota e repassar a observação")
    void reactivate_ShouldUseIdFromRouteAndPassNote() throws Exception {
        when(reactivateUseCase.handle(any(ReactivateLearnerCommand.class))).thenReturn(givenResponse());

        mockMvc.perform(post("/learner/{id}/reactivate", LEARNER_ID)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"note":"Retornou no próximo ano letivo"}
                                """))
                .andExpect(status().isOk());

        ArgumentCaptor<ReactivateLearnerCommand> captor = ArgumentCaptor.forClass(ReactivateLearnerCommand.class);
        verify(reactivateUseCase).handle(captor.capture());

        assertEquals(LEARNER_ID, captor.getValue().id());
        assertEquals("Retornou no próximo ano letivo", captor.getValue().note());
    }

    @Test
    @WithMockCustomUser
    @DisplayName("POST /learner/{id}/reactivate - Deve aceitar corpo vazio")
    void reactivate_ShouldAcceptEmptyBody() throws Exception {
        when(reactivateUseCase.handle(any(ReactivateLearnerCommand.class))).thenReturn(givenResponse());

        mockMvc.perform(post("/learner/{id}/reactivate", LEARNER_ID)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockCustomUser
    @DisplayName("POST /learner/{id}/reactivate - Deve retornar 409 quando o educando não estiver desistente")
    void reactivate_ShouldReturn409WhenNotDropped() throws Exception {
        when(reactivateUseCase.handle(any(ReactivateLearnerCommand.class)))
                .thenThrow(new IllegalStateException("O educando não está desistente."));

        mockMvc.perform(post("/learner/{id}/reactivate", LEARNER_ID)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("O educando não está desistente."));
    }

    @Test
    @WithMockCustomUser
    @DisplayName("POST /learner/{id}/reactivate - Deve retornar 404 quando o educando não existir")
    void reactivate_ShouldReturn404WhenNotFound() throws Exception {
        when(reactivateUseCase.handle(any(ReactivateLearnerCommand.class)))
                .thenThrow(new ResourceNotFoundException("Educando não encontrado com ID: " + LEARNER_ID));

        mockMvc.perform(post("/learner/{id}/reactivate", LEARNER_ID)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockCustomUser
    @DisplayName("GET /learner - Deve retornar 200 com a lista de educandos")
    void findAll_ShouldReturn200() throws Exception {
        LearnerSummaryResponse summary = new LearnerSummaryResponse(
                LEARNER_ID, "Maria da Silva", "Escola Projeto Resgate", 2026, true);

        when(findAllUseCase.handle(any(FindAllLearnersQuery.class)))
                .thenReturn(new PageImpl<>(List.of(summary), PAGEABLE, 1));

        mockMvc.perform(get("/learner"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].id").value(LEARNER_ID.toString()))
                .andExpect(jsonPath("$.content[0].name").value("Maria da Silva"))
                .andExpect(jsonPath("$.content[0].institutionName").value("Escola Projeto Resgate"))
                .andExpect(jsonPath("$.content[0].academicYear").value(2026))
                .andExpect(jsonPath("$.content[0].active").value(true));
    }

    @Test
    @WithMockCustomUser
    @DisplayName("GET /learner - Deve repassar os filtros e a paginação")
    void findAll_ShouldPassFiltersAndPagination() throws Exception {
        when(findAllUseCase.handle(any(FindAllLearnersQuery.class)))
                .thenReturn(new PageImpl<>(List.of(), PAGEABLE, 0));

        mockMvc.perform(get("/learner")
                        .param("searchTerm", "Maria")
                        .param("status", "DROPPED_OUT")
                        .param("institutionId", INSTITUTION_ID.toString())
                        .param("schoolGradeId", SCHOOL_GRADE_ID.toString())
                        .param("academicYear", "2025")
                        .param("page", "2")
                        .param("size", "5"))
                .andExpect(status().isOk());

        ArgumentCaptor<FindAllLearnersQuery> captor = ArgumentCaptor.forClass(FindAllLearnersQuery.class);
        verify(findAllUseCase).handle(captor.capture());

        FindAllLearnersQuery query = captor.getValue();
        assertEquals("Maria", query.searchTerm());
        assertEquals(LearnerStatus.DROPPED_OUT, query.status());
        assertEquals(INSTITUTION_ID, query.institutionId());
        assertEquals(SCHOOL_GRADE_ID, query.schoolGradeId());
        assertEquals(2025, query.academicYear());
        assertEquals(2, query.pageable().getPageNumber());
        assertEquals(5, query.pageable().getPageSize());
    }

    @Test
    @WithMockCustomUser
    @DisplayName("GET /learner - Deve usar paginação padrão ordenada por data de criação quando não informada")
    void findAll_ShouldUseDefaultPagination() throws Exception {
        when(findAllUseCase.handle(any(FindAllLearnersQuery.class)))
                .thenReturn(new PageImpl<>(List.of(), PAGEABLE, 0));

        mockMvc.perform(get("/learner"))
                .andExpect(status().isOk());

        ArgumentCaptor<FindAllLearnersQuery> captor = ArgumentCaptor.forClass(FindAllLearnersQuery.class);
        verify(findAllUseCase).handle(captor.capture());

        assertEquals(0, captor.getValue().pageable().getPageNumber());
        assertEquals(10, captor.getValue().pageable().getPageSize());
        assertEquals(Sort.by("dateCreated").descending(), captor.getValue().pageable().getSort());
    }

    @Test
    @WithMockCustomUser
    @DisplayName("GET /learner - Deve devolver lista vazia quando não houver educandos")
    void findAll_ShouldReturnEmptyList() throws Exception {
        when(findAllUseCase.handle(any(FindAllLearnersQuery.class)))
                .thenReturn(new PageImpl<>(List.of(), PAGEABLE, 0));

        mockMvc.perform(get("/learner"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isEmpty());
    }

    @Test
    @DisplayName("GET /learner - Deve retornar 401 Unauthorized sem autenticação")
    void findAll_ShouldReturn401Unauthorized() throws Exception {
        mockMvc.perform(get("/learner"))
                .andExpect(status().isUnauthorized());

        verify(findAllUseCase, never()).handle(any());
    }
}