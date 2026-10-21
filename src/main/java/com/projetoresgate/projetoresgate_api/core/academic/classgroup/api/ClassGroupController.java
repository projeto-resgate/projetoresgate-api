package com.projetoresgate.projetoresgate_api.core.academic.classgroup.api;

import com.projetoresgate.projetoresgate_api.core.academic.classgroup.api.dto.ClassGroupResponse;
import com.projetoresgate.projetoresgate_api.core.academic.classgroup.api.dto.ClassGroupSummaryResponse;
import com.projetoresgate.projetoresgate_api.core.academic.classgroup.usecase.*;
import com.projetoresgate.projetoresgate_api.core.academic.classgroup.usecase.command.CreateClassGroupCommand;
import com.projetoresgate.projetoresgate_api.core.academic.classgroup.usecase.command.SoftDeleteClassGroupCommand;
import com.projetoresgate.projetoresgate_api.core.academic.classgroup.usecase.command.UpdateClassGroupCommand;
import com.projetoresgate.projetoresgate_api.core.academic.classgroup.usecase.query.FindAllClassGroupsQuery;
import com.projetoresgate.projetoresgate_api.core.academic.classgroup.usecase.query.FindClassGroupByIdQuery;
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
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/class-group")
@Tag(name = "Class Group", description = "Endpoints para gerenciamento de Turmas")
public class ClassGroupController {

    private final CreateClassGroupUseCase createUseCase;
    private final UpdateClassGroupUseCase updateUseCase;
    private final SoftDeleteClassGroupUseCase softDeleteUseCase;
    private final FindClassGroupByIdUseCase findByIdUseCase;
    private final FindAllClassGroupsUseCase findAllUseCase;

    public ClassGroupController(CreateClassGroupUseCase createUseCase,
                                UpdateClassGroupUseCase updateUseCase,
                                SoftDeleteClassGroupUseCase softDeleteUseCase,
                                FindClassGroupByIdUseCase findByIdUseCase,
                                FindAllClassGroupsUseCase findAllUseCase) {
        this.createUseCase = createUseCase;
        this.updateUseCase = updateUseCase;
        this.softDeleteUseCase = softDeleteUseCase;
        this.findByIdUseCase = findByIdUseCase;
        this.findAllUseCase = findAllUseCase;
    }

    @PostMapping
    @Operation(summary = "Criar Turma",
            description = "Cadastra uma nova turma. O título é gerado automaticamente pelo backend, concatenando "
                    + "o nome do programa, as séries sugeridas e os dias da semana das aulas já cadastradas.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Turma criada com sucesso",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ClassGroupResponse.class))),
            @ApiResponse(responseCode = "400", description = "Dados inválidos", content = @Content),
            @ApiResponse(responseCode = "404", description = "Programa, instituição ou série escolar não encontrada", content = @Content)
    })
    public ResponseEntity<ClassGroupResponse> create(@RequestBody @Valid CreateClassGroupCommand command) {
        return ResponseEntity.status(HttpStatus.CREATED).body(createUseCase.handle(command));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualizar Turma",
            description = "Atualiza os dados da turma e recalcula o título automaticamente.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Turma atualizada com sucesso",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ClassGroupResponse.class))),
            @ApiResponse(responseCode = "400", description = "Dados inválidos", content = @Content),
            @ApiResponse(responseCode = "404", description = "Turma, programa, instituição ou série escolar não encontrada", content = @Content)
    })
    public ResponseEntity<ClassGroupResponse> update(@PathVariable UUID id,
                                                     @RequestBody @Valid UpdateClassGroupCommand command) {
        return ResponseEntity.ok(updateUseCase.handle(command.withId(id)));
    }

    @GetMapping
    @Operation(summary = "Listar Turmas",
            description = "Lista turmas com paginação, ordenadas pelo título, e filtros opcionais por termo de pesquisa, "
                    + "programa, instituição e situação (ativa/inativa).")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista retornada com sucesso",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = Page.class)))
    })
    public ResponseEntity<Page<ClassGroupSummaryResponse>> findAll(
            @Parameter(description = "Termo de pesquisa (título)") @RequestParam(required = false) String searchTerm,
            @Parameter(description = "Id do programa") @RequestParam(required = false) UUID programId,
            @Parameter(description = "Id da instituição") @RequestParam(required = false) UUID institutionId,
            @Parameter(description = "Situação da turma") @RequestParam(required = false) Boolean active,
            @Parameter(description = "Número da página") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Tamanho da página") @RequestParam(defaultValue = "10") int size
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("title").ascending());
        FindAllClassGroupsQuery query = new FindAllClassGroupsQuery(
                searchTerm, programId, institutionId, active, pageable);
        return ResponseEntity.ok(findAllUseCase.handle(query));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar Turma por ID",
            description = "Retorna os dados completos da turma, com os nomes do programa, da instituição e das séries sugeridas.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Turma encontrada",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ClassGroupResponse.class))),
            @ApiResponse(responseCode = "404", description = "Turma não encontrada", content = @Content)
    })
    public ResponseEntity<ClassGroupResponse> findById(@PathVariable UUID id) {
        return ResponseEntity.ok(findByIdUseCase.handle(new FindClassGroupByIdQuery(id)));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Excluir Turma",
            description = "Realiza a exclusão lógica (soft delete) da turma e de todos os seus vínculos (horários, "
                    + "educadores e educandos). Os registros não são removidos fisicamente do banco.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Turma excluída com sucesso", content = @Content),
            @ApiResponse(responseCode = "404", description = "Turma não encontrada", content = @Content)
    })
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        softDeleteUseCase.handle(new SoftDeleteClassGroupCommand(id));
        return ResponseEntity.noContent().build();
    }
}
