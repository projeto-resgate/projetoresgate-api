package com.projetoresgate.projetoresgate_api.core.identity.familygroup.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.projetoresgate.projetoresgate_api.config.security.WithMockCustomUser;
import com.projetoresgate.projetoresgate_api.core.identity.address.domain.Address;
import com.projetoresgate.projetoresgate_api.core.identity.address.api.command.AddressCommand;
import com.projetoresgate.projetoresgate_api.core.identity.familygroup.api.dto.FamilyGroupNaturalPersonResponse;
import com.projetoresgate.projetoresgate_api.core.identity.familygroup.api.dto.FamilyGroupSummaryResponse;
import com.projetoresgate.projetoresgate_api.core.identity.familygroup.domain.FamilyGroup;
import com.projetoresgate.projetoresgate_api.core.identity.familygroup.usecase.AddNaturalPersonToFamilyGroupUseCase;
import com.projetoresgate.projetoresgate_api.core.identity.familygroup.usecase.AddNewNaturalPersonToFamilyGroupUseCase;
import com.projetoresgate.projetoresgate_api.core.identity.familygroup.usecase.CreateFamilyGroupUseCase;
import com.projetoresgate.projetoresgate_api.core.identity.familygroup.usecase.FindFamilyGroupByIdUseCase;
import com.projetoresgate.projetoresgate_api.core.identity.familygroup.usecase.FindFamilyGroupNaturalPersonsUseCase;
import com.projetoresgate.projetoresgate_api.core.identity.familygroup.usecase.RemoveNaturalPersonFromFamilyGroupUseCase;
import com.projetoresgate.projetoresgate_api.core.identity.familygroup.usecase.FindAllFamilyGroupsUseCase;
import com.projetoresgate.projetoresgate_api.core.identity.familygroup.usecase.SoftDeleteFamilyGroupUseCase;
import com.projetoresgate.projetoresgate_api.core.identity.familygroup.usecase.UpdateFamilyGroupUseCase;
import com.projetoresgate.projetoresgate_api.core.identity.familygroup.usecase.command.AddNaturalPersonToFamilyGroupCommand;
import com.projetoresgate.projetoresgate_api.core.identity.familygroup.usecase.command.AddNewNaturalPersonToFamilyGroupCommand;
import com.projetoresgate.projetoresgate_api.core.identity.familygroup.usecase.command.CreateFamilyGroupCommand;
import com.projetoresgate.projetoresgate_api.core.identity.familygroup.usecase.command.RemoveNaturalPersonFromFamilyGroupCommand;
import com.projetoresgate.projetoresgate_api.core.identity.familygroup.usecase.command.UpdateFamilyGroupCommand;
import com.projetoresgate.projetoresgate_api.core.identity.familygroup.usecase.query.FindFamilyGroupNaturalPersonsQuery;
import com.projetoresgate.projetoresgate_api.core.identity.familygroup.usecase.query.FindAllFamilyGroupsQuery;
import com.projetoresgate.projetoresgate_api.core.identity.naturalperson.domain.NaturalPerson;
import com.projetoresgate.projetoresgate_api.core.identity.naturalperson.usecase.command.CreateNaturalPersonCommand;
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(FamilyGroupController.class)
@Import(SecurityConfigurations.class)
@DisplayName("FamilyGroupController - Test")
class FamilyGroupControllerTest {

    private static final Pageable PAGEABLE = PageRequest.of(0, 10, Sort.by("name").ascending());

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
    private FindAllFamilyGroupsUseCase findAllUseCase;

    @MockitoBean
    private SoftDeleteFamilyGroupUseCase softDeleteUseCase;

    @MockitoBean
    private AddNaturalPersonToFamilyGroupUseCase addNaturalPersonUseCase;

    @MockitoBean
    private RemoveNaturalPersonFromFamilyGroupUseCase removeNaturalPersonUseCase;

    @MockitoBean
    private AddNewNaturalPersonToFamilyGroupUseCase addNewNaturalPersonUseCase;

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

        when(findAllUseCase.handle(any())).thenReturn(page);

        mockMvc.perform(get("/family-group"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].friendlyId").value("FAM-1"))
                .andExpect(jsonPath("$.content[0].name").value("Família Silva"))
                .andExpect(jsonPath("$.content[0].registeredPeopleCount").value(3))
                .andExpect(jsonPath("$.content[0].naturalPersonList").doesNotExist());
    }

