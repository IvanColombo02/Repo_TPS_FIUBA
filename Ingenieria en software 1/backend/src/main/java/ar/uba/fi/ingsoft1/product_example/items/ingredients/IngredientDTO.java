package ar.uba.fi.ingsoft1.product_example.items.ingredients;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "DTO con la información de un ingrediente")
public record IngredientDTO(
        @Schema(description = "ID del ingrediente", example = "1") long id,
        @Schema(description = "Nombre del ingrediente", example = "Pan de Hamburguesa") String name,
        @Schema(description = "Stock disponible", example = "100") int stock,
        @Schema(description = "Imagen del ingrediente en base64", example = "data:image/png;base64,iVBORw0KG...") String base64Image) {
    public IngredientDTO(Ingredient ingredient) {
        this(ingredient.getId(), ingredient.getName(), ingredient.getStock(), ingredient.getBase64Image());
    }
}
