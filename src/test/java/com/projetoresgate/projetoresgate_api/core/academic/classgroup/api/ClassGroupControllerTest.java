package com.projetoresgate.projetoresgate_api.core.academic.classgroup.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.projetoresgate.projetoresgate_api.config.security.WithMockCustomUser;
import com.projetoresgate.projetoresgate_api.core.academic.classgroup.api.dto.ClassGroupResponse;
import com.projetoresgate.projetoresgate_api.core.academic.classgroup.api.dto.ClassGroupSummaryResponse;
import com.projetoresgate.projetoresgate_api.core.academic.classgroup.usecase.*;
import com.projetoresgate.projetoresgate_api.core.academic.classgroup.usecase.command.CreateClassGroupCommand;
import com.projetoresgate.projetoresgate_api.core.academic.classgroup.usecase.command.SoftDeleteClassGroupCommand;
import com.projetoresgate.projetoresgate_api.core.academic.classgroup.usecase.command.UpdateClassGroupCommand;
import com.projetoresgate.projetoresgate_api.core.academic.classgroup.usecase.query.FindClassGroupByIdQuery;
import com.projetoresgate.projetoresgate_api.core.academic.schoolgrade.api.dto.SchoolGradeNameResponse;
import com.projetoresgate.projetoresgate_api.core.identity.user.repository.UserRepository;
import com.projetoresgate.projetoresgate_api.infrastructure.exception.ResourceNotFoundException;
import com.projetoresgate.projetoresgate_api.infrastructure.security.SecurityConfigurations;
import com.projetoresgate.projetoresgate_api.infrastructure.services.ITokenService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = ClassGroupController.class)
@Import(SecurityConfigurations.class)
@DisplayName("ClassGroupController - Test")
class ClassGroupControllerTest {

    private static final UUID PROGRAM_ID = UUID.randomUUID();
    private static final UUID INSTITUTION_ID = UUID.randomUUID();
    private static final UUID CLASS_GROUP_ID = UUID.randomUUID();
    private static final UUID SCHOOL_GRADE_ID = UUID.randomUUID();

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private CreateClassGroupUseCase createUseCase;

    @MockitoBean
    private UpdateClassGroupUseCase updateUseCase;

    @MockitoBean
    private SoftDeleteClassGroupUseCase softDeleteUseCase;

    @MockitoBean
    private FindClassGroupByIdUseCase findByIdUseCase;

    @MockitoBean
    private FindAllClassGroupsUseCase findAllUseCase;

    @MockitoBean
    private UserDetailsService userDetailsService;

    @MockitoBean
    private ITokenService tokenService;

    @MockitoBean
    private UserRepository userRepository;

    private static ClassGroupResponse givenResponse() {
        return new ClassGroupResponse(
                CLASS_GROUP_ID,
                "Programa A - 1ª Série",
                PROGRAM_ID,
                "Programa A",
                INSTITUTION_ID,
                "Escola X",
                List.of(new SchoolGradeNameResponse(SCHOOL_GRADE_ID, "1ª Série", null, 1)),
                30,
                LocalDateTime.of(2026, 2, 1, 8, 0),
                LocalDateTime.of(2026, 8, 1, 8, 0),
                6,
                null,
                true,
                LocalDateTime.of(2026, 1, 10, 10, 0)
        );
    }

    @Test
    @WithMockCustomUser
    @DisplayName("POST /class-group - Deve retornar 201 Created ao criar com sucesso")
    void create_ShouldReturn201() throws Exception {
        CreateClassGroupCommand command = new CreateClassGroupCommand(
                PROGRAM_ID, INSTITUTION_ID, List.of(SCHOOL_GRADE_ID), 30,
                LocalDateTime.of(2026, 2, 1, 8, 0), 6, null);

        when(createUseCase.handle(any(CreateClassGroupCommand.class))).thenReturn(givenResponse());

        mockMvc.perform(post("/class-group")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(command)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(CLASS_GROUP_ID.toString()))
                .andExpect(jsonPath("$.title").value("Programa A - 1ª Série"))
                .andExpect(jsonPath("$.durationInMonths").value(6));
    }

