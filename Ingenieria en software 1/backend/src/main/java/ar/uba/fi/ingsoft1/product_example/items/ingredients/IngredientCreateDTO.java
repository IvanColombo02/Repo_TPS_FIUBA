package ar.uba.fi.ingsoft1.product_example.items.ingredients;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.NonNull;
import org.springframework.validation.annotation.Validated;

@Schema(description = "DTO para crear un nuevo ingrediente")
public record IngredientCreateDTO(
        @Schema(description = "Nombre del ingrediente", example = "Pan de Hamburguesa", requiredMode = Schema.RequiredMode.REQUIRED) @NotBlank @Size(min = 3, max = 100) String name,
        @Schema(description = "Stock inicial del ingrediente", example = "100", requiredMode = Schema.RequiredMode.REQUIRED) @Min(0) int stock,
        @Schema(description = "Imagen del ingrediente en base64", example = "data:image/png;base64,iVBORw0KG...") String base64Image) {
    public Ingredient asIngredient() {
        return new Ingredient(name, stock, base64Image);
    }
}
