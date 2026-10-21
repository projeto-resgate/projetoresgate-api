package com.projetoresgate.projetoresgate_api.core.academic.learner.api;

import com.projetoresgate.projetoresgate_api.core.academic.learner.api.dto.DropoutLearnerResponse;
import com.projetoresgate.projetoresgate_api.core.academic.learner.api.dto.EligibleResponsibleResponse;
import com.projetoresgate.projetoresgate_api.core.academic.learner.api.dto.LearnerAutocompleteResponse;
import com.projetoresgate.projetoresgate_api.core.academic.learner.api.dto.LearnerResponse;
import com.projetoresgate.projetoresgate_api.core.academic.learner.api.dto.LearnerSummaryResponse;
import com.projetoresgate.projetoresgate_api.core.academic.learner.domain.enums.LearnerStatus;
import com.projetoresgate.projetoresgate_api.core.academic.learner.usecase.*;
import com.projetoresgate.projetoresgate_api.core.academic.learner.usecase.command.CreateLearnerCommand;
import com.projetoresgate.projetoresgate_api.core.academic.learner.usecase.command.DropoutLearnerCommand;
import com.projetoresgate.projetoresgate_api.core.academic.learner.usecase.command.ReactivateLearnerCommand;
import com.projetoresgate.projetoresgate_api.core.academic.learner.usecase.command.SoftDeleteLearnerCommand;
import com.projetoresgate.projetoresgate_api.core.academic.learner.usecase.command.UpdateLearnerCommand;
import com.projetoresgate.projetoresgate_api.core.academic.learner.usecase.query.AutocompleteLearnerQuery;
import com.projetoresgate.projetoresgate_api.core.academic.learner.usecase.query.FindAllLearnersQuery;
import com.projetoresgate.projetoresgate_api.core.academic.learner.usecase.query.FindEligibleResponsiblesQuery;
import com.projetoresgate.projetoresgate_api.core.academic.learner.usecase.query.FindLearnerByIdQuery;
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
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/learner")
@Tag(name = "Learner", description = "Endpoints para gerenciamento de Educandos")
public class LearnerController {

    private final CreateLearnerUseCase createUseCase;
    private final UpdateLearnerUseCase updateUseCase;
    private final SoftDeleteLearnerUseCase softDeleteUseCase;
    private final FindLearnerByIdUseCase findByIdUseCase;
    private final FindAllLearnersUseCase findAllUseCase;
    private final FindEligibleResponsiblesUseCase findEligibleResponsiblesUseCase;
    private final DropoutLearnerUseCase dropoutUseCase;
    private final ReactivateLearnerUseCase reactivateUseCase;
    private final AutocompleteLearnerUseCase autocompleteUseCase;

    public LearnerController(CreateLearnerUseCase createUseCase,
                             UpdateLearnerUseCase updateUseCase,
                             SoftDeleteLearnerUseCase softDeleteUseCase,
                             FindLearnerByIdUseCase findByIdUseCase,
                             FindAllLearnersUseCase findAllUseCase,
                             FindEligibleResponsiblesUseCase findEligibleResponsiblesUseCase,
                             DropoutLearnerUseCase dropoutUseCase,
                             ReactivateLearnerUseCase reactivateUseCase,
                             AutocompleteLearnerUseCase autocompleteUseCase) {
        this.createUseCase = createUseCase;
        this.updateUseCase = updateUseCase;
        this.softDeleteUseCase = softDeleteUseCase;
        this.findByIdUseCase = findByIdUseCase;
        this.findAllUseCase = findAllUseCase;
        this.findEligibleResponsiblesUseCase = findEligibleResponsiblesUseCase;
        this.dropoutUseCase = dropoutUseCase;
        this.reactivateUseCase = reactivateUseCase;
        this.autocompleteUseCase = autocompleteUseCase;
    }

