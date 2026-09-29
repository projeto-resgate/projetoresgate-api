package com.projetoresgate.projetoresgate_api.core.identity.familygroup.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.projetoresgate.projetoresgate_api.config.security.WithMockCustomUser;
import com.projetoresgate.projetoresgate_api.core.identity.address.domain.Address;
import com.projetoresgate.projetoresgate_api.core.identity.address.api.command.AddressCommand;
import com.projetoresgate.projetoresgate_api.core.identity.familygroup.api.dto.FamilyGroupSummaryResponse;
import com.projetoresgate.projetoresgate_api.core.identity.familygroup.domain.FamilyGroup;
import com.projetoresgate.projetoresgate_api.core.identity.familygroup.usecase.CreateFamilyGroupUseCase;
import com.projetoresgate.projetoresgate_api.core.identity.familygroup.usecase.FindFamilyGroupByIdUseCase;
import com.projetoresgate.projetoresgate_api.core.identity.familygroup.usecase.FindFamilyGroupNaturalPersonsUseCase;
import com.projetoresgate.projetoresgate_api.core.identity.familygroup.usecase.SearchFamilyGroupUseCase;
import com.projetoresgate.projetoresgate_api.core.identity.familygroup.usecase.SoftDeleteFamilyGroupUseCase;
import com.projetoresgate.projetoresgate_api.core.identity.familygroup.usecase.UpdateFamilyGroupUseCase;
import com.projetoresgate.projetoresgate_api.core.identity.familygroup.usecase.command.CreateFamilyGroupCommand;
import com.projetoresgate.projetoresgate_api.core.identity.familygroup.usecase.command.UpdateFamilyGroupCommand;
import com.projetoresgate.projetoresgate_api.core.identity.naturalperson.domain.NaturalPerson;
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

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(FamilyGroupController.class)
@Import(SecurityConfigurations.class)
@DisplayName("FamilyGroupController - Test")
class FamilyGroupControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private CreateFamilyGroupUseCase createUseCase;

    @MockitoBean
    private UpdateFamilyGroupUseCase updateUseCase;

    @MockitoBean
    private FindFamilyGroupByIdUseCase findByIdUseCase;

    @MockitoBean
    private FindFamilyGroupNaturalPersonsUseCase findNaturalPersonsUseCase;

    @MockitoBean
    private SearchFamilyGroupUseCase searchUseCase;

    @MockitoBean
    private SoftDeleteFamilyGroupUseCase softDeleteUseCase;

    @MockitoBean
    private UserDetailsService userDetailsService;

    @MockitoBean
    private ITokenService tokenService;

    @MockitoBean
    private UserRepository userRepository;

    private AddressCommand buildAddress() {
        return new AddressCommand("Rua", "das Palmeiras", "01310-100", "1000", "Próximo à praça",
                "Apto 101", "Bela Vista", "São Paulo", "SP");
    }

    private FamilyGroup createMockFamilyGroup() {
        Address address = Address.create("Rua", "das Palmeiras", "01310-100", "1000", "Próximo à praça",
                "Apto 101", "Bela Vista", "São Paulo", "SP");
        return FamilyGroup.create(
                "FAM-1", "Família Silva",
                new BigDecimal("5000.00"),
                new BigDecimal("1250.00"),
                new BigDecimal("800.00"),
                new BigDecimal("400.00"),
                new BigDecimal("1500.00"),
                4,
                address);
    }

    @Test
    @WithMockCustomUser
    @DisplayName("POST /family-group - Deve retornar 201 Created ao criar com sucesso")
    void create_ShouldReturn201() throws Exception {
        CreateFamilyGroupCommand command = new CreateFamilyGroupCommand(
                "Família Silva",
                new BigDecimal("5000.00"),
                new BigDecimal("1250.00"),
                new BigDecimal("800.00"),
                new BigDecimal("400.00"),
                new BigDecimal("1500.00"),
                4,
                buildAddress());

        when(createUseCase.handle(any())).thenReturn(createMockFamilyGroup());

        mockMvc.perform(post("/family-group")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(command)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Família Silva"))
                .andExpect(jsonPath("$.householdIncome").value(5000.00))
                .andExpect(jsonPath("$.perCapitaIncome").value(1250.00))
                .andExpect(jsonPath("$.friendlyId").value("FAM-1"))
                .andExpect(jsonPath("$.numberOfResidents").value(4))
                .andExpect(jsonPath("$.address.id").isNotEmpty())
                .andExpect(jsonPath("$.address.streetName").value("das Palmeiras"))
                .andExpect(jsonPath("$.address.referencePoint").value("Próximo à praça"))
                .andExpect(jsonPath("$.naturalPersonList").doesNotExist());
    }

    @Test
    @WithMockCustomUser
    @DisplayName("POST /family-group - Deve retornar 400 Bad Request quando o nome for vazio")
    void create_ShouldReturn400WhenNameIsBlank() throws Exception {
        CreateFamilyGroupCommand command = new CreateFamilyGroupCommand(
                "", null, null, null, null, null, null, buildAddress());

        mockMvc.perform(post("/family-group")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(command)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockCustomUser
    @DisplayName("POST /family-group - Deve retornar 400 Bad Request quando já existir nome cadastrado")
    void create_ShouldReturn400WhenNameAlreadyExists() throws Exception {
        CreateFamilyGroupCommand command = new CreateFamilyGroupCommand(
                "Família Silva", null, null, null, null, null, null, null);

        when(createUseCase.handle(any()))
                .thenThrow(new InternalException("Já existe um grupo familiar cadastrado com este nome."));

        mockMvc.perform(post("/family-group")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(command)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockCustomUser
    @DisplayName("PUT /family-group/{id} - Deve retornar 200 OK ao atualizar com sucesso")
    void update_ShouldReturn200() throws Exception {
        UUID id = UUID.randomUUID();
        UpdateFamilyGroupCommand command = new UpdateFamilyGroupCommand(
                null, "Família Silva Souza", new BigDecimal("6000.00"), new BigDecimal("1500.00"),
                new BigDecimal("900.00"), new BigDecimal("500.00"), new BigDecimal("1600.00"), 5, buildAddress());

        when(updateUseCase.handle(any())).thenReturn(createMockFamilyGroup());

        mockMvc.perform(put("/family-group/{id}", id)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(command)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Família Silva"))
                .andExpect(jsonPath("$.numberOfResidents").value(4))
                .andExpect(jsonPath("$.address.streetName").value("das Palmeiras"))
                .andExpect(jsonPath("$.naturalPersonList").doesNotExist());
    }

    @Test
    @WithMockCustomUser
    @DisplayName("PUT /family-group/{id} - Deve retornar 404 Not Found quando não encontrado")
    void update_ShouldReturn404WhenNotFound() throws Exception {
        UUID id = UUID.randomUUID();
        UpdateFamilyGroupCommand command = new UpdateFamilyGroupCommand(
                null, "Família Silva", null, null, null, null, null, null, null);

        when(updateUseCase.handle(any()))
                .thenThrow(new ResourceNotFoundException("Grupo familiar não encontrado com ID: " + id));

        mockMvc.perform(put("/family-group/{id}", id)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(command)))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockCustomUser
    @DisplayName("GET /family-group - Deve retornar 200 OK com a lista paginada e o total de pessoas")
    void search_ShouldReturn200() throws Exception {
        Pageable pageable = PageRequest.of(0, 10, Sort.by("name").ascending());
        Page<FamilyGroupSummaryResponse> page =
                new PageImpl<>(List.of(new FamilyGroupSummaryResponse(UUID.randomUUID(), "FAM-1", "Família Silva", 3L)),
                        pageable, 1);

        when(searchUseCase.handle(any())).thenReturn(page);

        mockMvc.perform(get("/family-group"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].friendlyId").value("FAM-1"))
                .andExpect(jsonPath("$.content[0].name").value("Família Silva"))
                .andExpect(jsonPath("$.content[0].registeredPeopleCount").value(3))
                .andExpect(jsonPath("$.content[0].naturalPersonList").doesNotExist());
    }

    @Test
    @WithMockCustomUser
    @DisplayName("GET /family-group - Deve enviar os filtros de renda e número de moradores para o use case")
    void search_ShouldSendFiltersToUseCase() throws Exception {
        when(searchUseCase.handle(any())).thenReturn(new PageImpl<>(List.of()));

        mockMvc.perform(get("/family-group")
                        .param("searchTerm", "Silva")
                        .param("name", "Família")
                        .param("minHouseholdIncome", "1000.00")
                        .param("maxHouseholdIncome", "5000.00")
                        .param("minPerCapitaIncome", "500.00")
                        .param("maxPerCapitaIncome", "1500.00")
                        .param("numberOfResidents", "4")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk());

        verify(searchUseCase).handle(any());
    }

    @Test
    @WithMockCustomUser
    @DisplayName("GET /family-group/{id}/natural-person - Deve retornar 200 OK com as pessoas vinculadas")
    void findNaturalPersons_ShouldReturn200() throws Exception {
        UUID id = UUID.randomUUID();
        NaturalPerson daniela = NaturalPerson.create(
                "Daniela Ferreira", "daniela@email.com", null, "11144477735", "123456789",
                null, null, null, "11911112222");
        NaturalPerson breno = NaturalPerson.create(
                "Breno Ferreira", "breno@email.com", null, "11144477736", "223456789",
                null, null, null, "11911113333");

        when(findNaturalPersonsUseCase.handle(any())).thenReturn(List.of(daniela, breno));

        mockMvc.perform(get("/family-group/{id}/natural-person", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Daniela Ferreira"))
                .andExpect(jsonPath("$[0].cpf").value("11144477735"))
                .andExpect(jsonPath("$[0].rg").value("123456789"))
                .andExpect(jsonPath("$[0].cellphone").value("11911112222"))
                .andExpect(jsonPath("$[1].name").value("Breno Ferreira"));
    }

    @Test
    @WithMockCustomUser
    @DisplayName("GET /family-group/{id}/natural-person - Deve retornar 200 OK com lista vazia")
    void findNaturalPersons_ShouldReturnEmptyList() throws Exception {
        UUID id = UUID.randomUUID();

        when(findNaturalPersonsUseCase.handle(any())).thenReturn(List.of());

        mockMvc.perform(get("/family-group/{id}/natural-person", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isEmpty());
    }

    @Test
    @WithMockCustomUser
    @DisplayName("GET /family-group/{id}/natural-person - Deve retornar 404 Not Found quando o grupo não existir")
    void findNaturalPersons_ShouldReturn404WhenGroupNotFound() throws Exception {
        UUID id = UUID.randomUUID();

        when(findNaturalPersonsUseCase.handle(any()))
                .thenThrow(new ResourceNotFoundException("Grupo familiar não encontrado com ID: " + id));

        mockMvc.perform(get("/family-group/{id}/natural-person", id))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockCustomUser
    @DisplayName("GET /family-group/{id} - Deve retornar 200 OK e o grupo familiar com o endereço completo")
    void findById_ShouldReturn200() throws Exception {
        UUID id = UUID.randomUUID();

        when(findByIdUseCase.handle(any())).thenReturn(createMockFamilyGroup());

        mockMvc.perform(get("/family-group/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Família Silva"))
                .andExpect(jsonPath("$.numberOfResidents").value(4))
                .andExpect(jsonPath("$.address.id").isNotEmpty())
                .andExpect(jsonPath("$.address.streetType").value("Rua"))
                .andExpect(jsonPath("$.address.streetName").value("das Palmeiras"))
                .andExpect(jsonPath("$.address.number").value("1000"))
                .andExpect(jsonPath("$.address.referencePoint").value("Próximo à praça"))
                .andExpect(jsonPath("$.address.complement").value("Apto 101"))
                .andExpect(jsonPath("$.address.neighborhood").value("Bela Vista"))
                .andExpect(jsonPath("$.address.city").value("São Paulo"))
                .andExpect(jsonPath("$.address.state").value("SP"))
                .andExpect(jsonPath("$.address.zipCode").value("01310-100"))
                .andExpect(jsonPath("$.naturalPersonList").doesNotExist());
    }

    @Test
    @WithMockCustomUser
    @DisplayName("DELETE /family-group/{id} - Deve retornar 204 No Content")
    void softDelete_ShouldReturn204() throws Exception {
        UUID id = UUID.randomUUID();

        mockMvc.perform(delete("/family-group/{id}", id).with(csrf()))
                .andExpect(status().isNoContent());

        verify(softDeleteUseCase).handle(any());
    }

    @Test
    @WithMockCustomUser
    @DisplayName("DELETE /family-group/{id} - Deve retornar 404 Not Found quando não encontrado")
    void softDelete_ShouldReturn404WhenNotFound() throws Exception {
        UUID id = UUID.randomUUID();

        doThrow(new ResourceNotFoundException("Grupo familiar não encontrado com ID: " + id))
                .when(softDeleteUseCase).handle(any());

        mockMvc.perform(delete("/family-group/{id}", id).with(csrf()))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("DELETE /family-group/{id} - Deve retornar 401 Unauthorized sem autenticação")
    void softDelete_ShouldReturn401Unauthorized() throws Exception {
        mockMvc.perform(delete("/family-group/{id}", UUID.randomUUID()).with(csrf()))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockCustomUser
    @DisplayName("GET /family-group/{id} - Deve retornar 404 Not Found quando não encontrado")
    void findById_ShouldReturn404WhenNotFound() throws Exception {
        UUID id = UUID.randomUUID();

        when(findByIdUseCase.handle(any()))
                .thenThrow(new ResourceNotFoundException("Grupo familiar não encontrado com ID: " + id));

        mockMvc.perform(get("/family-group/{id}", id))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("GET /family-group/{id} - Deve retornar 401 Unauthorized sem autenticação")
    void findById_ShouldReturn401Unauthorized() throws Exception {
        mockMvc.perform(get("/family-group/{id}", UUID.randomUUID()))
                .andExpect(status().isUnauthorized());
    }
}
