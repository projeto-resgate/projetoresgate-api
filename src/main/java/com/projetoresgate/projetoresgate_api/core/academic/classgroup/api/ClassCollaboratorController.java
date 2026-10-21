package com.projetoresgate.projetoresgate_api.core.academic.classgroup.api;

import com.projetoresgate.projetoresgate_api.core.academic.classgroup.api.dto.ClassCollaboratorResponse;
import com.projetoresgate.projetoresgate_api.core.academic.classgroup.usecase.CreateClassCollaboratorUseCase;
import com.projetoresgate.projetoresgate_api.core.academic.classgroup.usecase.FindAllClassCollaboratorsUseCase;
import com.projetoresgate.projetoresgate_api.core.academic.classgroup.usecase.SoftDeleteClassCollaboratorUseCase;
import com.projetoresgate.projetoresgate_api.core.academic.classgroup.usecase.UpdateClassCollaboratorUseCase;
import com.projetoresgate.projetoresgate_api.core.academic.classgroup.usecase.command.CreateClassCollaboratorCommand;
import com.projetoresgate.projetoresgate_api.core.academic.classgroup.usecase.command.SoftDeleteClassCollaboratorCommand;
import com.projetoresgate.projetoresgate_api.core.academic.classgroup.usecase.command.UpdateClassCollaboratorCommand;
import com.projetoresgate.projetoresgate_api.core.academic.classgroup.usecase.query.FindAllClassCollaboratorsQuery;
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

import java.util.UUID;

@RestController
@RequestMapping("/class-group/{classGroupId}/collaborator")
@Tag(name = "Class Collaborator", description = "Endpoints para gerenciamento dos Educadores vinculados à Turma")
public class ClassCollaboratorController {

    private final CreateClassCollaboratorUseCase createUseCase;
    private final UpdateClassCollaboratorUseCase updateUseCase;
    private final SoftDeleteClassCollaboratorUseCase softDeleteUseCase;
    private final FindAllClassCollaboratorsUseCase findAllUseCase;

    public ClassCollaboratorController(CreateClassCollaboratorUseCase createUseCase,
                                       UpdateClassCollaboratorUseCase updateUseCase,
                                       SoftDeleteClassCollaboratorUseCase softDeleteUseCase,
                                       FindAllClassCollaboratorsUseCase findAllUseCase) {
        this.createUseCase = createUseCase;
        this.updateUseCase = updateUseCase;
        this.softDeleteUseCase = softDeleteUseCase;
        this.findAllUseCase = findAllUseCase;
    }

    @PostMapping
    @Operation(summary = "Vincular Educador",
            description = "Vincula uma pessoa física à turma com um tipo de educador do programa da turma.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Educador vinculado com sucesso",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ClassCollaboratorResponse.class))),
            @ApiResponse(responseCode = "400", description = "Dados inválidos", content = @Content),
            @ApiResponse(responseCode = "404", description = "Turma, pessoa física ou categoria de educador não encontrada", content = @Content),
            @ApiResponse(responseCode = "409", description = "Educador já vinculado à turma", content = @Content)
    })
    public ResponseEntity<ClassCollaboratorResponse> create(@PathVariable UUID classGroupId,
                                                            @RequestBody @Valid CreateClassCollaboratorCommand command) {
        ClassCollaboratorResponse response = createUseCase.handle(command.withClassGroupId(classGroupId));
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualizar vínculo de Educador",
            description = "Altera o tipo de educador e/ou a situação do vínculo.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Vínculo atualizado com sucesso",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ClassCollaboratorResponse.class))),
            @ApiResponse(responseCode = "400", description = "Dados inválidos", content = @Content),
            @ApiResponse(responseCode = "404", description = "Vínculo, turma ou categoria não encontrada", content = @Content)
    })
    public ResponseEntity<ClassCollaboratorResponse> update(@PathVariable UUID classGroupId,
                                                            @PathVariable UUID id,
                                                            @RequestBody @Valid UpdateClassCollaboratorCommand command) {
        return ResponseEntity.ok(updateUseCase.handle(command.withRoute(classGroupId, id)));
    }

    @GetMapping
    @Operation(summary = "Listar Educadores",
            description = "Lista os educadores vinculados à turma com paginação e filtros. Ordena primeiro os vínculos "
                    + "ativos, depois alfabeticamente pelo nome.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista retornada com sucesso",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = Page.class))),
            @ApiResponse(responseCode = "404", description = "Turma não encontrada", content = @Content)
    })
    public ResponseEntity<Page<ClassCollaboratorResponse>> findAll(
            @PathVariable UUID classGroupId,
            @Parameter(description = "Termo de pesquisa (Nome ou apelido do educador)") @RequestParam(required = false) String searchTerm,
            @Parameter(description = "Id da categoria de educador") @RequestParam(required = false) UUID educatorCategoryItemId,
            @Parameter(description = "Situação do vínculo") @RequestParam(required = false) Boolean active,
            @Parameter(description = "Número da página") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Tamanho da página") @RequestParam(defaultValue = "10") int size
    ) {
        Pageable pageable = PageRequest.of(page, size);
        FindAllClassCollaboratorsQuery query = new FindAllClassCollaboratorsQuery(
                classGroupId, educatorCategoryItemId, searchTerm, active, pageable);
        return ResponseEntity.ok(findAllUseCase.handle(query));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Desvincular Educador",
            description = "Realiza a exclusão lógica (soft delete) do vínculo do educador com a turma.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Educador desvinculado com sucesso", content = @Content),
            @ApiResponse(responseCode = "404", description = "Vínculo ou turma não encontrado", content = @Content)
    })
    public ResponseEntity<Void> delete(@PathVariable UUID classGroupId, @PathVariable UUID id) {
        softDeleteUseCase.handle(new SoftDeleteClassCollaboratorCommand(classGroupId, id));
        return ResponseEntity.noContent().build();
    }
}
