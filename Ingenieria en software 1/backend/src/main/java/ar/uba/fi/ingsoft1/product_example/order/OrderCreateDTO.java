package ar.uba.fi.ingsoft1.product_example.order;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Schema(description = "DTO para crear una nueva orden")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderCreateDTO {

    @Schema(description = "Mapa de items: componentId (producto o combo) a cantidad", example = "{\"852\": 3, \"853\": 1}", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty
    private Map<Long, Integer> items;

    @Schema(description = "Método de pago", example = "CASH", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull
    private PaymentMethod paymentMethod;
}
