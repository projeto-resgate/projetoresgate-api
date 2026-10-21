package com.projetoresgate.projetoresgate_api.core.academic.classgroup.api;

import com.projetoresgate.projetoresgate_api.core.academic.classgroup.api.dto.ClassLearnerResponse;
import com.projetoresgate.projetoresgate_api.core.academic.classgroup.usecase.CreateClassLearnerUseCase;
import com.projetoresgate.projetoresgate_api.core.academic.classgroup.usecase.FindAllClassLearnersUseCase;
import com.projetoresgate.projetoresgate_api.core.academic.classgroup.usecase.SoftDeleteClassLearnerUseCase;
import com.projetoresgate.projetoresgate_api.core.academic.classgroup.usecase.command.CreateClassLearnerCommand;
import com.projetoresgate.projetoresgate_api.core.academic.classgroup.usecase.command.SoftDeleteClassLearnerCommand;
import com.projetoresgate.projetoresgate_api.core.academic.classgroup.usecase.query.FindAllClassLearnersQuery;
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
@RequestMapping("/class-group/{classGroupId}/learner")
@Tag(name = "Class Learner", description = "Endpoints para gerenciamento dos Educandos vinculados à Turma")
public class ClassLearnerController {

    private final CreateClassLearnerUseCase createUseCase;
    private final SoftDeleteClassLearnerUseCase softDeleteUseCase;
    private final FindAllClassLearnersUseCase findAllUseCase;

    public ClassLearnerController(CreateClassLearnerUseCase createUseCase,
                                  SoftDeleteClassLearnerUseCase softDeleteUseCase,
                                  FindAllClassLearnersUseCase findAllUseCase) {
        this.createUseCase = createUseCase;
        this.softDeleteUseCase = softDeleteUseCase;
        this.findAllUseCase = findAllUseCase;
    }

    @PostMapping
    @Operation(summary = "Vincular Educando", description = "Vincula um educando já cadastrado à turma.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Educando vinculado com sucesso",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ClassLearnerResponse.class))),
            @ApiResponse(responseCode = "400", description = "Dados inválidos", content = @Content),
            @ApiResponse(responseCode = "404", description = "Turma ou educando não encontrado", content = @Content),
            @ApiResponse(responseCode = "409", description = "Educando já vinculado à turma", content = @Content)
    })
    public ResponseEntity<ClassLearnerResponse> create(@PathVariable UUID classGroupId,
                                                       @RequestBody @Valid CreateClassLearnerCommand command) {
        ClassLearnerResponse response = createUseCase.handle(command.withClassGroupId(classGroupId));
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    @Operation(summary = "Listar Educandos",
            description = "Lista os educandos vinculados à turma com paginação e filtros. Ordena primeiro os vínculos "
                    + "ativos, depois alfabeticamente pelo nome do educando.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista retornada com sucesso",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = Page.class))),
            @ApiResponse(responseCode = "404", description = "Turma não encontrada", content = @Content)
    })
    public ResponseEntity<Page<ClassLearnerResponse>> findAll(
            @PathVariable UUID classGroupId,
            @Parameter(description = "Termo de pesquisa (Nome ou apelido do educando)") @RequestParam(required = false) String searchTerm,
            @Parameter(description = "Situação do vínculo") @RequestParam(required = false) Boolean active,
            @Parameter(description = "Número da página") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Tamanho da página") @RequestParam(defaultValue = "10") int size
    ) {
        Pageable pageable = PageRequest.of(page, size);
        FindAllClassLearnersQuery query = new FindAllClassLearnersQuery(classGroupId, searchTerm, active, pageable);
        return ResponseEntity.ok(findAllUseCase.handle(query));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Desvincular Educando",
            description = "Realiza a exclusão lógica (soft delete) do vínculo do educando com a turma. O vínculo também "
                    + "é inativado automaticamente quando o educando sofre evasão pelo endpoint de desistência.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Educando desvinculado com sucesso", content = @Content),
            @ApiResponse(responseCode = "404", description = "Vínculo ou turma não encontrado", content = @Content)
    })
    public ResponseEntity<Void> delete(@PathVariable UUID classGroupId, @PathVariable UUID id) {
        softDeleteUseCase.handle(new SoftDeleteClassLearnerCommand(classGroupId, id));
        return ResponseEntity.noContent().build();
    }
}
