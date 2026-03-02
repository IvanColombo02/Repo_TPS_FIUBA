package ar.uba.fi.ingsoft1.product_example.promotions;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

@Schema(description = "DTO para reordenar las prioridades de las promociones")
public record PromotionPriorityUpdateDTO(
        @Schema(description = "Lista ordenada de IDs. La posición define la prioridad (1 = mayor prioridad).", example = "[3,1,2]", requiredMode = Schema.RequiredMode.REQUIRED) @NotEmpty List<@NotNull Long> orderedPromotionIds) {
}
