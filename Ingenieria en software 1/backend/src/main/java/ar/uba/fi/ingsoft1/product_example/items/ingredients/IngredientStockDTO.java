package ar.uba.fi.ingsoft1.product_example.items.ingredients;

import ar.uba.fi.ingsoft1.product_example.items.products.ProductConstants;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;

import java.util.Optional;

import static ar.uba.fi.ingsoft1.product_example.items.ingredients.IngredientConstants.MIN_STOCK_QUANTITY;

@Schema(description = "DTO para actualizar el stock de un ingrediente. Un valor positivo incrementa el stock, un valor negativo lo reduce.")
public record IngredientStockDTO(
        @Schema(description = "Cantidad a agregar (positivo) o quitar (negativo) del stock", example = "50") Optional<@Min(MIN_STOCK_QUANTITY) Integer> stock) {
    public Optional<Ingredient> applyTo(Ingredient ingredient) {
        int newStock = this.stock.orElse(MIN_STOCK_QUANTITY);
        if (newStock == MIN_STOCK_QUANTITY)
            return Optional.empty();
        boolean updated;
        if (newStock > MIN_STOCK_QUANTITY)
            updated = ingredient.addStock(newStock);
        else
            updated = ingredient.reduceStock(-newStock);
        return updated ? Optional.of(ingredient) : Optional.empty();
    }
}