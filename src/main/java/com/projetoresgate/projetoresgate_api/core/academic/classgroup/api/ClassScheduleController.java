package com.projetoresgate.projetoresgate_api.core.academic.classgroup.api;

import com.projetoresgate.projetoresgate_api.core.academic.classgroup.api.dto.ClassScheduleResponse;
import com.projetoresgate.projetoresgate_api.core.academic.classgroup.domain.enums.WeekDay;
import com.projetoresgate.projetoresgate_api.core.academic.classgroup.usecase.CreateClassScheduleUseCase;
import com.projetoresgate.projetoresgate_api.core.academic.classgroup.usecase.FindAllClassSchedulesUseCase;
import com.projetoresgate.projetoresgate_api.core.academic.classgroup.usecase.SoftDeleteClassScheduleUseCase;
import com.projetoresgate.projetoresgate_api.core.academic.classgroup.usecase.UpdateClassScheduleUseCase;
import com.projetoresgate.projetoresgate_api.core.academic.classgroup.usecase.command.CreateClassScheduleCommand;
import com.projetoresgate.projetoresgate_api.core.academic.classgroup.usecase.command.SoftDeleteClassScheduleCommand;
import com.projetoresgate.projetoresgate_api.core.academic.classgroup.usecase.command.UpdateClassScheduleCommand;
import com.projetoresgate.projetoresgate_api.core.academic.classgroup.usecase.query.FindAllClassSchedulesQuery;
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
@RequestMapping("/class-group/{classGroupId}/schedule")
@Tag(name = "Class Schedule", description = "Endpoints para gerenciamento da Grade de Horários da Turma")
public class ClassScheduleController {

    private final CreateClassScheduleUseCase createUseCase;
    private final UpdateClassScheduleUseCase updateUseCase;
    private final SoftDeleteClassScheduleUseCase softDeleteUseCase;
    private final FindAllClassSchedulesUseCase findAllUseCase;

    public ClassScheduleController(CreateClassScheduleUseCase createUseCase,
                                   UpdateClassScheduleUseCase updateUseCase,
                                   SoftDeleteClassScheduleUseCase softDeleteUseCase,
                                   FindAllClassSchedulesUseCase findAllUseCase) {
        this.createUseCase = createUseCase;
        this.updateUseCase = updateUseCase;
        this.softDeleteUseCase = softDeleteUseCase;
        this.findAllUseCase = findAllUseCase;
    }

    @PostMapping
    @Operation(summary = "Criar Horário", description = "Adiciona um horário à grade da turma e recalcula o título da turma.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Horário criado com sucesso",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ClassScheduleResponse.class))),
            @ApiResponse(responseCode = "400", description = "Dados inválidos", content = @Content),
            @ApiResponse(responseCode = "404", description = "Turma não encontrada", content = @Content)
    })
    public ResponseEntity<ClassScheduleResponse> create(@PathVariable UUID classGroupId,
                                                        @RequestBody @Valid CreateClassScheduleCommand command) {
        ClassScheduleResponse response = createUseCase.handle(command.withClassGroupId(classGroupId));
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualizar Horário", description = "Atualiza um horário da grade da turma e recalcula o título da turma.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Horário atualizado com sucesso",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ClassScheduleResponse.class))),
            @ApiResponse(responseCode = "400", description = "Dados inválidos", content = @Content),
            @ApiResponse(responseCode = "404", description = "Horário ou turma não encontrado", content = @Content)
    })
    public ResponseEntity<ClassScheduleResponse> update(@PathVariable UUID classGroupId,
                                                        @PathVariable UUID id,
                                                        @RequestBody @Valid UpdateClassScheduleCommand command) {
        return ResponseEntity.ok(updateUseCase.handle(command.withRoute(classGroupId, id)));
    }

    @GetMapping
    @Operation(summary = "Listar Horários",
            description = "Lista os horários da turma com paginação e filtros opcionais. Ordena primeiro os vínculos ativos, "
                    + "depois pelo dia da semana e pelo horário de início.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista retornada com sucesso",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = Page.class))),
            @ApiResponse(responseCode = "404", description = "Turma não encontrada", content = @Content)
    })
    public ResponseEntity<Page<ClassScheduleResponse>> findAll(
            @PathVariable UUID classGroupId,
            @Parameter(description = "Dia da semana") @RequestParam(required = false) WeekDay weekDay,
            @Parameter(description = "Situação do vínculo") @RequestParam(required = false) Boolean active,
            @Parameter(description = "Número da página") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Tamanho da página") @RequestParam(defaultValue = "10") int size
    ) {
        Pageable pageable = PageRequest.of(page, size,
                Sort.by(Sort.Order.desc("active"), Sort.Order.asc("weekDay"), Sort.Order.asc("startTime")));
        FindAllClassSchedulesQuery query = new FindAllClassSchedulesQuery(classGroupId, weekDay, active, pageable);
        return ResponseEntity.ok(findAllUseCase.handle(query));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Excluir Horário",
            description = "Realiza a exclusão lógica (soft delete) do horário da turma e recalcula o título da turma.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Horário excluído com sucesso", content = @Content),
            @ApiResponse(responseCode = "404", description = "Horário ou turma não encontrado", content = @Content)
    })
    public ResponseEntity<Void> delete(@PathVariable UUID classGroupId, @PathVariable UUID id) {
        softDeleteUseCase.handle(new SoftDeleteClassScheduleCommand(classGroupId, id));
        return ResponseEntity.noContent().build();
    }
}
