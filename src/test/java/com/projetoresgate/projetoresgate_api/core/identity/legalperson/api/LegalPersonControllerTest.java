package com.projetoresgate.projetoresgate_api.core.identity.legalperson.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.projetoresgate.projetoresgate_api.config.security.WithMockCustomUser;
import com.projetoresgate.projetoresgate_api.core.identity.address.domain.Address;
import com.projetoresgate.projetoresgate_api.core.identity.legalperson.domain.LegalPerson;
import com.projetoresgate.projetoresgate_api.core.identity.legalperson.domain.Representative;
import com.projetoresgate.projetoresgate_api.core.identity.legalperson.domain.enums.CompanyStatus;
import com.projetoresgate.projetoresgate_api.core.identity.legalperson.domain.enums.RegistrationStatus;
import com.projetoresgate.projetoresgate_api.core.identity.legalperson.usecase.*;
import com.projetoresgate.projetoresgate_api.core.identity.legalperson.usecase.command.AddressCommand;
import com.projetoresgate.projetoresgate_api.core.identity.legalperson.usecase.command.CreateLegalPersonCommand;
import com.projetoresgate.projetoresgate_api.core.identity.legalperson.usecase.command.SoftDeleteLegalPersonCommand;
import com.projetoresgate.projetoresgate_api.core.identity.legalperson.usecase.command.UpdateLegalPersonCommand;
import com.projetoresgate.projetoresgate_api.core.identity.legalperson.usecase.query.SearchLegalPersonQuery;
import com.projetoresgate.projetoresgate_api.core.identity.user.repository.UserRepository;
import com.projetoresgate.projetoresgate_api.infrastructure.exception.InternalException;
import com.projetoresgate.projetoresgate_api.infrastructure.exception.ResourceNotFoundException;
import com.projetoresgate.projetoresgate_api.infrastructure.security.SecurityConfigurations;
import com.projetoresgate.projetoresgate_api.infrastructure.services.ITokenService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(LegalPersonController.class)
@Import(SecurityConfigurations.class)
@DisplayName("LegalPersonController - Test")
class LegalPersonControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

@MockitoBean
private CreateLegalPersonUseCase createUseCase;

@MockitoBean
private UpdateLegalPersonUseCase updateUseCase;

@MockitoBean
private SoftDeleteLegalPersonUseCase softDeleteUseCase;

@MockitoBean
private FindLegalPersonByIdUseCase findByIdUseCase;

