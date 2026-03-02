package ar.uba.fi.ingsoft1.product_example.order;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Data Transfer Object for Order entity.
 * Used in API responses for order operations.
 */
@Schema(description = "DTO con la información de una orden")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderDTO {

    @Schema(description = "ID de la orden", example = "52")
    private Long id;
    @Schema(description = "ID del usuario que realizó la orden", example = "1")
    private Long userId;
    @Schema(description = "Email del usuario", example = "jperez@fi.uba.ar")
    private String userEmail;
    @Schema(description = "Lista de items de la orden")
    private List<OrderItemDTO> items;
    @Schema(description = "Estado de la orden", example = "IN_PREPARATION")
    private OrderStatus status;
    @Schema(description = "Método de pago", example = "CASH")
    private PaymentMethod paymentMethod;
    @Schema(description = "Precio total de la orden", example = "5990.0")
    private float totalPrice;
    @Schema(description = "Fecha de creación de la orden", example = "2025-11-13T19:06:55.844001")
    private LocalDateTime createdAt;
    @Schema(description = "Fecha de última actualización", example = "2025-11-13T19:06:55.844001")
    private LocalDateTime updatedAt;

    /**
     * Converts Order entity to DTO.
     */
    public static OrderDTO fromEntity(Order entity) {
        List<OrderItemDTO> itemDTOs = entity.getItems().stream()
                .map(OrderItemDTO::fromEntity)
                .collect(Collectors.toList());

        return new OrderDTO(
                entity.getId(),
                entity.getUserId(),
                entity.getUserEmail(),
                itemDTOs,
                entity.getStatus(),
                entity.getPaymentMethod(),
                entity.getTotalPrice(),
                entity.getCreatedAt(),
                entity.getUpdatedAt());
    }
}
