package com.projetoresgate.projetoresgate_api.core.academic.schoolgrade.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.projetoresgate.projetoresgate_api.config.security.WithMockCustomUser;
import com.projetoresgate.projetoresgate_api.core.academic.schoolgrade.api.dto.SchoolGradeNameResponse;
import com.projetoresgate.projetoresgate_api.core.academic.schoolgrade.api.dto.SchoolGradeResponse;
import com.projetoresgate.projetoresgate_api.core.academic.schoolgrade.domain.SchoolGrade;
import com.projetoresgate.projetoresgate_api.core.academic.schoolgrade.usecase.*;
import com.projetoresgate.projetoresgate_api.core.academic.schoolgrade.usecase.command.CreateSchoolGradeCommand;
import com.projetoresgate.projetoresgate_api.core.academic.schoolgrade.usecase.command.UpdateSchoolGradeCommand;
import com.projetoresgate.projetoresgate_api.core.academic.schoolgrade.usecase.query.FindAllSchoolGradesQuery;
import com.projetoresgate.projetoresgate_api.core.academic.schoolgrade.usecase.query.FindSchoolGradeNamesQuery;
import com.projetoresgate.projetoresgate_api.core.identity.user.repository.UserRepository;
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
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.UUID;

import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = SchoolGradeController.class)
@Import(SecurityConfigurations.class)
@DisplayName("SchoolGradeController - Test")
class SchoolGradeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private CreateSchoolGradeUseCase createUseCase;

    @MockitoBean
    private UpdateSchoolGradeUseCase updateUseCase;

    @MockitoBean
    private SoftDeleteSchoolGradeUseCase softDeleteUseCase;

    @MockitoBean
    private FindSchoolGradeByIdUseCase findByIdUseCase;

    @MockitoBean
    private FindAllSchoolGradesUseCase findAllUseCase;

    @MockitoBean
    private FindSchoolGradeNamesUseCase findNamesUseCase;

    @MockitoBean
    private UserDetailsService userDetailsService;

    @MockitoBean
    private ITokenService tokenService;

    @MockitoBean
    private UserRepository userRepository;

    @Test
    @WithMockCustomUser
    @DisplayName("POST /school-grade - Deve retornar 201 Created ao criar com sucesso")
    void create_ShouldReturn201() throws Exception {
        CreateSchoolGradeCommand command = new CreateSchoolGradeCommand("Primeiro ano do ensino médio", 1);
        when(createUseCase.handle(any(CreateSchoolGradeCommand.class)))
                .thenReturn(SchoolGrade.create("Primeiro ano do ensino médio", 1));

        mockMvc.perform(post("/school-grade")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(command)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Primeiro ano do ensino médio"))
                .andExpect(jsonPath("$.gradeOrder").value(1));
    }

    @Test
    @WithMockCustomUser
    @DisplayName("POST /school-grade - Deve retornar 400 quando o nome estiver em branco")
    void create_ShouldReturn400WhenNameIsBlank() throws Exception {
        CreateSchoolGradeCommand command = new CreateSchoolGradeCommand("  ", 1);

        mockMvc.perform(post("/school-grade")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(command)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString("O nome é obrigatório")));

        verify(createUseCase, never()).handle(any());
    }

    @Test
    @WithMockCustomUser
    @DisplayName("POST /school-grade - Deve retornar 400 quando a ordem for zero")
    void create_ShouldReturn400WhenGradeOrderIsNotPositive() throws Exception {
        CreateSchoolGradeCommand command = new CreateSchoolGradeCommand("Primeiro ano", 0);

        mockMvc.perform(post("/school-grade")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(command)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString("A ordem deve ser maior que zero")));

        verify(createUseCase, never()).handle(any());
    }

    @Test
    @WithMockCustomUser
    @DisplayName("POST /school-grade - Deve retornar 400 quando a ordem não for informada")
    void create_ShouldReturn400WhenGradeOrderIsMissing() throws Exception {
        CreateSchoolGradeCommand command = new CreateSchoolGradeCommand("Primeiro ano", null);

        mockMvc.perform(post("/school-grade")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(command)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString("A ordem é obrigatória")));

        verify(createUseCase, never()).handle(any());
    }

    @Test
    @WithMockCustomUser
    @DisplayName("PUT /school-grade/{id} - Deve retornar 200 OK e repassar o id da rota ao caso de uso")
    void update_ShouldReturn200AndForwardId() throws Exception {
        UUID id = UUID.randomUUID();
        UpdateSchoolGradeCommand command = new UpdateSchoolGradeCommand(null, "Segundo ano", 2);
        when(updateUseCase.handle(any(UpdateSchoolGradeCommand.class)))
                .thenReturn(SchoolGrade.create("Segundo ano", 2));

        mockMvc.perform(put("/school-grade/{id}", id)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(command)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Segundo ano"))
                .andExpect(jsonPath("$.gradeOrder").value(2));

        ArgumentCaptor<UpdateSchoolGradeCommand> captor = ArgumentCaptor.forClass(UpdateSchoolGradeCommand.class);
        verify(updateUseCase).handle(captor.capture());
        assertEquals(id, captor.getValue().id());
    }

    @Test
    @WithMockCustomUser
    @DisplayName("PUT /school-grade/{id} - Não deve aceitar id no corpo e deixar o corpo sobrepor a rota")
    void update_ShouldIgnoreBodyId() throws Exception {
        UUID routeId = UUID.randomUUID();
        UUID bodyId = UUID.randomUUID();
        when(updateUseCase.handle(any(UpdateSchoolGradeCommand.class)))
                .thenReturn(SchoolGrade.create("Segundo ano", 2));

        mockMvc.perform(put("/school-grade/{id}", routeId)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"id":"%s","name":"Segundo ano","gradeOrder":2}
                                """.formatted(bodyId)))
                .andExpect(status().isOk());

        ArgumentCaptor<UpdateSchoolGradeCommand> captor = ArgumentCaptor.forClass(UpdateSchoolGradeCommand.class);
        verify(updateUseCase).handle(captor.capture());
        assertEquals(routeId, captor.getValue().id());
    }

    @Test
    @WithMockCustomUser
    @DisplayName("GET /school-grade - Deve retornar 200 OK com a página de séries")
    void findAll_ShouldReturn200() throws Exception {
        SchoolGradeResponse response = SchoolGradeResponse.fromEntity(SchoolGrade.create("Primeiro ano", 1));
        when(findAllUseCase.handle(any(FindAllSchoolGradesQuery.class)))
                .thenReturn(new PageImpl<>(List.of(response)));

        mockMvc.perform(get("/school-grade")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].name").value("Primeiro ano"))
                .andExpect(jsonPath("$.content[0].gradeOrder").value(1))
                .andExpect(jsonPath("$.totalElements").value(1));
    }

    @Test
    @WithMockCustomUser
    @DisplayName("GET /school-grade - Deve repassar os filtros e ordenar pela ordem da série")
    void findAll_ShouldForwardFiltersAndSort() throws Exception {
        when(findAllUseCase.handle(any(FindAllSchoolGradesQuery.class))).thenReturn(Page.empty());

        mockMvc.perform(get("/school-grade")
                        .param("name", "primeiro")
                        .param("gradeOrder", "1")
                        .param("page", "1")
                        .param("size", "5"))
                .andExpect(status().isOk());

        ArgumentCaptor<FindAllSchoolGradesQuery> captor = ArgumentCaptor.forClass(FindAllSchoolGradesQuery.class);
        verify(findAllUseCase).handle(captor.capture());

        FindAllSchoolGradesQuery query = captor.getValue();
        assertEquals("primeiro", query.name());
        assertEquals(1, query.gradeOrder());

        Pageable pageable = query.pageable();
        assertEquals(1, pageable.getPageNumber());
        assertEquals(5, pageable.getPageSize());
        assertEquals("gradeOrder", pageable.getSort().getOrderFor("gradeOrder").getProperty());
    }

    @Test
    @WithMockCustomUser
    @DisplayName("GET /school-grade - Deve aplicar os valores padrão de page e size")
    void findAll_ShouldApplyDefaultPagination() throws Exception {
        when(findAllUseCase.handle(any(FindAllSchoolGradesQuery.class))).thenReturn(Page.empty());

        mockMvc.perform(get("/school-grade"))
                .andExpect(status().isOk());

        ArgumentCaptor<FindAllSchoolGradesQuery> captor = ArgumentCaptor.forClass(FindAllSchoolGradesQuery.class);
        verify(findAllUseCase).handle(captor.capture());

        assertEquals(0, captor.getValue().pageable().getPageNumber());
        assertEquals(10, captor.getValue().pageable().getPageSize());
    }

    @Test
    @WithMockCustomUser
    @DisplayName("GET /school-grade/names - Deve retornar 200 OK com a lista de nomes")
    void findNames_ShouldReturn200() throws Exception {
        UUID id = UUID.randomUUID();
        when(findNamesUseCase.handle(any(FindSchoolGradeNamesQuery.class)))
                .thenReturn(List.of(new SchoolGradeNameResponse(id, "Primeiro ano", 1)));

        mockMvc.perform(get("/school-grade/names")
                        .param("name", "Primeiro")
                        .param("limit", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(id.toString()))
                .andExpect(jsonPath("$[0].name").value("Primeiro ano"))
                .andExpect(jsonPath("$[0].gradeOrder").value(1))
                .andExpect(jsonPath("$[0].dateCreated").doesNotExist());

        ArgumentCaptor<FindSchoolGradeNamesQuery> captor = ArgumentCaptor.forClass(FindSchoolGradeNamesQuery.class);
        verify(findNamesUseCase).handle(captor.capture());
        assertEquals("Primeiro", captor.getValue().name());
        assertEquals(5, captor.getValue().limit());
    }

    @Test
    @WithMockCustomUser
    @DisplayName("GET /school-grade/names - Deve assumir name vazio e limit 10 quando não informados")
    void findNames_ShouldApplyDefaults() throws Exception {
        when(findNamesUseCase.handle(any(FindSchoolGradeNamesQuery.class))).thenReturn(List.of());

        mockMvc.perform(get("/school-grade/names"))
                .andExpect(status().isOk());

        ArgumentCaptor<FindSchoolGradeNamesQuery> captor = ArgumentCaptor.forClass(FindSchoolGradeNamesQuery.class);
        verify(findNamesUseCase).handle(captor.capture());
        assertEquals("", captor.getValue().name());
        assertEquals(10, captor.getValue().limit());
    }

    @Test
    @WithMockCustomUser
    @DisplayName("GET /school-grade/{id} - Deve retornar 200 OK com os dados da série")
    void findById_ShouldReturn200() throws Exception {
        UUID id = UUID.randomUUID();
        SchoolGrade schoolGrade = SchoolGrade.create("Primeiro ano", 1);
        when(findByIdUseCase.handle(any())).thenReturn(schoolGrade);

        mockMvc.perform(get("/school-grade/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Primeiro ano"))
                .andExpect(jsonPath("$.gradeOrder").value(1))
                .andExpect(jsonPath("$.deletedAt").doesNotExist());
    }

    @Test
    @WithMockCustomUser
    @DisplayName("DELETE /school-grade/{id} - Deve retornar 204 No Content ao excluir com sucesso")
    void delete_ShouldReturn204() throws Exception {
        UUID id = UUID.randomUUID();
        doNothing().when(softDeleteUseCase).handle(any());

        mockMvc.perform(delete("/school-grade/{id}", id).with(csrf()))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("GET /school-grade - Deve retornar 401 Unauthorized sem autenticação")
    void findAll_ShouldReturn401Unauthorized() throws Exception {
        mockMvc.perform(get("/school-grade"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("POST /school-grade - Deve retornar 401 Unauthorized sem autenticação")
    void create_ShouldReturn401Unauthorized() throws Exception {
        CreateSchoolGradeCommand command = new CreateSchoolGradeCommand("Primeiro ano", 1);

        mockMvc.perform(post("/school-grade")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(command)))
                .andExpect(status().isUnauthorized());
    }
}