    @Test
    @WithMockCustomUser
    @DisplayName("GET /family-group - Deve enviar o nome e a paginação para o use case")
    void search_ShouldSendFiltersToUseCase() throws Exception {
        when(findAllUseCase.handle(any())).thenReturn(new PageImpl<>(List.of()));

        mockMvc.perform(get("/family-group")
                        .param("name", "Família")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk());

        ArgumentCaptor<FindAllFamilyGroupsQuery> captor = ArgumentCaptor.forClass(FindAllFamilyGroupsQuery.class);
        verify(findAllUseCase).handle(captor.capture());

        assertEquals("Família", captor.getValue().name());
        assertEquals(PageRequest.of(0, 10, Sort.by("name").ascending()), captor.getValue().pageable());
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

        when(findNaturalPersonsUseCase.handle(any())).thenReturn(
                new PageImpl<>(List.of(
                        FamilyGroupNaturalPersonResponse.fromEntity(daniela),
                        FamilyGroupNaturalPersonResponse.fromEntity(breno)), PAGEABLE, 2));

        mockMvc.perform(get("/family-group/{id}/natural-person", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(2))
                .andExpect(jsonPath("$.size").value(10))
                .andExpect(jsonPath("$.content[0].id").value(daniela.getId().toString()))
                .andExpect(jsonPath("$.content[0].name").value("Daniela Ferreira"))
                .andExpect(jsonPath("$.content[0].cpf").value("11144477735"))
                .andExpect(jsonPath("$.content[0].rg").value("123456789"))
                .andExpect(jsonPath("$.content[0].cellphone").value("11911112222"))
                .andExpect(jsonPath("$.content[1].name").value("Breno Ferreira"))
                .andExpect(jsonPath("$.content[0].email").doesNotExist())
                .andExpect(jsonPath("$.content[0].nickname").doesNotExist())
                .andExpect(jsonPath("$.content[0].birthDate").doesNotExist())
                .andExpect(jsonPath("$.content[0].phone").doesNotExist())
                .andExpect(jsonPath("$.content[0].gender").doesNotExist())
                .andExpect(jsonPath("$.content[0].dateCreated").doesNotExist());
    }

    @Test
    @WithMockCustomUser
    @DisplayName("GET /family-group/{id}/natural-person - Deve repassar page e size e ordenar por nome")
    void findNaturalPersons_ShouldPassPaginationParams() throws Exception {
        UUID id = UUID.randomUUID();

        when(findNaturalPersonsUseCase.handle(any())).thenReturn(new PageImpl<>(List.of(), PAGEABLE, 0));

        mockMvc.perform(get("/family-group/{id}/natural-person", id)
                        .param("page", "2")
                        .param("size", "5"))
                .andExpect(status().isOk());

        ArgumentCaptor<FindFamilyGroupNaturalPersonsQuery> captor =
                ArgumentCaptor.forClass(FindFamilyGroupNaturalPersonsQuery.class);
        verify(findNaturalPersonsUseCase).handle(captor.capture());

        assertEquals(id, captor.getValue().familyGroupId());
        assertEquals(2, captor.getValue().pageable().getPageNumber());
        assertEquals(5, captor.getValue().pageable().getPageSize());
        assertEquals(Sort.by("name").ascending(), captor.getValue().pageable().getSort());
    }

    @Test
    @WithMockCustomUser
    @DisplayName("GET /family-group/{id}/natural-person - Deve usar page 0 e size 10 por padrão")
    void findNaturalPersons_ShouldUseDefaultPagination() throws Exception {
        UUID id = UUID.randomUUID();

        when(findNaturalPersonsUseCase.handle(any())).thenReturn(new PageImpl<>(List.of(), PAGEABLE, 0));

        mockMvc.perform(get("/family-group/{id}/natural-person", id))
                .andExpect(status().isOk());

        ArgumentCaptor<FindFamilyGroupNaturalPersonsQuery> captor =
                ArgumentCaptor.forClass(FindFamilyGroupNaturalPersonsQuery.class);
        verify(findNaturalPersonsUseCase).handle(captor.capture());

        assertEquals(0, captor.getValue().pageable().getPageNumber());
        assertEquals(10, captor.getValue().pageable().getPageSize());
    }

    @Test
    @WithMockCustomUser
    @DisplayName("GET /family-group/{id}/natural-person - Deve retornar 200 OK com lista vazia")
    void findNaturalPersons_ShouldReturnEmptyList() throws Exception {
        UUID id = UUID.randomUUID();

        when(findNaturalPersonsUseCase.handle(any())).thenReturn(new PageImpl<>(List.of(), PAGEABLE, 0));

        mockMvc.perform(get("/family-group/{id}/natural-person", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isEmpty())
                .andExpect(jsonPath("$.totalElements").value(0));
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
    @WithMockCustomUser
    @DisplayName("POST /family-group/{id}/natural-person - Deve criar a pessoa e vincular, retornando 201 com Location")
    void addNewNaturalPerson_ShouldReturn201WithLocation() throws Exception {
        UUID groupId = UUID.randomUUID();
        NaturalPerson person = NaturalPerson.create(
                "Maria Silva", "maria@email.com", null, "11144477735", null, null, null, null, null);

        CreateNaturalPersonCommand createCommand = new CreateNaturalPersonCommand(
                "Maria Silva", "maria@email.com", null, null, "11144477735", null, null, null, null);

        when(addNewNaturalPersonUseCase.handle(any())).thenReturn(person);

        mockMvc.perform(post("/family-group/{id}/natural-person", groupId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createCommand))
                        .with(csrf()))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "http://localhost/natural-person/" + person.getId()))
                .andExpect(jsonPath("$.name").value("Maria Silva"))
                .andExpect(jsonPath("$.cpf").value("11144477735"));

        ArgumentCaptor<AddNewNaturalPersonToFamilyGroupCommand> captor =
                ArgumentCaptor.forClass(AddNewNaturalPersonToFamilyGroupCommand.class);
        verify(addNewNaturalPersonUseCase).handle(captor.capture());

        assertEquals(groupId, captor.getValue().familyGroupId());
        assertEquals("Maria Silva", captor.getValue().naturalPerson().name());
    }

