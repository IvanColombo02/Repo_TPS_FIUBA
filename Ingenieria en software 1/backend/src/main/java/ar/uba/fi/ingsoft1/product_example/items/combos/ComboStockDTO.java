package ar.uba.fi.ingsoft1.product_example.items.combos;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;

import java.util.Optional;

import static ar.uba.fi.ingsoft1.product_example.items.combos.ComboConstants.MIN_COMBO_QUANTITY;

@Schema(description = "DTO para actualizar el stock de un combo. Un valor positivo incrementa el stock, un valor negativo lo reduce.")
public record ComboStockDTO(
        @Schema(description = "Cantidad a agregar (positivo) o quitar (negativo) del stock", example = "25") Optional<@Min(MIN_COMBO_QUANTITY) Integer> stock) {
    public Optional<Combo> applyTo(Combo combo) {
        int newStock = this.stock.orElse(MIN_COMBO_QUANTITY);
        if (newStock == MIN_COMBO_QUANTITY)
            return Optional.empty();
        boolean updated;
        if (newStock > MIN_COMBO_QUANTITY)
            updated = combo.addStock(newStock);
        else
            updated = combo.reduceStock(-newStock);
        return updated ? Optional.of(combo) : Optional.empty();
    }
}