@MockitoBean
private SearchLegalPersonUseCase searchUseCase;

    @MockitoBean
    private UserDetailsService userDetailsService;

    @MockitoBean
    private ITokenService tokenService;

    @MockitoBean
    private UserRepository userRepository;

    @Test
    @WithMockCustomUser
    @DisplayName("POST /legal-person - Deve retornar 201 Created ao criar com sucesso")
    void create_ShouldReturn201() throws Exception {
        CreateLegalPersonCommand command = new CreateLegalPersonCommand(
                "Razão Social LTDA", "Nome Fantasia", "Display Name", "12345678000195", "6201-5/00",
                RegistrationStatus.ACTIVE, CompanyStatus.ACTIVE, buildAddress());

        LegalPerson person = createMockPerson();
        when(createUseCase.handle(any())).thenReturn(person);

        mockMvc.perform(post("/legal-person")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(command)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.cnpj").value("12345678000195"))
                .andExpect(jsonPath("$.corporateName").value("Razão Social LTDA"))
                .andExpect(jsonPath("$.registrationStatus").value("ACTIVE"))
                .andExpect(jsonPath("$.companyStatus").value("ACTIVE"));
    }

    @Test
    @WithMockCustomUser
    @DisplayName("POST /legal-person - Deve retornar 400 BadRequest ao criar com dados inválidos")
    void create_ShouldReturn400WhenInvalid() throws Exception {
        CreateLegalPersonCommand command = new CreateLegalPersonCommand(
                "", null, null, "12345678000195", null,
                RegistrationStatus.ACTIVE, CompanyStatus.ACTIVE, buildAddress());

        mockMvc.perform(post("/legal-person")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(command)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockCustomUser
    @DisplayName("POST /legal-person - Deve retornar 400 BadRequest quando use case lançar exceção")
    void create_ShouldReturn400OnBusinessRuleViolation() throws Exception {
        CreateLegalPersonCommand command = new CreateLegalPersonCommand(
                "Razão Social LTDA", null, null, "12345678000195", null,
                RegistrationStatus.ACTIVE, CompanyStatus.ACTIVE, buildAddress());

        when(createUseCase.handle(any())).thenThrow(new InternalException("Já existe uma empresa cadastrada com este CNPJ."));

        mockMvc.perform(post("/legal-person")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(command)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockCustomUser
    @DisplayName("PUT /legal-person/{id} - Deve retornar 200 OK ao atualizar com sucesso")
    void update_ShouldReturn200() throws Exception {
        UUID id = UUID.randomUUID();
        UpdateLegalPersonCommand command = new UpdateLegalPersonCommand(
                null, "Nova Razão Social", null, null, "12345678000195",
                null, RegistrationStatus.ACTIVE, CompanyStatus.ACTIVE, buildAddress());

        LegalPerson person = createMockPerson();
        when(updateUseCase.handle(any())).thenReturn(person);

        mockMvc.perform(put("/legal-person/{id}", id)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(command)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cnpj").value("12345678000195"))
                .andExpect(jsonPath("$.corporateName").value("Razão Social LTDA"));
    }

    @Test
    @WithMockCustomUser
    @DisplayName("PUT /legal-person/{id} - Deve retornar 400 BadRequest quando regra de negócio falhar")
    void update_ShouldReturn400OnBusinessRuleViolation() throws Exception {
        UUID id = UUID.randomUUID();
        UpdateLegalPersonCommand command = new UpdateLegalPersonCommand(
                null, "Nova Razão Social", null, null, "98765432000198",
                null, RegistrationStatus.ACTIVE, CompanyStatus.ACTIVE, buildAddress());

        when(updateUseCase.handle(any())).thenThrow(new InternalException("Já existe uma empresa cadastrada com este CNPJ."));

        mockMvc.perform(put("/legal-person/{id}", id)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(command)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockCustomUser
    @DisplayName("PUT /legal-person/{id} - Deve retornar 404 NotFound quando a pessoa jurídica não existir")
    void update_ShouldReturn404WhenNotFound() throws Exception {
        UUID id = UUID.randomUUID();
        UpdateLegalPersonCommand command = new UpdateLegalPersonCommand(
                null, "Nova Razão Social", null, null, "98765432000198",
                null, RegistrationStatus.ACTIVE, CompanyStatus.ACTIVE, buildAddress());

        when(updateUseCase.handle(any())).thenThrow(new ResourceNotFoundException("Pessoa jurídica não encontrada com ID: " + id));

        mockMvc.perform(put("/legal-person/{id}", id)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(command)))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockCustomUser
    @DisplayName("GET /legal-person/{id} - Deve retornar 200 OK e a pessoa jurídica")
    void findById_ShouldReturn200() throws Exception {
        UUID id = UUID.randomUUID();
        LegalPerson person = createMockPerson();

        when(findByIdUseCase.handle(any())).thenReturn(person);

        mockMvc.perform(get("/legal-person/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cnpj").value("12345678000195"))
                .andExpect(jsonPath("$.corporateName").value("Razão Social LTDA"))
                .andExpect(jsonPath("$.address.city").value("São Paulo"))
                .andExpect(jsonPath("$.representative.name").value("John Doe"))
                .andExpect(jsonPath("$.dateCreated").value("2025-06-15T14:30:00"));
    }

    @Test
    @WithMockCustomUser
    @DisplayName("GET /legal-person/{id} - Deve retornar 404 NotFound quando a pessoa jurídica não existir")
    void findById_ShouldReturn404WhenNotFound() throws Exception {
        UUID id = UUID.randomUUID();

        when(findByIdUseCase.handle(any())).thenThrow(new ResourceNotFoundException("Pessoa jurídica não encontrada com ID: " + id));

        mockMvc.perform(get("/legal-person/{id}", id))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockCustomUser
    @DisplayName("GET /legal-person - Deve retornar 200 OK com página de resultados")
    void search_ShouldReturn200WithPage() throws Exception {
        LegalPerson person = createMockPerson();
        Page<LegalPerson> pageResult = new PageImpl<>(List.of(person));

        when(searchUseCase.handle(any())).thenReturn(pageResult);

        mockMvc.perform(get("/legal-person")
                        .param("page", "0")
                        .param("size", "10")
                        .param("searchTerm", "Razão"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].cnpj").value("12345678000195"))
                .andExpect(jsonPath("$.totalElements").value(1));
    }

    @Test
    @WithMockCustomUser
    @DisplayName("GET /legal-person - Deve retornar 200 OK com filtros de status")
    void search_ShouldReturn200WithStatusFilters() throws Exception {
        LegalPerson person = createMockPerson();
        Page<LegalPerson> pageResult = new PageImpl<>(List.of(person));

        when(searchUseCase.handle(any())).thenReturn(pageResult);

        mockMvc.perform(get("/legal-person")
                        .param("page", "0")
                        .param("size", "10")
                        .param("registrationStatus", "ACTIVE")
                        .param("companyStatus", "ACTIVE"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].registrationStatus").value("ACTIVE"))
                .andExpect(jsonPath("$.content[0].companyStatus").value("ACTIVE"))
                .andExpect(jsonPath("$.totalElements").value(1));
    }

    @Test
    @WithMockCustomUser
    @DisplayName("GET /legal-person - Deve montar a query correta com filtros e ordenação por razão social")
    void search_ShouldPassCorrectQueryToUseCase() throws Exception {
        Pageable pageable = PageRequest.of(2, 25, Sort.by("corporateName").ascending());
        SearchLegalPersonQuery expectedQuery = new SearchLegalPersonQuery(
                "Razão", "11222333000181", "ACME LTDA", RegistrationStatus.SUSPENDED, CompanyStatus.INACTIVE, pageable);

        when(searchUseCase.handle(any())).thenReturn(new PageImpl<>(List.of()));

        mockMvc.perform(get("/legal-person")
                        .param("searchTerm", "Razão")
                        .param("cnpj", "11222333000181")
                        .param("corporateName", "ACME LTDA")
                        .param("registrationStatus", "SUSPENDED")
                        .param("companyStatus", "INACTIVE")
                        .param("page", "2")
                        .param("size", "25"))
                .andExpect(status().isOk());

        verify(searchUseCase).handle(eq(expectedQuery));
    }

    @Test
    @WithMockCustomUser
    @DisplayName("GET /legal-person - Deve usar os valores padrão de paginação e ordenação")
    void search_ShouldUseDefaultPaginationAndSorting() throws Exception {
        Pageable pageable = PageRequest.of(0, 10, Sort.by("corporateName").ascending());
        SearchLegalPersonQuery expectedQuery = new SearchLegalPersonQuery(
                null, null, null, null, null, pageable);

        when(searchUseCase.handle(any())).thenReturn(new PageImpl<>(List.of()));

        mockMvc.perform(get("/legal-person"))
                .andExpect(status().isOk());

        verify(searchUseCase).handle(eq(expectedQuery));
    }

    @Test
    @DisplayName("POST /legal-person - Deve retornar 401 Unauthorized sem autenticação")
    void create_ShouldReturn401WithoutAuth() throws Exception {
        CreateLegalPersonCommand command = new CreateLegalPersonCommand(
                "Razão Social LTDA", null, null, "12345678000195", null,
                RegistrationStatus.ACTIVE, CompanyStatus.ACTIVE, buildAddress());

        mockMvc.perform(post("/legal-person")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(command)))
                .andExpect(status().isUnauthorized());
    }

@Test
@WithMockCustomUser
@DisplayName("DELETE /legal-person/{id} - Deve retornar 204 No Content ao excluir")
void delete_ShouldReturn204() throws Exception {
    UUID id = UUID.randomUUID();

    mockMvc.perform(delete("/legal-person/{id}", id)
                    .with(csrf()))
            .andExpect(status().isNoContent());

    verify(softDeleteUseCase).handle(any(SoftDeleteLegalPersonCommand.class));
}

@Test
@WithMockCustomUser
@DisplayName("DELETE /legal-person/{id} - Deve retornar 404 NotFound quando a pessoa jurídica não existir")
void delete_ShouldReturn404WhenNotFound() throws Exception {
    UUID id = UUID.randomUUID();

    doThrow(new ResourceNotFoundException("Pessoa jurídica não encontrada com ID: " + id))
            .when(softDeleteUseCase).handle(any());

    mockMvc.perform(delete("/legal-person/{id}", id)
                    .with(csrf()))
            .andExpect(status().isNotFound());
}

@Test
@DisplayName("DELETE /legal-person/{id} - Deve retornar 401 Unauthorized sem autenticação")
void delete_ShouldReturn401WithoutAuth() throws Exception {
    UUID id = UUID.randomUUID();

    mockMvc.perform(delete("/legal-person/{id}", id))
            .andExpect(status().isUnauthorized());
}

@Test
@DisplayName("GET /legal-person/{id} - Deve retornar 401 Unauthorized sem autenticação")
void findById_ShouldReturn401WithoutAuth() throws Exception {
    UUID id = UUID.randomUUID();

    mockMvc.perform(get("/legal-person/{id}", id))
            .andExpect(status().isUnauthorized());
}

    private AddressCommand buildAddress() {
        return new AddressCommand("01310-100", "1000", null, "Bela Vista", "São Paulo", "SP");
    }

    private LegalPerson createMockPerson() {
        Address address = Address.create("01310-100", "1000", null, "Bela Vista", "São Paulo", "SP");
        Representative representative = Representative.create("John Doe", "11988887777", "1133334444", "john@doe.com");

        LegalPerson person = LegalPerson.create(
                "12345678000195", "Razão Social LTDA", "Nome Fantasia", "Display Name", "6201-5/00",
                RegistrationStatus.ACTIVE, CompanyStatus.ACTIVE, address, representative);
        person.setDateCreated(LocalDateTime.of(2025, 6, 15, 14, 30, 0));
        return person;
    }
}
