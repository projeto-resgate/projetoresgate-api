package com.projetoresgate.projetoresgate_api.core.identity.familygroup.usecase.command;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.projetoresgate.projetoresgate_api.core.identity.address.api.command.AddressCommand;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.util.UUID;

public record UpdateFamilyGroupCommand(
        @JsonIgnore
        UUID id,
        @NotBlank(message = "O nome do grupo familiar é obrigatório")
        @Size(max = 255, message = "O nome do grupo familiar não pode exceder 255 caracteres")
        String name,
        @DecimalMin(value = "0.00", message = "A renda familiar não pode ser negativa")
        BigDecimal householdIncome,
        @DecimalMin(value = "0.00", message = "A renda per capita não pode ser negativa")
        BigDecimal perCapitaIncome,
        @DecimalMin(value = "0.00", message = "A despesa com educação não pode ser negativa")
        BigDecimal educationExpense,
        @DecimalMin(value = "0.00", message = "A despesa com saúde não pode ser negativa")
        BigDecimal healthExpense,
        @DecimalMin(value = "0.00", message = "A despesa com moradia não pode ser negativa")
        BigDecimal housingExpense,
        @Min(value = 1, message = "O número de moradores deve ser maior que zero")
        Integer numberOfResidents,
        @Valid
        AddressCommand address
) {
    public UpdateFamilyGroupCommand withId(UUID id) {
        return new UpdateFamilyGroupCommand(
                id,
                this.name,
                this.householdIncome,
                this.perCapitaIncome,
                this.educationExpense,
                this.healthExpense,
                this.housingExpense,
                this.numberOfResidents,
                this.address
        );
    }
}
