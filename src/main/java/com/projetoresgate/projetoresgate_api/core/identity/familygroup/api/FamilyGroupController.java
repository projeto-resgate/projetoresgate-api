package com.projetoresgate.projetoresgate_api.core.identity.familygroup.api;

import com.projetoresgate.projetoresgate_api.core.identity.familygroup.api.dto.FamilyGroupNaturalPersonResponse;
import com.projetoresgate.projetoresgate_api.core.identity.familygroup.api.dto.FamilyGroupResponse;
import com.projetoresgate.projetoresgate_api.core.identity.familygroup.api.dto.FamilyGroupSummaryResponse;
import com.projetoresgate.projetoresgate_api.core.identity.familygroup.domain.FamilyGroup;
import com.projetoresgate.projetoresgate_api.core.identity.familygroup.usecase.*;
import com.projetoresgate.projetoresgate_api.core.identity.familygroup.usecase.command.*;
import com.projetoresgate.projetoresgate_api.core.identity.familygroup.usecase.query.FindFamilyGroupByIdQuery;
import com.projetoresgate.projetoresgate_api.core.identity.familygroup.usecase.query.FindFamilyGroupNaturalPersonsQuery;
import com.projetoresgate.projetoresgate_api.core.identity.familygroup.usecase.query.SearchFamilyGroupQuery;
import com.projetoresgate.projetoresgate_api.core.identity.naturalperson.api.dto.NaturalPersonResponse;
import com.projetoresgate.projetoresgate_api.core.identity.naturalperson.domain.NaturalPerson;
import com.projetoresgate.projetoresgate_api.core.identity.naturalperson.usecase.command.CreateNaturalPersonCommand;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/family-group")
@Tag(name = "Family Group", description = "Endpoints para gerenciamento de Grupos Familiares")
public class FamilyGroupController {

    private final CreateFamilyGroupUseCase createUseCase;
    private final UpdateFamilyGroupUseCase updateUseCase;
    private final SoftDeleteFamilyGroupUseCase softDeleteUseCase;
    private final FindFamilyGroupByIdUseCase findByIdUseCase;
    private final FindFamilyGroupNaturalPersonsUseCase findNaturalPersonsUseCase;
    private final SearchFamilyGroupUseCase searchUseCase;
    private final AddNaturalPersonToFamilyGroupUseCase addNaturalPersonUseCase;
    private final RemoveNaturalPersonFromFamilyGroupUseCase removeNaturalPersonUseCase;
    private final AddNewNaturalPersonToFamilyGroupUseCase addNewNaturalPersonUseCase;

    public FamilyGroupController(CreateFamilyGroupUseCase createUseCase,
                                 UpdateFamilyGroupUseCase updateUseCase,
                                 SoftDeleteFamilyGroupUseCase softDeleteUseCase,
                                 FindFamilyGroupByIdUseCase findByIdUseCase,
                                 FindFamilyGroupNaturalPersonsUseCase findNaturalPersonsUseCase,
                                 SearchFamilyGroupUseCase searchUseCase,
                                 AddNaturalPersonToFamilyGroupUseCase addNaturalPersonUseCase,
                                 RemoveNaturalPersonFromFamilyGroupUseCase removeNaturalPersonUseCase,
                                 AddNewNaturalPersonToFamilyGroupUseCase addNewNaturalPersonUseCase) {
        this.createUseCase = createUseCase;
        this.updateUseCase = updateUseCase;
        this.softDeleteUseCase = softDeleteUseCase;
        this.findByIdUseCase = findByIdUseCase;
        this.findNaturalPersonsUseCase = findNaturalPersonsUseCase;
        this.searchUseCase = searchUseCase;
        this.addNaturalPersonUseCase = addNaturalPersonUseCase;
        this.removeNaturalPersonUseCase = removeNaturalPersonUseCase;
        this.addNewNaturalPersonUseCase = addNewNaturalPersonUseCase;
    }

