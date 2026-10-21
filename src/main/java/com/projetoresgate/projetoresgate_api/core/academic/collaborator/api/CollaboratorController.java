package com.projetoresgate.projetoresgate_api.core.academic.collaborator.api;

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
import com.projetoresgate.projetoresgate_api.core.academic.collaborator.usecase.query.AutocompleteCollaboratorQuery;
import com.projetoresgate.projetoresgate_api.core.academic.collaborator.usecase.query.FindAllCollaboratorsQuery;
import com.projetoresgate.projetoresgate_api.core.academic.collaborator.usecase.query.FindCollaboratorByIdQuery;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/collaborator")
@Tag(name = "Collaborator", description = "Endpoints para gerenciamento de Colaboradores")
public class CollaboratorController {

    private final CreateCollaboratorUseCase createUseCase;
    private final UpdateCollaboratorUseCase updateUseCase;
    private final SoftDeleteCollaboratorUseCase softDeleteUseCase;
    private final FindCollaboratorByIdUseCase findByIdUseCase;
    private final FindAllCollaboratorsUseCase findAllUseCase;
    private final AutocompleteCollaboratorUseCase autocompleteUseCase;

    public CollaboratorController(CreateCollaboratorUseCase createUseCase,
                                  UpdateCollaboratorUseCase updateUseCase,
                                  SoftDeleteCollaboratorUseCase softDeleteUseCase,
                                  FindCollaboratorByIdUseCase findByIdUseCase,
                                  FindAllCollaboratorsUseCase findAllUseCase,
                                  AutocompleteCollaboratorUseCase autocompleteUseCase) {
        this.createUseCase = createUseCase;
        this.updateUseCase = updateUseCase;
        this.softDeleteUseCase = softDeleteUseCase;
        this.findByIdUseCase = findByIdUseCase;
        this.findAllUseCase = findAllUseCase;
        this.autocompleteUseCase = autocompleteUseCase;
    }

    @PostMapping
    @Operation(summary = "Criar Colaborador",
            description = "Cadastra um colaborador a partir de uma pessoa física obrigatória e, opcionalmente, de uma "
                    + "pessoa jurídica vinculada. A pessoa física não pode ter dois cadastros ativos de colaborador.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Colaborador criado com sucesso",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = CollaboratorResponse.class))),
            @ApiResponse(responseCode = "400", description = "Dados inválidos", content = @Content),
            @ApiResponse(responseCode = "404", description = "Pessoa física ou jurídica não encontrada", content = @Content),
            @ApiResponse(responseCode = "409", description = "A pessoa física já possui um cadastro de colaborador", content = @Content)
    })
    public ResponseEntity<CollaboratorResponse> create(@RequestBody @Valid CreateCollaboratorCommand command) {
        CollaboratorResponse response = createUseCase.handle(command);

        URI location = ServletUriComponentsBuilder.fromCurrentContextPath()
                .path("/collaborator/{id}")
                .buildAndExpand(response.id())
                .toUri();

        return ResponseEntity.status(HttpStatus.CREATED).location(location).body(response);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualizar Colaborador",
            description = "Atualiza a pessoa física, a pessoa jurídica opcional e a indicação de voluntário do colaborador.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Colaborador atualizado com sucesso",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = CollaboratorResponse.class))),
            @ApiResponse(responseCode = "400", description = "Dados inválidos", content = @Content),
            @ApiResponse(responseCode = "404", description = "Colaborador, pessoa física ou jurídica não encontrada", content = @Content),
            @ApiResponse(responseCode = "409", description = "A pessoa física informada já possui um cadastro de colaborador", content = @Content)
    })
    public ResponseEntity<CollaboratorResponse> update(@PathVariable UUID id,
                                                       @RequestBody @Valid UpdateCollaboratorCommand command) {
        return ResponseEntity.ok(updateUseCase.handle(command.withId(id)));
    }

    @GetMapping
    @Operation(summary = "Listar Colaboradores",
            description = "Lista os colaboradores com paginação, ordenados pelo nome da pessoa física. O termo de pesquisa "
                    + "filtra por dados da pessoa física (nome, apelido, CPF ou RG) e da pessoa jurídica (razão social, nome "
                    + "fantasia ou CNPJ).")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista retornada com sucesso",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = Page.class)))
    })
    public ResponseEntity<Page<CollaboratorSummaryResponse>> findAll(
            @Parameter(description = "Termo de pesquisa (pessoa física ou jurídica)") @RequestParam(required = false) String searchTerm,
            @Parameter(description = "Número da página") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Tamanho da página") @RequestParam(defaultValue = "10") int size
    ) {
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(findAllUseCase.handle(new FindAllCollaboratorsQuery(searchTerm, pageable)));
    }

    @GetMapping("/autocomplete")
    @Operation(summary = "Autocomplete de Colaboradores",
            description = "Busca leve de colaboradores para componentes de seleção do front, como a vinculação de educador "
                    + "à turma. O termo de pesquisa cruza os dados da pessoa física e da pessoa jurídica vinculadas.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista retornada com sucesso",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = CollaboratorAutocompleteResponse.class)))
    })
    public ResponseEntity<List<CollaboratorAutocompleteResponse>> autocomplete(
            @Parameter(description = "Termo de pesquisa (pessoa física ou jurídica)") @RequestParam(required = false) String searchTerm,
            @Parameter(description = "Quantidade máxima de resultados") @RequestParam(defaultValue = "10") int limit
    ) {
        return ResponseEntity.ok(autocompleteUseCase.handle(new AutocompleteCollaboratorQuery(searchTerm, limit)));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar Colaborador por ID",
            description = "Retorna os dados do colaborador, com o nome e o CPF da pessoa física e o nome da pessoa jurídica "
                    + "quando houver, para carga do modal de edição.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Colaborador encontrado",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = CollaboratorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Colaborador não encontrado", content = @Content)
    })
    public ResponseEntity<CollaboratorResponse> findById(@PathVariable UUID id) {
        return ResponseEntity.ok(findByIdUseCase.handle(new FindCollaboratorByIdQuery(id)));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Excluir Colaborador",
            description = "Realiza a exclusão lógica (soft delete) do colaborador. O registro não é removido fisicamente do banco.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Colaborador excluído com sucesso", content = @Content),
            @ApiResponse(responseCode = "404", description = "Colaborador não encontrado", content = @Content)
    })
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        softDeleteUseCase.handle(new SoftDeleteCollaboratorCommand(id));
        return ResponseEntity.noContent().build();
    }
}
