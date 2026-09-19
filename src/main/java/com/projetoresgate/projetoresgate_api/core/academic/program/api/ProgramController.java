package com.projetoresgate.projetoresgate_api.core.academic.program.api;

import com.projetoresgate.projetoresgate_api.core.academic.program.api.dto.EducatorCategoryItemNameResponse;
import com.projetoresgate.projetoresgate_api.core.academic.program.api.dto.ProgramNameResponse;
import com.projetoresgate.projetoresgate_api.core.academic.program.api.dto.ProgramResponse;
import com.projetoresgate.projetoresgate_api.core.academic.program.domain.enums.ProgramStatus;
import com.projetoresgate.projetoresgate_api.core.academic.program.usecase.*;
import com.projetoresgate.projetoresgate_api.core.academic.program.usecase.command.*;
import com.projetoresgate.projetoresgate_api.core.academic.program.usecase.query.FindEducatorCategoryItemNamesQuery;
import com.projetoresgate.projetoresgate_api.core.academic.program.usecase.query.FindProgramByIdQuery;
import com.projetoresgate.projetoresgate_api.core.academic.program.usecase.query.FindProgramNamesQuery;
import com.projetoresgate.projetoresgate_api.core.academic.program.usecase.query.SearchProgramQuery;
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

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/program")
@Tag(name = "Program", description = "Endpoints para gerenciamento de Programas")
public class ProgramController {

    private final CreateProgramUseCase createUseCase;
    private final UpdateProgramUseCase updateUseCase;
    private final SoftDeleteProgramUseCase softDeleteUseCase;
    private final FindProgramByIdUseCase findByIdUseCase;
    private final SearchProgramUseCase searchUseCase;
    private final FindProgramNamesUseCase findProgramNamesUseCase;
    private final FindEducatorCategoryItemNamesUseCase findEducatorCategoryItemNamesUseCase;

    public ProgramController(CreateProgramUseCase createUseCase,
                             UpdateProgramUseCase updateUseCase,
                             SoftDeleteProgramUseCase softDeleteUseCase,
                             FindProgramByIdUseCase findByIdUseCase,
                             SearchProgramUseCase searchUseCase,
                             FindProgramNamesUseCase findProgramNamesUseCase,
                             FindEducatorCategoryItemNamesUseCase findEducatorCategoryItemNamesUseCase) {
        this.createUseCase = createUseCase;
        this.updateUseCase = updateUseCase;
        this.softDeleteUseCase = softDeleteUseCase;
        this.findByIdUseCase = findByIdUseCase;
        this.searchUseCase = searchUseCase;
        this.findProgramNamesUseCase = findProgramNamesUseCase;
        this.findEducatorCategoryItemNamesUseCase = findEducatorCategoryItemNamesUseCase;
    }

    @PostMapping
    @Operation(summary = "Criar Programa", description = "Cadastra um novo programa, podendo nascer já com a lista de categorias de educadores populada ou vazia.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Programa criado com sucesso",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ProgramResponse.class))),
            @ApiResponse(responseCode = "400", description = "Dados inválidos ou nome já cadastrado", content = @Content),
            @ApiResponse(responseCode = "404", description = "Instituição não encontrada", content = @Content)
    })
    public ResponseEntity<ProgramResponse> create(@RequestBody @Valid CreateProgramCommand command) {
        ProgramResponse response = createUseCase.handle(command);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(response);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualizar Programa", description = "Atualiza os dados de um programa existente.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Programa atualizado com sucesso",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ProgramResponse.class))),
            @ApiResponse(responseCode = "400", description = "Dados inválidos ou nome já cadastrado para outro programa", content = @Content),
            @ApiResponse(responseCode = "404", description = "Programa não encontrado", content = @Content)
    })
    public ResponseEntity<ProgramResponse> update(@PathVariable UUID id, @RequestBody @Valid UpdateProgramCommand command) {
        ProgramResponse response = updateUseCase.handle(command.withId(id));
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Deletar Programa (Soft Delete)", description = "Marca um programa como deletado sem remover do banco.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Programa deletado com sucesso", content = @Content),
            @ApiResponse(responseCode = "404", description = "Programa não encontrado", content = @Content)
    })
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        softDeleteUseCase.handle(new SoftDeleteProgramCommand(id));
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    @Operation(summary = "Listar com Filtros", description = "Lista programas com paginação e filtros opcionais.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista retornada com sucesso",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = Page.class)))
    })
    public ResponseEntity<Page<ProgramResponse>> search(
            @Parameter(description = "Status") @RequestParam(required = false) ProgramStatus status,
            @Parameter(description = "Id da instituição") @RequestParam(required = false) UUID institutionId,
            @Parameter(description = "Número da página") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Tamanho da página") @RequestParam(defaultValue = "10") int size
    ) {
        Pageable pageable = PageRequest.of(page, size);
        SearchProgramQuery query = new SearchProgramQuery(status, institutionId, pageable);
        return ResponseEntity.ok(searchUseCase.handle(query));
    }

    @GetMapping("/names")
    @Operation(summary = "Listar Nomes de Programas", description = "Retorna apenas os nomes dos programas, com filtro opcional por nome (ilike).")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista de nomes retornada com sucesso",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ProgramNameResponse.class)))
    })
    public ResponseEntity<List<ProgramNameResponse>> findNames(
            @Parameter(description = "Filtro opcional por nome") @RequestParam(required = false) String name
    ) {
        List<ProgramNameResponse> names = findProgramNamesUseCase.handle(new FindProgramNamesQuery(name));
        return ResponseEntity.ok(names);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar por ID", description = "Retorna os dados de um programa pelo seu ID.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Programa encontrado",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ProgramResponse.class))),
            @ApiResponse(responseCode = "404", description = "Programa não encontrado", content = @Content)
    })
    public ResponseEntity<ProgramResponse> findById(@PathVariable UUID id) {
        ProgramResponse response = findByIdUseCase.handle(new FindProgramByIdQuery(id));
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}/educator-category-items/names")
    @Operation(summary = "Listar Nomes de Categorias de Educadores", description = "Retorna apenas os nomes das categorias de educadores de um programa, com filtro opcional por nome (ilike).")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista de nomes retornada com sucesso",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = EducatorCategoryItemNameResponse.class))),
            @ApiResponse(responseCode = "404", description = "Programa não encontrado", content = @Content)
    })
    public ResponseEntity<List<EducatorCategoryItemNameResponse>> findEducatorCategoryItemNames(
            @PathVariable UUID id,
            @Parameter(description = "Filtro opcional por nome") @RequestParam(required = false) String name
    ) {
        List<EducatorCategoryItemNameResponse> names =
                findEducatorCategoryItemNamesUseCase.handle(new FindEducatorCategoryItemNamesQuery(id, name));
        return ResponseEntity.ok(names);
    }

}