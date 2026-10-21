package com.projetoresgate.projetoresgate_api.core.academic.collaborator.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.projetoresgate.projetoresgate_api.config.security.WithMockCustomUser;
import com.projetoresgate.projetoresgate_api.core.academic.collaborator.api.dto.CollaboratorAutocompleteResponse;
import com.projetoresgate.projetoresgate_api.core.academic.collaborator.api.dto.CollaboratorResponse;
import com.projetoresgate.projetoresgate_api.core.academic.collaborator.api.dto.CollaboratorSummaryResponse;
import com.projetoresgate.projetoresgate_api.core.academic.collaborator.usecase.AutocompleteCollaboratorUseCase;
import com.projetoresgate.projetoresgate_api.core.academic.collaborator.usecase.CreateCollaboratorUseCase;
import com.projetoresgate.projetoresgate_api.core.academic.collaborator.usecase.FindAllCollaboratorsUseCase;
import com.projetoresgate.projetoresgate_api.core.academic.collaborator.usecase.FindCollaboratorByIdUseCase;
import com.projetoresgate.projetoresgate_api.core.academic.collaborator.usecase.SoftDeleteCollaboratorUseCase;
import com.projetoresgate.projetoresgate_api.core.academic.collaborator.usecase.UpdateCollaboratorUseCase;
import com.projetoresgate.projetoresgate_api.core.academic.collaborator.usecase.command.CreateCollaboratorCommand;
import com.projetoresgate.projetoresgate_api.core.academic.collaborator.usecase.command.SoftDeleteCollaboratorCommand;
import com.projetoresgate.projetoresgate_api.core.academic.collaborator.usecase.command.UpdateCollaboratorCommand;
import com.projetoresgate.projetoresgate_api.core.academic.collaborator.usecase.query.FindAllCollaboratorsQuery;
import com.projetoresgate.projetoresgate_api.core.academic.collaborator.usecase.query.FindCollaboratorByIdQuery;
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
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = CollaboratorController.class)
@Import(SecurityConfigurations.class)
@DisplayName("CollaboratorController - Test")
class CollaboratorControllerTest {

    private static final UUID COLLABORATOR_ID = UUID.randomUUID();
    private static final UUID NATURAL_PERSON_ID = UUID.randomUUID();
    private static final UUID LEGAL_PERSON_ID = UUID.randomUUID();

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private CreateCollaboratorUseCase createUseCase;

    @MockitoBean
    private UpdateCollaboratorUseCase updateUseCase;

    @MockitoBean
    private SoftDeleteCollaboratorUseCase softDeleteUseCase;

    @MockitoBean
    private FindCollaboratorByIdUseCase findByIdUseCase;

    @MockitoBean
    private FindAllCollaboratorsUseCase findAllUseCase;

    @MockitoBean
    private AutocompleteCollaboratorUseCase autocompleteUseCase;

    @MockitoBean
    private UserDetailsService userDetailsService;

    @MockitoBean
    private ITokenService tokenService;

    @MockitoBean
    private UserRepository userRepository;

    private static CollaboratorResponse givenResponse() {
        return new CollaboratorResponse(
                COLLABORATOR_ID,
                NATURAL_PERSON_ID,
                "Ana Souza",
                "12345678900",
                LEGAL_PERSON_ID,
                "Empresa X LTDA",
                true,
                LocalDateTime.of(2026, 1, 10, 10, 0)
        );
    }

    @Test
    @WithMockCustomUser
    @DisplayName("POST /collaborator - Deve retornar 201 Created ao criar com sucesso")
    void create_ShouldReturn201() throws Exception {
        CreateCollaboratorCommand command = new CreateCollaboratorCommand(NATURAL_PERSON_ID, LEGAL_PERSON_ID, true);

        when(createUseCase.handle(any(CreateCollaboratorCommand.class))).thenReturn(givenResponse());

        mockMvc.perform(post("/collaborator")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(command)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(COLLABORATOR_ID.toString()))
                .andExpect(jsonPath("$.naturalPersonName").value("Ana Souza"))
                .andExpect(jsonPath("$.isVoluntary").value(true));
    }

    @Test
    @WithMockCustomUser
    @DisplayName("POST /collaborator - Deve retornar 400 quando campos obrigatórios faltarem")
    void create_ShouldReturn400WhenInvalid() throws Exception {
        mockMvc.perform(post("/collaborator")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());

        verify(createUseCase, never()).handle(any());
    }

