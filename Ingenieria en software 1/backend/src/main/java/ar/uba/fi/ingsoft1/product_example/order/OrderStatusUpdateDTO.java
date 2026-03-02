package ar.uba.fi.ingsoft1.product_example.order;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Schema(description = "DTO para actualizar el estado de una orden")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderStatusUpdateDTO {

    @Schema(description = "Nuevo estado de la orden", example = "IN_PREPARATION", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull
    private OrderStatus status;
}
