package ar.uba.fi.ingsoft1.product_example.promotions;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.NonNull;

import java.time.LocalDate;

@Schema(description = "DTO para crear una nueva promoción")
public record PromotionCreateDTO(
        @Schema(description = "Nombre de la promoción", example = "Lleva 3 paga 2", requiredMode = Schema.RequiredMode.REQUIRED) @Size(min = 3, max = 100) @NonNull String name,
        @Schema(description = "Descripción de la promoción", example = "Promoción especial de verano", requiredMode = Schema.RequiredMode.NOT_REQUIRED) @Size(min = 10, max = 500) String description,
        @Schema(description = "Fecha de inicio (formato YYYY-MM-DD)", example = "2025-01-01", requiredMode = Schema.RequiredMode.REQUIRED) @NotBlank String fromDate,
        @Schema(description = "Fecha de fin (formato YYYY-MM-DD)", example = "2025-12-31", requiredMode = Schema.RequiredMode.REQUIRED) @NotBlank String toDate,
        @Schema(description = "Expresión JSON con condiciones y acciones de la promoción", example = "{\"conditions\": [...], \"actions\": [...]}", requiredMode = Schema.RequiredMode.REQUIRED) @NotBlank String expression,
        @Schema(description = "Imagen de la promoción en base64", example = "data:image/png;base64,iVBORw0KG...") String base64Image,
        @Schema(description = "Prioridad opcional (1 = mayor prioridad)", example = "1", requiredMode = Schema.RequiredMode.NOT_REQUIRED) @Min(1) Integer priority) {
    public Promotion asPromotion() {
        LocalDate fromLocalDate = LocalDate.parse(fromDate);
        LocalDate toLocalDate = LocalDate.parse(toDate);
        if (fromLocalDate.isAfter(toLocalDate)) {
            throw new IllegalArgumentException("fromDate must be before toDate");
        }
        return new Promotion(name, description, fromLocalDate, toLocalDate, expression, base64Image, priority);
    }
}