    @Test
    @DisplayName("POST /collaborator - Deve retornar 401 sem autenticação")
    void create_ShouldReturn401() throws Exception {
        mockMvc.perform(post("/collaborator")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockCustomUser
    @DisplayName("PUT /collaborator/{id} - Deve usar o id da rota")
    void update_ShouldUseIdFromRoute() throws Exception {
        when(updateUseCase.handle(any(UpdateCollaboratorCommand.class))).thenReturn(givenResponse());

        UpdateCollaboratorCommand body = new UpdateCollaboratorCommand(null, NATURAL_PERSON_ID, null, false);

        mockMvc.perform(put("/collaborator/{id}", COLLABORATOR_ID)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isOk());

        ArgumentCaptor<UpdateCollaboratorCommand> captor = ArgumentCaptor.forClass(UpdateCollaboratorCommand.class);
        verify(updateUseCase).handle(captor.capture());
        assertEquals(COLLABORATOR_ID, captor.getValue().id());
    }

    @Test
    @WithMockCustomUser
    @DisplayName("GET /collaborator - Deve retornar 200 com a página")
    void findAll_ShouldReturnPage() throws Exception {
        CollaboratorSummaryResponse summary = new CollaboratorSummaryResponse(
                COLLABORATOR_ID, NATURAL_PERSON_ID, "Ana Souza", LEGAL_PERSON_ID, "Empresa X LTDA", true);
        Page<CollaboratorSummaryResponse> page = new PageImpl<>(List.of(summary), PageRequest.of(0, 10), 1);

        when(findAllUseCase.handle(any(FindAllCollaboratorsQuery.class))).thenReturn(page);

        mockMvc.perform(get("/collaborator").with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].naturalPersonName").value("Ana Souza"))
                .andExpect(jsonPath("$.totalElements").value(1));
    }

    @Test
    @WithMockCustomUser
    @DisplayName("GET /collaborator/autocomplete - Deve retornar 200 com a lista")
    void autocomplete_ShouldReturnList() throws Exception {
        CollaboratorAutocompleteResponse suggestion = new CollaboratorAutocompleteResponse(
                COLLABORATOR_ID, NATURAL_PERSON_ID, "Ana Souza", "12345678900", null, null, true);

        when(autocompleteUseCase.handle(any())).thenReturn(List.of(suggestion));

        mockMvc.perform(get("/collaborator/autocomplete").param("searchTerm", "ana").with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(COLLABORATOR_ID.toString()))
                .andExpect(jsonPath("$[0].naturalPersonName").value("Ana Souza"));
    }

    @Test
    @WithMockCustomUser
    @DisplayName("GET /collaborator/{id} - Deve retornar 200 com o colaborador")
    void findById_ShouldReturn200() throws Exception {
        when(findByIdUseCase.handle(any(FindCollaboratorByIdQuery.class))).thenReturn(givenResponse());

        mockMvc.perform(get("/collaborator/{id}", COLLABORATOR_ID).with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(COLLABORATOR_ID.toString()))
                .andExpect(jsonPath("$.legalPersonName").value("Empresa X LTDA"));
    }

    @Test
    @WithMockCustomUser
    @DisplayName("GET /collaborator/{id} - Deve retornar 404 quando não existir")
    void findById_ShouldReturn404() throws Exception {
        when(findByIdUseCase.handle(any(FindCollaboratorByIdQuery.class)))
                .thenThrow(new ResourceNotFoundException("Colaborador não encontrado com ID: " + COLLABORATOR_ID));

        mockMvc.perform(get("/collaborator/{id}", COLLABORATOR_ID).with(csrf()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value(containsString("Colaborador não encontrado")));
    }

    @Test
    @WithMockCustomUser
    @DisplayName("DELETE /collaborator/{id} - Deve retornar 204")
    void delete_ShouldReturn204() throws Exception {
        mockMvc.perform(delete("/collaborator/{id}", COLLABORATOR_ID).with(csrf()))
                .andExpect(status().isNoContent());

        ArgumentCaptor<SoftDeleteCollaboratorCommand> captor =
                ArgumentCaptor.forClass(SoftDeleteCollaboratorCommand.class);
        verify(softDeleteUseCase).handle(captor.capture());
        assertEquals(COLLABORATOR_ID, captor.getValue().id());
    }
}
