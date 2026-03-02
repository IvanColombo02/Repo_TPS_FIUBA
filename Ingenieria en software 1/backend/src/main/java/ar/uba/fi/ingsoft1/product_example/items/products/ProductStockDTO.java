package ar.uba.fi.ingsoft1.product_example.items.products;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;

import java.util.Optional;

import static ar.uba.fi.ingsoft1.product_example.items.products.ProductConstants.MIN_STOCK_QUANTITY;

@Schema(description = "DTO para actualizar el stock de un producto. Un valor positivo incrementa el stock, un valor negativo lo reduce.")
public record ProductStockDTO(
        @Schema(description = "Cantidad de stock", example = "10") Optional<@Min(MIN_STOCK_QUANTITY) Integer> stock) {
    public Optional<Product> applyTo(Product product) {
        int newStock = this.stock.orElse(0);
        if (newStock == MIN_STOCK_QUANTITY)
            return Optional.empty();
        boolean updated;
        if (newStock > MIN_STOCK_QUANTITY)
            updated = product.addStock(newStock);
        else
            updated = product.reduceStock(-newStock);
        return updated ? Optional.of(product) : Optional.empty();
    }
}