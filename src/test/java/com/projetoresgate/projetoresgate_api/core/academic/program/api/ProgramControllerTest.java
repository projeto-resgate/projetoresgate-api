package com.projetoresgate.projetoresgate_api.core.academic.program.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.projetoresgate.projetoresgate_api.config.security.WithMockCustomUser;
import com.projetoresgate.projetoresgate_api.core.academic.program.api.dto.EducatorCategoryItemNameResponse;
import com.projetoresgate.projetoresgate_api.core.academic.program.api.dto.ProgramNameResponse;
import com.projetoresgate.projetoresgate_api.core.academic.program.api.dto.ProgramResponse;
import com.projetoresgate.projetoresgate_api.core.academic.program.domain.EducatorCategoryItem;
import com.projetoresgate.projetoresgate_api.core.academic.program.domain.Program;
import com.projetoresgate.projetoresgate_api.core.academic.program.domain.enums.ProgramStatus;
import com.projetoresgate.projetoresgate_api.core.academic.program.usecase.*;
import com.projetoresgate.projetoresgate_api.core.academic.program.usecase.command.CreateProgramCommand;
import com.projetoresgate.projetoresgate_api.core.academic.program.usecase.command.UpdateProgramCommand;
import com.projetoresgate.projetoresgate_api.core.identity.user.repository.UserRepository;
import com.projetoresgate.projetoresgate_api.infrastructure.security.SecurityConfigurations;
import com.projetoresgate.projetoresgate_api.infrastructure.services.ITokenService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.UUID;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = ProgramController.class)
@Import(SecurityConfigurations.class)
@DisplayName("ProgramController - Test")
class ProgramControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private CreateProgramUseCase createUseCase;

    @MockitoBean
    private UpdateProgramUseCase updateUseCase;

    @MockitoBean
    private SoftDeleteProgramUseCase softDeleteUseCase;

    @MockitoBean
    private FindProgramByIdUseCase findByIdUseCase;

    @MockitoBean
    private SearchProgramUseCase searchUseCase;

    @MockitoBean
    private FindProgramNamesUseCase findProgramNamesUseCase;

    @MockitoBean
    private FindEducatorCategoryItemNamesUseCase findEducatorCategoryItemNamesUseCase;

    @MockitoBean
    private UserDetailsService userDetailsService;

    @MockitoBean
    private ITokenService tokenService;

    @MockitoBean
    private UserRepository userRepository;

    @Test
    @WithMockCustomUser
    @DisplayName("POST /program - Deve retornar 201 Created ao criar com sucesso")
    void create_ShouldReturn201() throws Exception {
        CreateProgramCommand command = new CreateProgramCommand(
                "Programa A", "https://site.com", ProgramStatus.ACTIVE, null, null);

        ProgramResponse response = ProgramResponse.fromEntity(buildProgram("Programa A"), null);

        when(createUseCase.handle(any(CreateProgramCommand.class))).thenReturn(response);

        mockMvc.perform(post("/program")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(command)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Programa A"))
                .andExpect(jsonPath("$.status").value("ACTIVE"));
    }

    @Test
    @WithMockCustomUser
    @DisplayName("POST /program - Deve retornar 400 Bad Request quando validação falha")
    void create_ShouldReturn400WhenValidationFails() throws Exception {
        CreateProgramCommand command = new CreateProgramCommand("", "https://site.com", ProgramStatus.ACTIVE, null, null);

        mockMvc.perform(post("/program")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(command)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString("O nome é obrigatório")));

        verify(createUseCase, org.mockito.Mockito.never()).handle(any());
    }

    @Test
    @WithMockCustomUser
    @DisplayName("POST /program - Deve retornar 400 quando o item de categoria não tiver nome")
    void create_ShouldReturn400WhenCategoryItemValidationFails() throws Exception {
        CreateProgramCommand.CreateEducatorCategoryItemCommand invalidItem =
                new CreateProgramCommand.CreateEducatorCategoryItemCommand("");
        CreateProgramCommand command = new CreateProgramCommand(
                "Programa A", null, ProgramStatus.ACTIVE, null, List.of(invalidItem));

        mockMvc.perform(post("/program")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(command)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString("O nome da categoria é obrigatório")));
    }

    @Test
    @WithMockCustomUser
    @DisplayName("PUT /program/{id} - Deve retornar 200 OK ao atualizar com sucesso")
    void update_ShouldReturn200() throws Exception {
        UUID id = UUID.randomUUID();
        UpdateProgramCommand command = new UpdateProgramCommand(
                null, "Programa B", null, ProgramStatus.ACTIVE, null);

        ProgramResponse response = ProgramResponse.fromEntity(buildProgram("Programa B"), null);

        when(updateUseCase.handle(any(UpdateProgramCommand.class))).thenReturn(response);

        mockMvc.perform(put("/program/{id}", id)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(command)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Programa B"));
    }

    @Test
    @WithMockCustomUser
    @DisplayName("DELETE /program/{id} - Deve retornar 204 No Content ao deletar com sucesso")
    void delete_ShouldReturn204() throws Exception {
        UUID id = UUID.randomUUID();
        doNothing().when(softDeleteUseCase).handle(any());

        mockMvc.perform(delete("/program/{id}", id)
                        .with(csrf()))
                .andExpect(status().isNoContent());
    }

    @Test
    @WithMockCustomUser
    @DisplayName("GET /program - Deve retornar 200 OK com página de resultados")
    void search_ShouldReturn200WithPage() throws Exception {
        ProgramResponse response = ProgramResponse.fromEntity(buildProgram("Programa A"), null);
        Page<ProgramResponse> pageResult = new PageImpl<>(List.of(response));

        when(searchUseCase.handle(any())).thenReturn(pageResult);

        mockMvc.perform(get("/program")
                        .param("page", "0")
                        .param("size", "10")
                        .param("status", "ACTIVE"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].name").value("Programa A"))
                .andExpect(jsonPath("$.totalElements").value(1));
    }

    @Test
    @WithMockCustomUser
    @DisplayName("GET /program/{id} - Deve retornar 200 OK e os dados do programa")
    void findById_ShouldReturn200() throws Exception {
        UUID id = UUID.randomUUID();
        ProgramResponse response = ProgramResponse.fromEntity(buildProgram("Programa A"), null);

        when(findByIdUseCase.handle(any())).thenReturn(response);

        mockMvc.perform(get("/program/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(response.id().toString()))
                .andExpect(jsonPath("$.name").value("Programa A"));
    }

    @Test
    @WithMockCustomUser
    @DisplayName("GET /program/{id} - Deve retornar 200 OK com categorias de educador")
    void findById_ShouldReturn200WithEducatorCategoryItems() throws Exception {
        UUID id = UUID.randomUUID();
        Program program = Program.create("Programa A", null, ProgramStatus.ACTIVE, null);
        EducatorCategoryItem item = EducatorCategoryItem.create("Fonoaudiólogo", program);
        program.getEducatorCategoryItemList().add(item);
        ProgramResponse response = ProgramResponse.fromEntity(program, null);

        when(findByIdUseCase.handle(any())).thenReturn(response);

        mockMvc.perform(get("/program/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.educatorCategoryItems[0].name").value("Fonoaudiólogo"));
    }

    @Test
    @WithMockCustomUser
    @DisplayName("GET /program/names - Deve retornar 200 OK com lista de nomes")
    void findNames_ShouldReturn200() throws Exception {
        when(findProgramNamesUseCase.handle(any()))
                .thenReturn(List.of(new ProgramNameResponse("Programa A")));

        mockMvc.perform(get("/program/names")
                        .param("name", "Programa"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Programa A"));
    }

    @Test
    @WithMockCustomUser
    @DisplayName("GET /program/{id}/educator-category-items/names - Deve retornar 200 OK com lista de nomes")
    void findEducatorCategoryItemNames_ShouldReturn200() throws Exception {
        UUID id = UUID.randomUUID();
        when(findEducatorCategoryItemNamesUseCase.handle(any()))
                .thenReturn(List.of(new EducatorCategoryItemNameResponse("Fonoaudiólogo")));

        mockMvc.perform(get("/program/{id}/educator-category-items/names", id)
                        .param("name", "Fono"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Fonoaudiólogo"));
    }

    @Test
    @DisplayName("GET /program/{id} - Deve retornar 401 Unauthorized sem autenticação")
    void findById_ShouldReturn401Unauthorized() throws Exception {
        UUID id = UUID.randomUUID();

        mockMvc.perform(get("/program/{id}", id))
                .andExpect(status().isUnauthorized());
    }

    private Program buildProgram(String name) {
        return Program.create(name, "https://site.com", ProgramStatus.ACTIVE, null);
    }
}