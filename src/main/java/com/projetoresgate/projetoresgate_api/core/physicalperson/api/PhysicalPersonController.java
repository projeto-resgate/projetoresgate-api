package com.projetoresgate.projetoresgate_api.core.physicalperson.api;

import com.projetoresgate.projetoresgate_api.core.physicalperson.api.dto.PhysicalPersonResponse;
import com.projetoresgate.projetoresgate_api.core.physicalperson.domain.PhysicalPerson;
import com.projetoresgate.projetoresgate_api.core.physicalperson.domain.enums.Gender;
import com.projetoresgate.projetoresgate_api.core.physicalperson.domain.valueobjects.Cpf;
import com.projetoresgate.projetoresgate_api.core.physicalperson.domain.valueobjects.Rg;
import com.projetoresgate.projetoresgate_api.core.physicalperson.usecase.*;
import com.projetoresgate.projetoresgate_api.core.physicalperson.usecase.command.CreatePhysicalPersonCommand;
import com.projetoresgate.projetoresgate_api.core.physicalperson.usecase.command.SoftDeletePhysicalPersonCommand;
import com.projetoresgate.projetoresgate_api.core.physicalperson.usecase.command.UpdatePhysicalPersonCommand;
import com.projetoresgate.projetoresgate_api.core.physicalperson.usecase.query.FindPhysicalPersonByIdQuery;
import com.projetoresgate.projetoresgate_api.core.physicalperson.usecase.query.FindPhysicalPersonByUserIdQuery;
import com.projetoresgate.projetoresgate_api.core.physicalperson.usecase.query.SearchPhysicalPersonQuery;
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
@RequestMapping("/physical-person")
@Tag(name = "Physical Person", description = "Endpoints para gerenciamento de Pessoas Físicas")
public class PhysicalPersonController {

    private final CreatePhysicalPersonUseCase createUseCase;
    private final UpdatePhysicalPersonUseCase updateUseCase;
    private final SoftDeletePhysicalPersonUseCase softDeleteUseCase;
    private final FindPhysicalPersonByIdUseCase findByIdUseCase;
    private final FindPhysicalPersonByUserIdUseCase findByUserIdUseCase;
    private final SearchPhysicalPersonUseCase searchUseCase;

    public PhysicalPersonController(CreatePhysicalPersonUseCase createUseCase,
                                    UpdatePhysicalPersonUseCase updateUseCase,
                                    SoftDeletePhysicalPersonUseCase softDeleteUseCase,
                                    FindPhysicalPersonByIdUseCase findByIdUseCase,
                                    FindPhysicalPersonByUserIdUseCase findByUserIdUseCase,
                                    SearchPhysicalPersonUseCase searchUseCase) {
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
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = PhysicalPersonResponse.class))),
            @ApiResponse(responseCode = "400", description = "Dados inválidos ou CPF já cadastrado", content = @Content)
    })
    public ResponseEntity<PhysicalPersonResponse> create(@RequestBody @Valid CreatePhysicalPersonCommand command) {
        PhysicalPerson person = createUseCase.handle(command);
        return ResponseEntity.status(HttpStatus.CREATED).body(PhysicalPersonResponse.fromEntity(person));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualizar Pessoa Física", description = "Atualiza os dados de uma pessoa física existente.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Pessoa física atualizada com sucesso",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = PhysicalPersonResponse.class))),
            @ApiResponse(responseCode = "400", description = "Dados inválidos ou CPF já cadastrado para outra pessoa", content = @Content),
            @ApiResponse(responseCode = "404", description = "Pessoa física não encontrada", content = @Content)
    })
    public ResponseEntity<PhysicalPersonResponse> update(@PathVariable UUID id, @RequestBody @Valid UpdatePhysicalPersonCommand command) {
        PhysicalPerson person = updateUseCase.handle(command.withId(id));
        return ResponseEntity.ok(PhysicalPersonResponse.fromEntity(person));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Deletar Pessoa Física (Soft Delete)", description = "Marca uma pessoa física como deletada sem remover do banco.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Pessoa física deletada com sucesso", content = @Content),
            @ApiResponse(responseCode = "404", description = "Pessoa física não encontrada", content = @Content)
    })
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        SoftDeletePhysicalPersonCommand command = new SoftDeletePhysicalPersonCommand(id);
        softDeleteUseCase.handle(command);
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    @Operation(summary = "Listar com Filtros", description = "Lista pessoas físicas com paginação e filtros opcionais.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista retornada com sucesso",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = Page.class)))
    })
    public ResponseEntity<Page<PhysicalPersonResponse>> search(
            @Parameter(description = "Termo de pesquisa (Nome, Nickname, CPF ou RG)") @RequestParam(required = false) String searchTerm,
            @Parameter(description = "CPF") @RequestParam(required = false) String cpf,
            @Parameter(description = "RG") @RequestParam(required = false) String rg,
            @Parameter(description = "Celular") @RequestParam(required = false) String cellphone,
            @Parameter(description = "Gênero") @RequestParam(required = false) Gender gender,
            @Parameter(description = "Número da página") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Tamanho da página") @RequestParam(defaultValue = "10") int size
    ) {
        Pageable pageable = PageRequest.of(page, size);
        SearchPhysicalPersonQuery query = new SearchPhysicalPersonQuery(searchTerm, new Rg(rg), new Cpf(cpf), cellphone, gender, pageable);
        Page<PhysicalPerson> pageResult = searchUseCase.handle(query);
        return ResponseEntity.ok(pageResult.map(PhysicalPersonResponse::fromEntity));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar por ID", description = "Retorna os dados de uma pessoa física pelo seu ID.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Pessoa física encontrada",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = PhysicalPersonResponse.class))),
            @ApiResponse(responseCode = "404", description = "Pessoa física não encontrada", content = @Content)
    })
    public ResponseEntity<PhysicalPersonResponse> findById(@PathVariable UUID id) {
        FindPhysicalPersonByIdQuery query = new FindPhysicalPersonByIdQuery(id);
        PhysicalPerson person = findByIdUseCase.handle(query);
        return ResponseEntity.ok(PhysicalPersonResponse.fromEntity(person));
    }

    @GetMapping("/user/{userId}")
    @Operation(summary = "Buscar por ID de Usuário", description = "Retorna os dados de uma pessoa física vinculada a um usuário específico.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Pessoa física encontrada",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = PhysicalPersonResponse.class))),
            @ApiResponse(responseCode = "404", description = "Pessoa física não encontrada para este usuário", content = @Content)
    })
    public ResponseEntity<PhysicalPersonResponse> findByUserId(@PathVariable UUID userId) {
        FindPhysicalPersonByUserIdQuery query = new FindPhysicalPersonByUserIdQuery(userId);
        PhysicalPerson person = findByUserIdUseCase.handle(query);
        return ResponseEntity.ok(PhysicalPersonResponse.fromEntity(person));
    }
}