    @Test
    @WithMockCustomUser
    @DisplayName("POST /family-group/{id}/natural-person - Deve retornar 400 quando o corpo for invalido")
    void addNewNaturalPerson_ShouldReturn400WhenBodyInvalid() throws Exception {
        CreateNaturalPersonCommand invalid = new CreateNaturalPersonCommand(
                "", "email-invalido", null, null, "cpf-invalido", null, null, null, null);

        mockMvc.perform(post("/family-group/{id}/natural-person", UUID.randomUUID())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalid))
                        .with(csrf()))
                .andExpect(status().isBadRequest());

        verify(addNewNaturalPersonUseCase, never()).handle(any());
    }

    @Test
    @WithMockCustomUser
    @DisplayName("POST /family-group/{id}/natural-person/{naturalPersonId} - Deve vincular existente e retornar 204")
    void addNaturalPerson_ShouldReturn204() throws Exception {
        UUID groupId = UUID.randomUUID();
        UUID personId = UUID.randomUUID();

        doNothing().when(addNaturalPersonUseCase).handle(any());

        mockMvc.perform(post("/family-group/{id}/natural-person/{naturalPersonId}", groupId, personId)
                        .with(csrf()))
                .andExpect(status().isNoContent());

        ArgumentCaptor<AddNaturalPersonToFamilyGroupCommand> captor =
                ArgumentCaptor.forClass(AddNaturalPersonToFamilyGroupCommand.class);
        verify(addNaturalPersonUseCase).handle(captor.capture());

        assertEquals(groupId, captor.getValue().familyGroupId());
        assertEquals(personId, captor.getValue().naturalPersonId());
    }

    @Test
    @WithMockCustomUser
    @DisplayName("POST /family-group/{id}/natural-person/{naturalPersonId} - Deve retornar 409 quando ja estiver vinculado")
    void addNaturalPerson_ShouldReturn409WhenAlreadyLinked() throws Exception {
        doThrow(new IllegalStateException("Esta pessoa ja esta vinculada a este grupo familiar."))
                .when(addNaturalPersonUseCase).handle(any());

        mockMvc.perform(post("/family-group/{id}/natural-person/{naturalPersonId}",
                        UUID.randomUUID(), UUID.randomUUID())
                        .with(csrf()))
                .andExpect(status().isConflict());
    }

    @Test
    @WithMockCustomUser
    @DisplayName("DELETE /family-group/{id}/natural-person/{naturalPersonId} - Deve desvincular e retornar 204")
    void removeNaturalPerson_ShouldReturn204() throws Exception {
        UUID groupId = UUID.randomUUID();
        UUID personId = UUID.randomUUID();

        doNothing().when(removeNaturalPersonUseCase).handle(any());

        mockMvc.perform(delete("/family-group/{id}/natural-person/{naturalPersonId}", groupId, personId)
                        .with(csrf()))
                .andExpect(status().isNoContent());

        ArgumentCaptor<RemoveNaturalPersonFromFamilyGroupCommand> captor =
                ArgumentCaptor.forClass(RemoveNaturalPersonFromFamilyGroupCommand.class);
        verify(removeNaturalPersonUseCase).handle(captor.capture());

        assertEquals(groupId, captor.getValue().familyGroupId());
        assertEquals(personId, captor.getValue().naturalPersonId());
    }

    @Test
    @WithMockCustomUser
    @DisplayName("DELETE /family-group/{id}/natural-person/{naturalPersonId} - Deve retornar 409 quando nao estiver vinculado")
    void removeNaturalPerson_ShouldReturn409WhenNotLinked() throws Exception {
        doThrow(new IllegalStateException("Esta pessoa nao esta vinculada a este grupo familiar."))
                .when(removeNaturalPersonUseCase).handle(any());

        mockMvc.perform(delete("/family-group/{id}/natural-person/{naturalPersonId}",
                        UUID.randomUUID(), UUID.randomUUID())
                        .with(csrf()))
                .andExpect(status().isConflict());
    }

    @Test
    @DisplayName("GET /family-group/{id} - Deve retornar 401 Unauthorized sem autenticação")
    void findById_ShouldReturn401Unauthorized() throws Exception {
        mockMvc.perform(get("/family-group/{id}", UUID.randomUUID()))
                .andExpect(status().isUnauthorized());
    }
}
