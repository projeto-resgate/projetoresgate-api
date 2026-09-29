package com.projetoresgate.projetoresgate_api.core.academic.schoolgrade.api;

import com.projetoresgate.projetoresgate_api.core.academic.schoolgrade.api.dto.SchoolGradeNameResponse;
import com.projetoresgate.projetoresgate_api.core.academic.schoolgrade.api.dto.SchoolGradeResponse;
import com.projetoresgate.projetoresgate_api.core.academic.schoolgrade.domain.SchoolGrade;
import com.projetoresgate.projetoresgate_api.core.academic.schoolgrade.usecase.*;
import com.projetoresgate.projetoresgate_api.core.academic.schoolgrade.usecase.command.CreateSchoolGradeCommand;
import com.projetoresgate.projetoresgate_api.core.academic.schoolgrade.usecase.command.SoftDeleteSchoolGradeCommand;
import com.projetoresgate.projetoresgate_api.core.academic.schoolgrade.usecase.command.UpdateSchoolGradeCommand;
import com.projetoresgate.projetoresgate_api.core.academic.schoolgrade.usecase.query.FindAllSchoolGradesQuery;
import com.projetoresgate.projetoresgate_api.core.academic.schoolgrade.usecase.query.FindSchoolGradeByIdQuery;
import com.projetoresgate.projetoresgate_api.core.academic.schoolgrade.usecase.query.FindSchoolGradeNamesQuery;
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

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/school-grade")
@Tag(name = "School Grade", description = "Endpoints para gerenciamento de Séries Escolares")
public class SchoolGradeController {

    private final CreateSchoolGradeUseCase createUseCase;
    private final UpdateSchoolGradeUseCase updateUseCase;
    private final SoftDeleteSchoolGradeUseCase softDeleteUseCase;
    private final FindSchoolGradeByIdUseCase findByIdUseCase;
    private final FindAllSchoolGradesUseCase findAllUseCase;
    private final FindSchoolGradeNamesUseCase findNamesUseCase;

    public SchoolGradeController(CreateSchoolGradeUseCase createUseCase,
                                 UpdateSchoolGradeUseCase updateUseCase,
                                 SoftDeleteSchoolGradeUseCase softDeleteUseCase,
                                 FindSchoolGradeByIdUseCase findByIdUseCase,
                                 FindAllSchoolGradesUseCase findAllUseCase,
                                 FindSchoolGradeNamesUseCase findNamesUseCase) {
        this.createUseCase = createUseCase;
        this.updateUseCase = updateUseCase;
        this.softDeleteUseCase = softDeleteUseCase;
        this.findByIdUseCase = findByIdUseCase;
        this.findAllUseCase = findAllUseCase;
        this.findNamesUseCase = findNamesUseCase;
    }

    @PostMapping
    @Operation(summary = "Criar Série Escolar",
            description = "Cadastra uma nova série escolar. O nome deve ser único entre as séries não excluídas.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Série escolar criada com sucesso",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = SchoolGradeResponse.class))),
            @ApiResponse(responseCode = "400", description = "Dados inválidos ou nome já cadastrado", content = @Content)
    })
    public ResponseEntity<SchoolGradeResponse> create(@RequestBody @Valid CreateSchoolGradeCommand command) {
        SchoolGrade schoolGrade = createUseCase.handle(command);
        return ResponseEntity.status(HttpStatus.CREATED).body(SchoolGradeResponse.fromEntity(schoolGrade));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualizar Série Escolar",
            description = "Atualiza os dados de uma série escolar existente. A atualização é completa: os campos omitidos são apagados.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Série escolar atualizada com sucesso",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = SchoolGradeResponse.class))),
            @ApiResponse(responseCode = "400", description = "Dados inválidos ou nome já cadastrado para outra série", content = @Content),
            @ApiResponse(responseCode = "404", description = "Série escolar não encontrada", content = @Content)
    })
    public ResponseEntity<SchoolGradeResponse> update(@PathVariable UUID id,
                                                      @RequestBody @Valid UpdateSchoolGradeCommand command) {
        SchoolGrade schoolGrade = updateUseCase.handle(command.withId(id));
        return ResponseEntity.ok(SchoolGradeResponse.fromEntity(schoolGrade));
    }

    @GetMapping
    @Operation(summary = "Listar Séries Escolares",
            description = "Lista séries escolares com paginação, ordenadas pela ordem da trilha escolar, e filtros opcionais por nome e por ordem.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista retornada com sucesso",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = Page.class)))
    })
    public ResponseEntity<Page<SchoolGradeResponse>> findAll(
            @Parameter(description = "Nome") @RequestParam(required = false) String name,
            @Parameter(description = "Ordem da série") @RequestParam(required = false) Integer gradeOrder,
            @Parameter(description = "Número da página") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Tamanho da página") @RequestParam(defaultValue = "10") int size
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("gradeOrder").ascending());
        FindAllSchoolGradesQuery query = new FindAllSchoolGradesQuery(name, gradeOrder, pageable);
        return ResponseEntity.ok(findAllUseCase.handle(query));
    }

    @GetMapping("/names")
    @Operation(summary = "Listar Nomes de Séries Escolares",
            description = "Busca leve de séries escolares para componentes de seleção do front. Retorna o id, o nome e a ordem, na ordem da trilha escolar.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista de nomes retornada com sucesso",
                    content = @Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = SchoolGradeNameResponse.class))))
    })
    public ResponseEntity<List<SchoolGradeNameResponse>> findNames(
            @Parameter(description = "Filtro opcional por nome") @RequestParam(defaultValue = "") String name,
            @Parameter(description = "Quantidade máxima de resultados") @RequestParam(defaultValue = "10") int limit
    ) {
        return ResponseEntity.ok(findNamesUseCase.handle(new FindSchoolGradeNamesQuery(name, limit)));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar por ID", description = "Retorna os dados de uma série escolar pelo seu ID.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Série escolar encontrada",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = SchoolGradeResponse.class))),
            @ApiResponse(responseCode = "404", description = "Série escolar não encontrada", content = @Content)
    })
    public ResponseEntity<SchoolGradeResponse> findById(@PathVariable UUID id) {
        SchoolGrade schoolGrade = findByIdUseCase.handle(new FindSchoolGradeByIdQuery(id));
        return ResponseEntity.ok(SchoolGradeResponse.fromEntity(schoolGrade));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Excluir Série Escolar",
            description = "Realiza a exclusão lógica (soft delete) da série escolar. O registro não é removido fisicamente do banco.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Série escolar excluída com sucesso", content = @Content),
            @ApiResponse(responseCode = "404", description = "Série escolar não encontrada", content = @Content)
    })
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        softDeleteUseCase.handle(new SoftDeleteSchoolGradeCommand(id));
        return ResponseEntity.noContent().build();
    }
}