    @Test
    @WithMockCustomUser
    @DisplayName("POST /class-group - Deve retornar 400 quando campos obrigatórios faltarem")
    void create_ShouldReturn400WhenInvalid() throws Exception {
        mockMvc.perform(post("/class-group")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());

        verify(createUseCase, never()).handle(any());
    }

    @Test
    @DisplayName("POST /class-group - Deve retornar 401 sem autenticação")
    void create_ShouldReturn401() throws Exception {
        mockMvc.perform(post("/class-group")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockCustomUser
    @DisplayName("PUT /class-group/{id} - Deve usar o id da rota")
    void update_ShouldUseIdFromRoute() throws Exception {
        when(updateUseCase.handle(any(UpdateClassGroupCommand.class))).thenReturn(givenResponse());

        UpdateClassGroupCommand body = new UpdateClassGroupCommand(
                null, null, null, null, 40, null, null, null, true);

        mockMvc.perform(put("/class-group/{id}", CLASS_GROUP_ID)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isOk());

        ArgumentCaptor<UpdateClassGroupCommand> captor = ArgumentCaptor.forClass(UpdateClassGroupCommand.class);
        verify(updateUseCase).handle(captor.capture());
        assertEquals(CLASS_GROUP_ID, captor.getValue().id());
    }

    @Test
    @WithMockCustomUser
    @DisplayName("GET /class-group - Deve retornar 200 com a página")
    void findAll_ShouldReturnPage() throws Exception {
        ClassGroupSummaryResponse summary = new ClassGroupSummaryResponse(
                CLASS_GROUP_ID, "Programa A - 1ª Série", PROGRAM_ID, "Programa A", INSTITUTION_ID, "Escola X", true);
        Page<ClassGroupSummaryResponse> page = new PageImpl<>(List.of(summary), PageRequest.of(0, 10), 1);

        when(findAllUseCase.handle(any())).thenReturn(page);

        mockMvc.perform(get("/class-group").with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].title").value("Programa A - 1ª Série"))
                .andExpect(jsonPath("$.totalElements").value(1));
    }

    @Test
    @WithMockCustomUser
    @DisplayName("GET /class-group/{id} - Deve retornar 200 com a turma")
    void findById_ShouldReturn200() throws Exception {
        when(findByIdUseCase.handle(any(FindClassGroupByIdQuery.class))).thenReturn(givenResponse());

        mockMvc.perform(get("/class-group/{id}", CLASS_GROUP_ID).with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(CLASS_GROUP_ID.toString()))
                .andExpect(jsonPath("$.schoolGrades[0].name").value("1ª Série"));
    }

    @Test
    @WithMockCustomUser
    @DisplayName("GET /class-group/{id} - Deve retornar 404 quando não existir")
    void findById_ShouldReturn404() throws Exception {
        when(findByIdUseCase.handle(any(FindClassGroupByIdQuery.class)))
                .thenThrow(new ResourceNotFoundException("Turma não encontrada com ID: " + CLASS_GROUP_ID));

        mockMvc.perform(get("/class-group/{id}", CLASS_GROUP_ID).with(csrf()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value(containsString("Turma não encontrada")));
    }

    @Test
    @WithMockCustomUser
    @DisplayName("DELETE /class-group/{id} - Deve retornar 204")
    void delete_ShouldReturn204() throws Exception {
        mockMvc.perform(delete("/class-group/{id}", CLASS_GROUP_ID).with(csrf()))
                .andExpect(status().isNoContent());

        ArgumentCaptor<SoftDeleteClassGroupCommand> captor =
                ArgumentCaptor.forClass(SoftDeleteClassGroupCommand.class);
        verify(softDeleteUseCase).handle(captor.capture());

        assertEquals(CLASS_GROUP_ID, captor.getValue().id());
    }
}