    @PostMapping
    @Operation(summary = "Criar Grupo Familiar",
            description = "Cadastra um novo grupo familiar. O endereço é recebido no mesmo command e salvo na tabela de endereços. Não recebe a lista de pessoas físicas vinculadas.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Grupo familiar criado com sucesso",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = FamilyGroupResponse.class))),
            @ApiResponse(responseCode = "400", description = "Dados inválidos ou nome já cadastrado", content = @Content)
    })
    public ResponseEntity<FamilyGroupResponse> create(@RequestBody @Valid CreateFamilyGroupCommand command) {
        FamilyGroup familyGroup = createUseCase.handle(command);
        return ResponseEntity.status(HttpStatus.CREATED).body(FamilyGroupResponse.fromEntity(familyGroup));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualizar Grupo Familiar",
            description = "Atualiza os dados de um grupo familiar existente. O endereço é recebido no mesmo command e salvo na tabela de endereços. Não recebe a lista de pessoas físicas vinculadas.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Grupo familiar atualizado com sucesso",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = FamilyGroupResponse.class))),
            @ApiResponse(responseCode = "400", description = "Dados inválidos ou nome já cadastrado para outro grupo", content = @Content),
            @ApiResponse(responseCode = "404", description = "Grupo familiar não encontrado", content = @Content)
    })
    public ResponseEntity<FamilyGroupResponse> update(@PathVariable UUID id,
                                                      @RequestBody @Valid UpdateFamilyGroupCommand command) {
        FamilyGroup familyGroup = updateUseCase.handle(command.withId(id));
        return ResponseEntity.ok(FamilyGroupResponse.fromEntity(familyGroup));
    }

    @GetMapping
    @Operation(summary = "Listar Grupos Familiares",
            description = "Lista grupos familiares com paginação e filtro opcional por nome, retornando apenas os dados da tela de listagem (id, friendlyId, nome e total de pessoas cadastradas).")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista retornada com sucesso",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = Page.class)))
    })
    public ResponseEntity<Page<FamilyGroupSummaryResponse>> search(
            @Parameter(description = "Nome") @RequestParam(required = false) String name,
            @Parameter(description = "Número da página") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Tamanho da página") @RequestParam(defaultValue = "10") int size
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("name").ascending());
        SearchFamilyGroupQuery query = new SearchFamilyGroupQuery(name, pageable);
        return ResponseEntity.ok(searchUseCase.handle(query));
    }

    @GetMapping("/{id}/natural-person")
    @Operation(summary = "Listar Pessoas Físicas do Grupo Familiar",
            description = "Lista as pessoas físicas vinculadas a um grupo familiar com paginação, retornando apenas os dados da grid (id, nome, rg, cpf e celular). O id é usado para desvincular a pessoa do grupo.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista retornada com sucesso",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = Page.class))),
            @ApiResponse(responseCode = "404", description = "Grupo familiar não encontrado", content = @Content)
    })
    public ResponseEntity<Page<FamilyGroupNaturalPersonResponse>> findNaturalPersons(
            @PathVariable UUID id,
            @Parameter(description = "Número da página") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Tamanho da página") @RequestParam(defaultValue = "10") int size
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("name").ascending());
        FindFamilyGroupNaturalPersonsQuery query = new FindFamilyGroupNaturalPersonsQuery(id, pageable);
        return ResponseEntity.ok(findNaturalPersonsUseCase.handle(query));
    }

    @PostMapping("/{id}/natural-person")
    @Operation(summary = "Cadastrar Pessoa Física no Grupo Familiar",
            description = "Cria uma nova pessoa física e já vincula ao grupo familiar na mesma transação. O corpo é idêntico ao de POST /natural-person.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Pessoa física criada e vinculada com sucesso",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = NaturalPersonResponse.class))),
            @ApiResponse(responseCode = "400", description = "Dados inválidos ou CPF já cadastrado", content = @Content),
            @ApiResponse(responseCode = "404", description = "Grupo familiar não encontrado", content = @Content)
    })
    public ResponseEntity<NaturalPersonResponse> addNewNaturalPerson(
            @PathVariable UUID id,
            @RequestBody @Valid CreateNaturalPersonCommand command
    ) {
        NaturalPerson person = addNewNaturalPersonUseCase.handle(
                new AddNewNaturalPersonToFamilyGroupCommand(id, command));

        URI location = ServletUriComponentsBuilder.fromCurrentContextPath()
                .path("/natural-person/{id}")
                .buildAndExpand(person.getId())
                .toUri();

        return ResponseEntity.created(location).body(NaturalPersonResponse.fromEntity(person));
    }

    @PostMapping("/{id}/natural-person/{naturalPersonId}")
    @Operation(summary = "Vincular Pessoa Física Existente ao Grupo Familiar",
            description = "Vincula ao grupo familiar uma pessoa física já cadastrada. Não cria pessoa nova e não altera o numberOfResidents do grupo. Retorna 409 se a pessoa já estiver vinculada.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Pessoa física vinculada com sucesso", content = @Content),
            @ApiResponse(responseCode = "404", description = "Grupo familiar ou pessoa física não encontrada", content = @Content),
            @ApiResponse(responseCode = "409", description = "Pessoa física já vinculada a este grupo familiar", content = @Content)
    })
    public ResponseEntity<Void> addNaturalPerson(@PathVariable UUID id, @PathVariable UUID naturalPersonId) {
        addNaturalPersonUseCase.handle(new AddNaturalPersonToFamilyGroupCommand(null, naturalPersonId)
                .withFamilyGroupId(id));
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}/natural-person/{naturalPersonId}")
    @Operation(summary = "Desvincular Pessoa Física do Grupo Familiar",
            description = "Remove o vínculo entre a pessoa física e o grupo familiar. A pessoa continua cadastrada. Não altera o numberOfResidents do grupo.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Vínculo removido com sucesso", content = @Content),
            @ApiResponse(responseCode = "404", description = "Grupo familiar não encontrado", content = @Content),
            @ApiResponse(responseCode = "409", description = "Pessoa física não está vinculada a este grupo familiar", content = @Content)
    })
    public ResponseEntity<Void> removeNaturalPerson(@PathVariable UUID id, @PathVariable UUID naturalPersonId) {
        removeNaturalPersonUseCase.handle(new RemoveNaturalPersonFromFamilyGroupCommand(null, naturalPersonId)
                .withFamilyGroupId(id));
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar por ID",
            description = "Retorna os dados de um grupo familiar pelo seu ID. Não retorna a lista de pessoas físicas vinculadas.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Grupo familiar encontrado",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = FamilyGroupResponse.class))),
            @ApiResponse(responseCode = "404", description = "Grupo familiar não encontrado", content = @Content)
    })
    public ResponseEntity<FamilyGroupResponse> findById(@PathVariable UUID id) {
        FindFamilyGroupByIdQuery query = new FindFamilyGroupByIdQuery(id);
        FamilyGroup familyGroup = findByIdUseCase.handle(query);
        return ResponseEntity.ok(FamilyGroupResponse.fromEntity(familyGroup));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Excluir Grupo Familiar",
            description = "Realiza a exclusão lógica (soft delete) do grupo familiar. O registro não é removido fisicamente do banco.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Grupo familiar excluído com sucesso", content = @Content),
            @ApiResponse(responseCode = "404", description = "Grupo familiar não encontrado", content = @Content)
    })
    public ResponseEntity<Void> softDelete(@PathVariable UUID id) {
        softDeleteUseCase.handle(new SoftDeleteFamilyGroupCommand(id));
        return ResponseEntity.noContent().build();
    }
}
