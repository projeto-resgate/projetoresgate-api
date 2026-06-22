package com.projetoresgate.projetoresgate_api.core.naturalperson.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.projetoresgate.projetoresgate_api.config.security.WithMockCustomUser;
import com.projetoresgate.projetoresgate_api.core.identity.naturalperson.api.NaturalPersonController;
import com.projetoresgate.projetoresgate_api.core.identity.naturalperson.domain.NaturalPerson;
import com.projetoresgate.projetoresgate_api.core.identity.naturalperson.domain.enums.Gender;
import com.projetoresgate.projetoresgate_api.core.identity.naturalperson.usecase.*;
import com.projetoresgate.projetoresgate_api.core.identity.naturalperson.usecase.command.CreateNaturalPersonCommand;
import com.projetoresgate.projetoresgate_api.core.identity.naturalperson.usecase.command.UpdateNaturalPersonCommand;
import com.projetoresgate.projetoresgate_api.core.identity.user.domain.User;
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

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(NaturalPersonController.class)
@Import(SecurityConfigurations.class)
@DisplayName("NaturalPersonController - Test")
class NaturalPersonControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private CreateNaturalPersonUseCase createUseCase;

    @MockitoBean
    private UpdateNaturalPersonUseCase updateUseCase;

    @MockitoBean
    private SoftDeleteNaturalPersonUseCase softDeleteUseCase;

    @MockitoBean
    private FindNaturalPersonByIdUseCase findByIdUseCase;

    @MockitoBean
    private FindNaturalPersonByUserIdUseCase findByUserIdUseCase;

    @MockitoBean
    private SearchNaturalPersonUseCase searchUseCase;

    @MockitoBean
    private UserDetailsService userDetailsService;

    @MockitoBean
    private ITokenService tokenService;

    @MockitoBean
    private UserRepository userRepository;

    @Test
    @DisplayName("POST /natural-person - Deve retornar 201 Created ao criar com sucesso")
    void create_ShouldReturn201() throws Exception {
        CreateNaturalPersonCommand command = new CreateNaturalPersonCommand(
                "John Doe", "john@test.com", "johny", "1234567", "51086174968",
                LocalDate.of(1990, 1, 1), "1133334444", "11988888888", Gender.MALE
        );

        NaturalPerson person = createMockPerson();
        when(createUseCase.handle(any())).thenReturn(person);

        mockMvc.perform(post("/natural-person")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(command)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.cpf").value("51086174968"))
                .andExpect(jsonPath("$.name").value("Test Name"));
    }

    @Test
    @WithMockCustomUser
    @DisplayName("PUT /natural-person/{id} - Deve retornar 200 OK ao atualizar com sucesso")
    void update_ShouldReturn200() throws Exception {
        UUID id = UUID.randomUUID();
        UpdateNaturalPersonCommand command = new UpdateNaturalPersonCommand(
                null, "Updated Name", "newnick", null, "51086174968", null, null, null, null);

        NaturalPerson person = createMockPerson();
        when(updateUseCase.handle(any())).thenReturn(person);

        mockMvc.perform(put("/natural-person/{id}", id)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(command)))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockCustomUser
    @DisplayName("DELETE /natural-person/{id} - Deve retornar 204 No Content ao deletar com sucesso")
    void delete_ShouldReturn204() throws Exception {
        UUID id = UUID.randomUUID();

        mockMvc.perform(delete("/natural-person/{id}", id)
                        .with(csrf()))
                .andExpect(status().isNoContent());
    }

    @Test
    @WithMockCustomUser
    @DisplayName("GET /natural-person/{id} - Deve retornar 200 OK e a pessoa física")
    void findById_ShouldReturn200() throws Exception {
        UUID id = UUID.randomUUID();
        NaturalPerson person = createMockPerson();

        when(findByIdUseCase.handle(any())).thenReturn(person);

        mockMvc.perform(get("/natural-person/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cpf").value("51086174968"));
    }

    @Test
    @WithMockCustomUser
    @DisplayName("GET /natural-person/user/{userId} - Deve retornar 200 OK e a pessoa física")
    void findByUserId_ShouldReturn200() throws Exception {
        UUID userId = UUID.randomUUID();
        NaturalPerson person = createMockPerson();

        when(findByUserIdUseCase.handle(any())).thenReturn(person);

        mockMvc.perform(get("/natural-person/user/{userId}", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cpf").value("51086174968"));
    }

    @Test
    @WithMockCustomUser
    @DisplayName("GET /natural-person - Deve retornar 200 OK com página de resultados")
    void search_ShouldReturn200WithPage() throws Exception {
        NaturalPerson person = createMockPerson();
        Page<NaturalPerson> pageResult = new PageImpl<>(List.of(person));

        when(searchUseCase.handle(any())).thenReturn(pageResult);

        mockMvc.perform(get("/natural-person")
                        .param("page", "0")
                        .param("size", "10")
                        .param("searchTerm", "Test"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].cpf").value("51086174968"))
                .andExpect(jsonPath("$.totalElements").value(1));
    }

    private NaturalPerson createMockPerson() {
        User user = User.create("test@test.com", "pass1234", "Test Name", "tester");

        return NaturalPerson.create(
                user, "51086174968", "1234567", LocalDate.of(1990, 1, 1),
                Gender.MALE, "11999999999", "11888888888"
        );
    }
}