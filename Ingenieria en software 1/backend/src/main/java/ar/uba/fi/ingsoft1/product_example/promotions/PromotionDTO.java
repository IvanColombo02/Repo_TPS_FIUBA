package ar.uba.fi.ingsoft1.product_example.promotions;

import static ar.uba.fi.ingsoft1.product_example.promotions.PromotionConstants.DEFAULT_PRIORITY;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "DTO con la información de una promoción")
public record PromotionDTO(
        @Schema(description = "ID de la promoción", example = "1") long id,
        @Schema(description = "Nombre de la promoción", example = "Lleva 3 paga 2") String name,
        @Schema(description = "Descripción de la promoción", example = "Promoción especial de verano") String description,
        @Schema(description = "Fecha de inicio (formato YYYY-MM-DD)", example = "2025-01-01") String fromDate,
        @Schema(description = "Fecha de fin (formato YYYY-MM-DD)", example = "2025-12-31") String toDate,
        @Schema(description = "Expresión JSON con condiciones y acciones de la promoción", example = "{\"conditions\": [...], \"actions\": [...]}") String expression, // JSON
        @Schema(description = "Imagen de la promoción en base64", example = "data:image/png;base64,iVBORw0KG...") String base64Image,
        @Schema(description = "Prioridad de ejecución (1 = mayor prioridad)", example = "1") int priority) {
    public PromotionDTO(Promotion promotion) {
        this(
                promotion.getId(),
                promotion.getName(),
                promotion.getDescription(),
                promotion.getFromDate().toString(),
                promotion.getToDate().toString(),
                promotion.getExpression(),
                promotion.getBase64Image(),
                promotion.getPriority() != null ? promotion.getPriority() : DEFAULT_PRIORITY);
    }
}