    @PostMapping
    @Operation(summary = "Tornar Pessoa Física em Educando",
            description = "Cria o educando a partir de uma pessoa física já vinculada ao grupo familiar informado, salvando em lote os dados "
                    + "escolares e a lista inicial de responsáveis. Cada responsável precisa pertencer ao mesmo grupo familiar e não pode ser a "
                    + "própria pessoa física do educando.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Educando criado com sucesso",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = LearnerResponse.class))),
            @ApiResponse(responseCode = "400", description = "Dados inválidos, pessoa física fora do grupo familiar ou responsável repetido", content = @Content),
            @ApiResponse(responseCode = "404", description = "Grupo familiar, pessoa física, instituição ou série escolar não encontrada", content = @Content),
            @ApiResponse(responseCode = "409", description = "A pessoa física já é um educando cadastrado", content = @Content)
    })
    public ResponseEntity<LearnerResponse> create(@RequestBody @Valid CreateLearnerCommand command) {
        LearnerResponse response = createUseCase.handle(command);

        URI location = ServletUriComponentsBuilder.fromCurrentContextPath()
                .path("/learner/{id}")
                .buildAndExpand(response.id())
                .toUri();

        return ResponseEntity.status(HttpStatus.CREATED).location(location).body(response);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualizar Educando e Responsáveis",
            description = "Atualiza os dados escolares do educando e sincroniza a lista de responsáveis: vínculos com id são atualizados no lugar, "
                    + "vínculos sem id são criados e os que não vierem na lista são removidos.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Educando atualizado com sucesso",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = LearnerResponse.class))),
            @ApiResponse(responseCode = "400", description = "Dados inválidos, pessoa física fora do grupo familiar ou tentativa de trocar o educando de pessoa física", content = @Content),
            @ApiResponse(responseCode = "404", description = "Educando, grupo familiar, pessoa física, instituição ou série escolar não encontrado", content = @Content)
    })
    public ResponseEntity<LearnerResponse> update(@PathVariable UUID id,
                                                  @RequestBody @Valid UpdateLearnerCommand command) {
        return ResponseEntity.ok(updateUseCase.handle(command.withId(id)));
    }

    @PostMapping("/{id}/dropout")
    @Operation(summary = "Registrar Desistência",
            description = "Marca o educando como desistente sem removê-lo das consultas. Exige o motivo e aceita "
                    + "opcionalmente a data da desistência (padrão: hoje). O vínculo com responsáveis é mantido para "
                    + "permitir uma futura reativação. Todos os vínculos ativos do educando com turmas são inativados "
                    + "automaticamente e a resposta informa quantos foram.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Desistência registrada com sucesso",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = DropoutLearnerResponse.class))),
            @ApiResponse(responseCode = "400", description = "Motivo não informado", content = @Content),
            @ApiResponse(responseCode = "404", description = "Educando não encontrado", content = @Content),
            @ApiResponse(responseCode = "409", description = "Educando já está desistente", content = @Content)
    })
    public ResponseEntity<DropoutLearnerResponse> dropOut(@PathVariable UUID id,
                                                          @RequestBody @Valid DropoutLearnerCommand command) {
        return ResponseEntity.ok(dropoutUseCase.handle(command.withId(id)));
    }

    @PostMapping("/{id}/reactivate")
    @Operation(summary = "Reativar Educando",
            description = "Devolve o educando desistente para o status ativo, preservando o histórico da desistência. "
                    + "Aceita uma observação opcional da reativação.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Educando reativado com sucesso",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = LearnerResponse.class))),
            @ApiResponse(responseCode = "404", description = "Educando não encontrado", content = @Content),
            @ApiResponse(responseCode = "409", description = "Educando não está desistente", content = @Content)
    })
    public ResponseEntity<LearnerResponse> reactivate(@PathVariable UUID id,
                                                      @RequestBody @Valid ReactivateLearnerCommand command) {
        return ResponseEntity.ok(reactivateUseCase.handle(command.withId(id)));
    }

    @GetMapping
    @Operation(summary = "Listar Educandos",
            description = "Lista os educandos com paginação e filtros opcionais, retornando o nome da pessoa física, o nome da instituição de "
                    + "ensino quando houver, o ano letivo e se o educando está ativo.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista retornada com sucesso",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = Page.class)))
    })
    public ResponseEntity<Page<LearnerSummaryResponse>> findAll(
            @Parameter(description = "Termo de pesquisa (Nome ou apelido do educando)") @RequestParam(required = false) String searchTerm,
            @Parameter(description = "Status do educando") @RequestParam(required = false) LearnerStatus status,
            @Parameter(description = "Id da instituição") @RequestParam(required = false) UUID institutionId,
            @Parameter(description = "Id da série escolar") @RequestParam(required = false) UUID schoolGradeId,
            @Parameter(description = "Ano letivo") @RequestParam(required = false) Integer academicYear,
            @Parameter(description = "Número da página") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Tamanho da página") @RequestParam(defaultValue = "10") int size
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("dateCreated").descending());
        FindAllLearnersQuery query = new FindAllLearnersQuery(
                searchTerm, status, institutionId, schoolGradeId, academicYear, pageable);
        return ResponseEntity.ok(findAllUseCase.handle(query));
    }

    @GetMapping("/autocomplete")
    @Operation(summary = "Autocomplete de Educandos",
            description = "Busca leve de educandos (por nome, apelido ou CPF da pessoa física) para componentes de "
                    + "seleção do front, como o modal de vínculo de educando à turma.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista retornada com sucesso",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = LearnerAutocompleteResponse.class)))
    })
    public ResponseEntity<List<LearnerAutocompleteResponse>> autocomplete(
            @Parameter(description = "Termo de pesquisa (Nome, apelido ou CPF do educando)") @RequestParam(required = false) String searchTerm,
            @Parameter(description = "Quantidade máxima de resultados") @RequestParam(defaultValue = "10") int limit
    ) {
        return ResponseEntity.ok(autocompleteUseCase.handle(new AutocompleteLearnerQuery(searchTerm, limit)));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar Educando por ID",
            description = "Retorna os dados escolares do educando, a instituição de ensino e a lista de responsáveis já associados com seus respectivos "
                    + "nomes, para carga do modal de edição.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Educando encontrado",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = LearnerResponse.class))),
            @ApiResponse(responseCode = "404", description = "Educando não encontrado", content = @Content)
    })
    public ResponseEntity<LearnerResponse> findById(@PathVariable UUID id) {
        return ResponseEntity.ok(findByIdUseCase.handle(new FindLearnerByIdQuery(id)));
    }

    @GetMapping("/eligible-responsibles")
    @Operation(summary = "Listar Responsáveis Elegíveis",
            description = "Lista as pessoas físicas do grupo familiar que podem virar responsável de um educando, excluindo da lista a própria "
                    + "pessoa física do educando informada em excludeNaturalPersonId. Retorna apenas os dados do seletor (id, nome, rg, cpf e celular).")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista retornada com sucesso",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = Page.class))),
            @ApiResponse(responseCode = "404", description = "Grupo familiar não encontrado", content = @Content)
    })
    public ResponseEntity<Page<EligibleResponsibleResponse>> findEligibleResponsibles(
            @Parameter(description = "Grupo familiar do educando") @RequestParam UUID familyGroupId,
            @Parameter(description = "Pessoa física do educando, que não pode ser a própria responsável")
            @RequestParam UUID excludeNaturalPersonId,
            @Parameter(description = "Número da página") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Tamanho da página") @RequestParam(defaultValue = "10") int size
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("name").ascending());
        FindEligibleResponsiblesQuery query = new FindEligibleResponsiblesQuery(
                familyGroupId, excludeNaturalPersonId, pageable);
        return ResponseEntity.ok(findEligibleResponsiblesUseCase.handle(query));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Excluir Educando",
            description = "Realiza a exclusão lógica (soft delete) do educando e dos seus responsáveis. Os registros não são removidos fisicamente do banco.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Educando excluído com sucesso", content = @Content),
            @ApiResponse(responseCode = "404", description = "Educando não encontrado", content = @Content)
    })
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        softDeleteUseCase.handle(new SoftDeleteLearnerCommand(id));
        return ResponseEntity.noContent().build();
    }
}