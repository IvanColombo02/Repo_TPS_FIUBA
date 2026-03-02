package ar.uba.fi.ingsoft1.product_example.promotions;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.util.Optional;

@Schema(description = "DTO para actualizar una promoción. Todos los campos son opcionales. Solo se actualizan los campos enviados.", example = "{\"name\":\"Promo Verano 2025\",\"description\":\"Promoción especial de verano actualizada\",\"fromDate\":\"2025-06-01\",\"toDate\":\"2025-08-31\",\"expression\":\"{\\\"condition\\\":\\\"type\\\",\\\"action\\\":\\\"discount\\\"}\",\"base64Image\":\"data:image/png;base64,iVBORw0KG...\"}")
public record PromotionUpdateDTO(
        @Schema(description = "Nuevo nombre de la promoción", example = "Promo Verano 2025") Optional<@Size(min = 3, max = 100) String> name,
        @Schema(description = "Nueva descripción de la promoción", example = "Promoción especial de verano actualizada") Optional<@Size(min = 10, max = 500) String> description,
        @Schema(description = "Nueva fecha de inicio (formato YYYY-MM-DD)", example = "2025-06-01") Optional<String> fromDate,
        @Schema(description = "Nueva fecha de fin (formato YYYY-MM-DD)", example = "2025-08-31") Optional<String> toDate,
        @Schema(description = "Nueva expresión JSON con condiciones y acciones", example = "{\"conditions\": [...], \"actions\": [...]}") Optional<String> expression,
        @Schema(description = "Nueva imagen de la promoción en base64", example = "data:image/png;base64,iVBORw0KG...") Optional<String> base64Image) {
    public Promotion applyTo(Promotion promotion) {
        Optional<LocalDate> fromLocalDate = fromDate
                .filter(s -> s != null && !s.trim().isEmpty())
                .map(s -> {
                    try {
                        return LocalDate.parse(s);
                    } catch (Exception e) {
                        throw new IllegalArgumentException(
                                "Formato de fecha inválido para fromDate: " + s + ". Formato esperado: YYYY-MM-DD", e);
                    }
                });

        Optional<LocalDate> toLocalDate = toDate
                .filter(s -> s != null && !s.trim().isEmpty())
                .map(s -> {
                    try {
                        return LocalDate.parse(s);
                    } catch (Exception e) {
                        throw new IllegalArgumentException(
                                "Formato de fecha inválido para toDate: " + s + ". Formato esperado: YYYY-MM-DD", e);
                    }
                });

   
        if (fromLocalDate.isPresent() && toLocalDate.isPresent()) {
            if (fromLocalDate.get().isAfter(toLocalDate.get())) {
                throw new IllegalArgumentException("fromDate must be before toDate");
            }
        }

        if (fromLocalDate.isPresent() && !toLocalDate.isPresent() && promotion.getToDate() != null) {
            if (fromLocalDate.get().isAfter(promotion.getToDate())) {
                throw new IllegalArgumentException("fromDate must be before existing toDate: " + promotion.getToDate());
            }
        }

        if (toLocalDate.isPresent() && !fromLocalDate.isPresent() && promotion.getFromDate() != null) {
            if (toLocalDate.get().isBefore(promotion.getFromDate())) {
                throw new IllegalArgumentException(
                        "toDate must be after existing fromDate: " + promotion.getFromDate());
            }
        }

        name.ifPresent(promotion::setName);
        description.ifPresent(promotion::setDescription);
        fromLocalDate.ifPresent(promotion::setFromDate);
        toLocalDate.ifPresent(promotion::setToDate);
        expression.ifPresent(promotion::setExpression);
        base64Image.ifPresent(promotion::setBase64Image);
        return promotion;
    }
}
