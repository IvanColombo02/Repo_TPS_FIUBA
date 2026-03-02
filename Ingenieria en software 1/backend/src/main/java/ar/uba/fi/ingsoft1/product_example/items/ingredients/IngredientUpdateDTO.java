package ar.uba.fi.ingsoft1.product_example.items.ingredients;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;

import java.util.Optional;

@Schema(description = "DTO para actualizar un ingrediente. ")
public record IngredientUpdateDTO(
        @Schema(description = "Nuevo nombre del ingrediente", example = "Pan Integral") Optional<@Size(min = 3, max = 100) String> name,
        @Schema(description = "Nuevo stock del ingrediente", example = "150") Optional<@Min(0) Integer> stock,
        @Schema(description = "Nueva imagen del ingrediente en base64", example = "data:image/png;base64,iVBORw0KG...") Optional<String> base64Image) {
    public Ingredient applyTo(Ingredient ingredient) {
        name.ifPresent(ingredient::setName);
        stock.ifPresent(ingredient::setStock);
        base64Image.ifPresent(ingredient::setBase64Image);
        return ingredient;
    }
}
