package ar.uba.fi.ingsoft1.product_example.order;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Schema(description = "DTO con el resultado del cálculo de descuentos del carrito")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CartDiscountCalculationDTO {

    @Schema(description = "Subtotal original sin descuentos", example = "100.0")
    private double originalSubtotal;

    @Schema(description = "Total de descuentos aplicados", example = "28.0")
    private double totalDiscount;

    @Schema(description = "Total final después de descuentos", example = "72.0")
    private double finalTotal;

    @Schema(description = "Lista de descuentos aplicados con sus descripciones")
    private List<AppliedDiscountDTO> appliedDiscounts;

    @Schema(description = "Descripción de las promociones aplicadas")
    private List<String> promotionDescriptions;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AppliedDiscountDTO {
        @Schema(description = "ID de la promoción", example = "1")
        private Long promotionId;

        @Schema(description = "Nombre de la promoción", example = "Descuento 20%")
        private String promotionName;

        @Schema(description = "Monto del descuento", example = "18.0")
        private double discount;

        @Schema(description = "Descripción del descuento", example = "Descuento 20%: 18.0% de descuento")
        private String description;
    }
}
