package ar.uba.fi.ingsoft1.product_example.order;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Data Transfer Object for OrderItem entity.
 * Contains snapshot data of the product/combo at purchase time.
 */
@Schema(description = "DTO con la información de un item de orden")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderItemDTO {

    @Schema(description = "ID del item", example = "131")
    private Long id;
    @Schema(description = "ID del componente (producto o combo)", example = "852")
    private Long componentId;
    @Schema(description = "Nombre del item", example = "Alfajor")
    private String itemName;
    @Schema(description = "Precio unitario del item", example = "3000.0")
    private float itemPrice;
    @Schema(description = "Cantidad del item", example = "3")
    private int quantity;
    @Schema(description = "Subtotal del item (sin descuentos)", example = "9000.0")
    private float subtotal;
    @Schema(description = "Descuento aplicado al item", example = "3010.0")
    private Float discount;
    @Schema(description = "Descripción de la promoción aplicada", example = "Lleva 3 paga 2; Descuento fijo: $10.0")
    private String promotionApplied;

    /**
     * Converts OrderItem entity to DTO.
     */
    public static OrderItemDTO fromEntity(OrderItem entity) {
        return new OrderItemDTO(
                entity.getId(),
                entity.getComponent() != null ? entity.getComponent().getId() : null,
                entity.getItemName(),
                entity.getItemPrice(),
                entity.getQuantity(),
                entity.getSubtotal(),
                entity.getDiscount(),
                entity.getPromotionApplied());
    }
}
