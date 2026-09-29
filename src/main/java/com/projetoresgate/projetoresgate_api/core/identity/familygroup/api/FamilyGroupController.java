package com.projetoresgate.projetoresgate_api.core.identity.familygroup.api;

import com.projetoresgate.projetoresgate_api.core.identity.familygroup.api.dto.FamilyGroupResponse;
import com.projetoresgate.projetoresgate_api.core.identity.familygroup.api.dto.FamilyGroupSummaryResponse;
import com.projetoresgate.projetoresgate_api.core.identity.familygroup.domain.FamilyGroup;
import com.projetoresgate.projetoresgate_api.core.identity.familygroup.usecase.CreateFamilyGroupUseCase;
import com.projetoresgate.projetoresgate_api.core.identity.familygroup.usecase.FindFamilyGroupByIdUseCase;
import com.projetoresgate.projetoresgate_api.core.identity.familygroup.usecase.FindFamilyGroupNaturalPersonsUseCase;
import com.projetoresgate.projetoresgate_api.core.identity.familygroup.usecase.SearchFamilyGroupUseCase;
import com.projetoresgate.projetoresgate_api.core.identity.familygroup.usecase.SoftDeleteFamilyGroupUseCase;
import com.projetoresgate.projetoresgate_api.core.identity.familygroup.usecase.UpdateFamilyGroupUseCase;
import com.projetoresgate.projetoresgate_api.core.identity.familygroup.usecase.command.CreateFamilyGroupCommand;
import com.projetoresgate.projetoresgate_api.core.identity.familygroup.usecase.command.SoftDeleteFamilyGroupCommand;
import com.projetoresgate.projetoresgate_api.core.identity.familygroup.usecase.command.UpdateFamilyGroupCommand;
import com.projetoresgate.projetoresgate_api.core.identity.familygroup.usecase.query.FindFamilyGroupByIdQuery;
import com.projetoresgate.projetoresgate_api.core.identity.familygroup.usecase.query.FindFamilyGroupNaturalPersonsQuery;
import com.projetoresgate.projetoresgate_api.core.identity.familygroup.usecase.query.SearchFamilyGroupQuery;
import com.projetoresgate.projetoresgate_api.core.identity.naturalperson.api.dto.NaturalPersonResponse;
import com.projetoresgate.projetoresgate_api.core.identity.naturalperson.domain.NaturalPerson;
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

import java.math.BigDecimal;
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

    public FamilyGroupController(CreateFamilyGroupUseCase createUseCase,
                                 UpdateFamilyGroupUseCase updateUseCase,
                                 SoftDeleteFamilyGroupUseCase softDeleteUseCase,
                                 FindFamilyGroupByIdUseCase findByIdUseCase,
                                 FindFamilyGroupNaturalPersonsUseCase findNaturalPersonsUseCase,
                                 SearchFamilyGroupUseCase searchUseCase) {
        this.createUseCase = createUseCase;
        this.updateUseCase = updateUseCase;
        this.softDeleteUseCase = softDeleteUseCase;
        this.findByIdUseCase = findByIdUseCase;
        this.findNaturalPersonsUseCase = findNaturalPersonsUseCase;
        this.searchUseCase = searchUseCase;
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
    @Operation(summary = "Listar com Filtros",
            description = "Lista grupos familiares com paginação e filtros opcionais, retornando apenas os dados da tela de listagem (id, friendlyId, nome e total de pessoas cadastradas).")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista retornada com sucesso",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = Page.class)))
    })
    public ResponseEntity<Page<FamilyGroupSummaryResponse>> search(
            @Parameter(description = "Termo de pesquisa (Nome)") @RequestParam(required = false) String searchTerm,
            @Parameter(description = "Nome") @RequestParam(required = false) String name,
            @Parameter(description = "Renda familiar mínima") @RequestParam(required = false) BigDecimal minHouseholdIncome,
            @Parameter(description = "Renda familiar máxima") @RequestParam(required = false) BigDecimal maxHouseholdIncome,
            @Parameter(description = "Renda per capita mínima") @RequestParam(required = false) BigDecimal minPerCapitaIncome,
            @Parameter(description = "Renda per capita máxima") @RequestParam(required = false) BigDecimal maxPerCapitaIncome,
            @Parameter(description = "Número de moradores") @RequestParam(required = false) Integer numberOfResidents,
            @Parameter(description = "Número da página") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Tamanho da página") @RequestParam(defaultValue = "10") int size
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("name").ascending());
        SearchFamilyGroupQuery query = new SearchFamilyGroupQuery(searchTerm, name, minHouseholdIncome,
                maxHouseholdIncome, minPerCapitaIncome, maxPerCapitaIncome, numberOfResidents, pageable);
        return ResponseEntity.ok(searchUseCase.handle(query));
    }

    @GetMapping("/{id}/natural-person")
    @Operation(summary = "Listar Pessoas Físicas do Grupo Familiar",
            description = "Retorna as pessoas físicas vinculadas a um grupo familiar pelo seu ID.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista de pessoas físicas retornada com sucesso",
                    content = @Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = NaturalPersonResponse.class)))),
            @ApiResponse(responseCode = "404", description = "Grupo familiar não encontrado", content = @Content)
    })
    public ResponseEntity<List<NaturalPersonResponse>> findNaturalPersons(@PathVariable UUID id) {
        List<NaturalPerson> naturalPersons =
                findNaturalPersonsUseCase.handle(new FindFamilyGroupNaturalPersonsQuery(id));
        return ResponseEntity.ok(naturalPersons.stream().map(NaturalPersonResponse::fromEntity).toList());
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
