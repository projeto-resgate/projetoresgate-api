package com.projetoresgate.projetoresgate_api.core.identity.legalperson.api;

import com.projetoresgate.projetoresgate_api.core.identity.legalperson.api.dto.LegalPersonResponse;
import com.projetoresgate.projetoresgate_api.core.identity.legalperson.domain.LegalPerson;
import com.projetoresgate.projetoresgate_api.core.identity.legalperson.domain.enums.CompanyStatus;
import com.projetoresgate.projetoresgate_api.core.identity.legalperson.domain.enums.RegistrationStatus;
import com.projetoresgate.projetoresgate_api.core.identity.legalperson.usecase.*;
import com.projetoresgate.projetoresgate_api.core.identity.legalperson.usecase.command.CreateLegalPersonCommand;
import com.projetoresgate.projetoresgate_api.core.identity.legalperson.usecase.command.UpdateLegalPersonCommand;
import com.projetoresgate.projetoresgate_api.core.identity.legalperson.usecase.query.FindLegalPersonByIdQuery;
import com.projetoresgate.projetoresgate_api.core.identity.legalperson.usecase.query.SearchLegalPersonQuery;
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
@RequestMapping("/legal-person")
@Tag(name = "Legal Person", description = "Endpoints para gerenciamento de Pessoas Jurídicas")
public class LegalPersonController {

    private final CreateLegalPersonUseCase createUseCase;
    private final UpdateLegalPersonUseCase updateUseCase;
    private final FindLegalPersonByIdUseCase findByIdUseCase;
    private final SearchLegalPersonUseCase searchUseCase;

    public LegalPersonController(CreateLegalPersonUseCase createUseCase,
                                 UpdateLegalPersonUseCase updateUseCase,
                                 FindLegalPersonByIdUseCase findByIdUseCase,
                                 SearchLegalPersonUseCase searchUseCase) {
        this.createUseCase = createUseCase;
        this.updateUseCase = updateUseCase;
        this.findByIdUseCase = findByIdUseCase;
        this.searchUseCase = searchUseCase;
    }

    @PostMapping
    @Operation(summary = "Criar Pessoa Jurídica", description = "Cadastra uma nova pessoa jurídica.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Pessoa jurídica criada com sucesso",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = LegalPersonResponse.class))),
            @ApiResponse(responseCode = "400", description = "Dados inválidos ou CNPJ já cadastrado", content = @Content)
    })
    public ResponseEntity<LegalPersonResponse> create(@RequestBody @Valid CreateLegalPersonCommand command) {
        LegalPerson person = createUseCase.handle(command);
        return ResponseEntity.status(HttpStatus.CREATED).body(LegalPersonResponse.fromEntity(person));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualizar Pessoa Jurídica", description = "Atualiza os dados de uma pessoa jurídica existente.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Pessoa jurídica atualizada com sucesso",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = LegalPersonResponse.class))),
            @ApiResponse(responseCode = "400", description = "Dados inválidos ou CNPJ já cadastrado para outra empresa", content = @Content),
            @ApiResponse(responseCode = "404", description = "Pessoa jurídica não encontrada", content = @Content)
    })
    public ResponseEntity<LegalPersonResponse> update(@PathVariable UUID id, @RequestBody @Valid UpdateLegalPersonCommand command) {
        LegalPerson person = updateUseCase.handle(command.withId(id));
        return ResponseEntity.ok(LegalPersonResponse.fromEntity(person));
    }

    @GetMapping
    @Operation(summary = "Listar com Filtros", description = "Lista pessoas jurídicas com paginação e filtros opcionais.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista retornada com sucesso",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = Page.class)))
    })
    public ResponseEntity<Page<LegalPersonResponse>> search(
            @Parameter(description = "Termo de pesquisa (Razão Social, Nome Fantasia, Nome de Exibição ou CNPJ)") @RequestParam(required = false) String searchTerm,
            @Parameter(description = "CNPJ") @RequestParam(required = false) String cnpj,
            @Parameter(description = "Razão Social") @RequestParam(required = false) String corporateName,
            @Parameter(description = "Status de registro") @RequestParam(required = false) RegistrationStatus registrationStatus,
            @Parameter(description = "Status da empresa") @RequestParam(required = false) CompanyStatus companyStatus,
            @Parameter(description = "Número da página") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Tamanho da página") @RequestParam(defaultValue = "10") int size
    ) {
        Pageable pageable = PageRequest.of(page, size);
        SearchLegalPersonQuery query = new SearchLegalPersonQuery(searchTerm, cnpj, corporateName, registrationStatus, companyStatus, pageable);
        Page<LegalPerson> pageResult = searchUseCase.handle(query);
        return ResponseEntity.ok(pageResult.map(LegalPersonResponse::fromEntity));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar por ID", description = "Retorna os dados de uma pessoa jurídica pelo seu ID.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Pessoa jurídica encontrada",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = LegalPersonResponse.class))),
            @ApiResponse(responseCode = "404", description = "Pessoa jurídica não encontrada", content = @Content)
    })
    public ResponseEntity<LegalPersonResponse> findById(@PathVariable UUID id) {
        FindLegalPersonByIdQuery query = new FindLegalPersonByIdQuery(id);
        LegalPerson person = findByIdUseCase.handle(query);
        return ResponseEntity.ok(LegalPersonResponse.fromEntity(person));
    }
}
