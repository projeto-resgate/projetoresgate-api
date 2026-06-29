package com.projetoresgate.projetoresgate_api.core.identity.naturalperson.api;

import com.projetoresgate.projetoresgate_api.core.identity.naturalperson.api.dto.NaturalPersonResponse;
import com.projetoresgate.projetoresgate_api.core.identity.naturalperson.domain.NaturalPerson;
import com.projetoresgate.projetoresgate_api.core.identity.naturalperson.domain.enums.Gender;
import com.projetoresgate.projetoresgate_api.core.identity.naturalperson.usecase.*;
import com.projetoresgate.projetoresgate_api.core.identity.naturalperson.usecase.command.CreateNaturalPersonCommand;
import com.projetoresgate.projetoresgate_api.core.identity.naturalperson.usecase.command.SoftDeleteNaturalPersonCommand;
import com.projetoresgate.projetoresgate_api.core.identity.naturalperson.usecase.command.UpdateNaturalPersonCommand;
import com.projetoresgate.projetoresgate_api.core.identity.naturalperson.usecase.query.FindNaturalPersonByIdQuery;
import com.projetoresgate.projetoresgate_api.core.identity.naturalperson.usecase.query.FindNaturalPersonByUserIdQuery;
import com.projetoresgate.projetoresgate_api.core.identity.naturalperson.usecase.query.SearchNaturalPersonQuery;
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
@RequestMapping("/natural-person")
@Tag(name = "Natural Person", description = "Endpoints para gerenciamento de Pessoas Físicas")
public class NaturalPersonController {

    private final CreateNaturalPersonUseCase createUseCase;
    private final UpdateNaturalPersonUseCase updateUseCase;
    private final SoftDeleteNaturalPersonUseCase softDeleteUseCase;
    private final FindNaturalPersonByIdUseCase findByIdUseCase;
    private final FindNaturalPersonByUserIdUseCase findByUserIdUseCase;
    private final SearchNaturalPersonUseCase searchUseCase;

    public NaturalPersonController(CreateNaturalPersonUseCase createUseCase,
                                   UpdateNaturalPersonUseCase updateUseCase,
                                   SoftDeleteNaturalPersonUseCase softDeleteUseCase,
                                   FindNaturalPersonByIdUseCase findByIdUseCase,
                                   FindNaturalPersonByUserIdUseCase findByUserIdUseCase,
                                   SearchNaturalPersonUseCase searchUseCase) {
        this.createUseCase = createUseCase;
        this.updateUseCase = updateUseCase;
        this.softDeleteUseCase = softDeleteUseCase;
        this.findByIdUseCase = findByIdUseCase;
        this.findByUserIdUseCase = findByUserIdUseCase;
        this.searchUseCase = searchUseCase;
    }

    @PostMapping
    @Operation(summary = "Criar Pessoa Física", description = "Cadastra uma nova pessoa física. Sempre cria um novo usuário vinculado sem senha inicial.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Pessoa física criada com sucesso",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = NaturalPersonResponse.class))),
            @ApiResponse(responseCode = "400", description = "Dados inválidos ou CPF já cadastrado", content = @Content)
    })
    public ResponseEntity<NaturalPersonResponse> create(@RequestBody @Valid CreateNaturalPersonCommand command) {
        NaturalPerson person = createUseCase.handle(command);
        return ResponseEntity.status(HttpStatus.CREATED).body(NaturalPersonResponse.fromEntity(person));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualizar Pessoa Física", description = "Atualiza os dados de uma pessoa física existente.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Pessoa física atualizada com sucesso",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = NaturalPersonResponse.class))),
            @ApiResponse(responseCode = "400", description = "Dados inválidos ou CPF já cadastrado para outra pessoa", content = @Content),
            @ApiResponse(responseCode = "404", description = "Pessoa física não encontrada", content = @Content)
    })
    public ResponseEntity<NaturalPersonResponse> update(@PathVariable UUID id, @RequestBody @Valid UpdateNaturalPersonCommand command) {
        NaturalPerson person = updateUseCase.handle(command.withId(id));
        return ResponseEntity.ok(NaturalPersonResponse.fromEntity(person));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Deletar Pessoa Física (Soft Delete)", description = "Marca uma pessoa física como deletada sem remover do banco.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Pessoa física deletada com sucesso", content = @Content),
            @ApiResponse(responseCode = "404", description = "Pessoa física não encontrada", content = @Content)
    })
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        SoftDeleteNaturalPersonCommand command = new SoftDeleteNaturalPersonCommand(id);
        softDeleteUseCase.handle(command);
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    @Operation(summary = "Listar com Filtros", description = "Lista pessoas físicas com paginação e filtros opcionais.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista retornada com sucesso",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = Page.class)))
    })
    public ResponseEntity<Page<NaturalPersonResponse>> search(
            @Parameter(description = "Termo de pesquisa (Nome, Nickname, CPF ou RG)") @RequestParam(required = false) String searchTerm,
            @Parameter(description = "CPF") @RequestParam(required = false) String cpf,
            @Parameter(description = "RG") @RequestParam(required = false) String rg,
            @Parameter(description = "Celular") @RequestParam(required = false) String cellphone,
            @Parameter(description = "Gênero") @RequestParam(required = false) Gender gender,
            @Parameter(description = "Número da página") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Tamanho da página") @RequestParam(defaultValue = "10") int size
    ) {
        Pageable pageable = PageRequest.of(page, size);
        SearchNaturalPersonQuery query = new SearchNaturalPersonQuery(searchTerm, rg, cpf, cellphone, gender, pageable);
        Page<NaturalPerson> pageResult = searchUseCase.handle(query);
        return ResponseEntity.ok(pageResult.map(NaturalPersonResponse::fromEntity));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar por ID", description = "Retorna os dados de uma pessoa física pelo seu ID.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Pessoa física encontrada",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = NaturalPersonResponse.class))),
            @ApiResponse(responseCode = "404", description = "Pessoa física não encontrada", content = @Content)
    })
    public ResponseEntity<NaturalPersonResponse> findById(@PathVariable UUID id) {
        FindNaturalPersonByIdQuery query = new FindNaturalPersonByIdQuery(id);
        NaturalPerson person = findByIdUseCase.handle(query);
        return ResponseEntity.ok(NaturalPersonResponse.fromEntity(person));
    }

    @GetMapping("/user/{userId}")
    @Operation(summary = "Buscar por ID de Usuário", description = "Retorna os dados de uma pessoa física vinculada a um usuário específico.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Pessoa física encontrada",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = NaturalPersonResponse.class))),
            @ApiResponse(responseCode = "404", description = "Pessoa física não encontrada para este usuário", content = @Content)
    })
    public ResponseEntity<NaturalPersonResponse> findByUserId(@PathVariable UUID userId) {
        FindNaturalPersonByUserIdQuery query = new FindNaturalPersonByUserIdQuery(userId);
        NaturalPerson person = findByUserIdUseCase.handle(query);
        return ResponseEntity.ok(NaturalPersonResponse.fromEntity(person));
    }
}
