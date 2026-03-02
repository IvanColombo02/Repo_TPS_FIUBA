package ar.uba.fi.ingsoft1.product_example.items.products;

import ar.uba.fi.ingsoft1.product_example.items.Component;
import ar.uba.fi.ingsoft1.product_example.items.ingredients.Ingredient;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.NonNull;
import org.springframework.validation.annotation.Validated;

import java.util.List;
import java.util.Map;
import java.util.function.LongFunction;
import java.util.stream.Collectors;

@Schema(description = "DTO para crear un nuevo producto", example = "{\"name\":\"Hamburguesa Completa\",\"description\":\"Hamburguesa con carne, lechuga, tomate y queso\",\"price\":2500,\"categories\":[\"MAIN_COURSE\",\"FAST_FOOD\"],\"type\":\"FOOD\",\"estimatedTime\":15,\"ingredientsIds\":{\"1\":2,\"2\":1,\"3\":1}}")
public record ProductCreateDTO(
        @Schema(description = "Nombre del producto", example = "Hamburguesa Completa", requiredMode = Schema.RequiredMode.REQUIRED) @Size(min = 3, max = 100) String name,
        @Schema(description = "Descripción del producto", example = "Hamburguesa con carne, lechuga, tomate y queso", requiredMode = Schema.RequiredMode.REQUIRED) @Size(min = 10, max = 500) String description,
        @Schema(description = "Precio del producto", example = "2500", requiredMode = Schema.RequiredMode.REQUIRED) @Min(1) float price,
        @Schema(description = "Lista de categorías del producto", example = "[\"MAIN_COURSE\", \"FAST_FOOD\"]", requiredMode = Schema.RequiredMode.REQUIRED) @NotEmpty List<String> categories,
        @Schema(description = "Tipo del producto", example = "FOOD", requiredMode = Schema.RequiredMode.REQUIRED) @NotEmpty String type,
        @Schema(description = "Tiempo estimado de preparación en minutos", example = "15", requiredMode = Schema.RequiredMode.REQUIRED) @Min(0) @Max(400) Integer estimatedTime,
        @Schema(description = "Mapa de ingredientes: ingredientId a cantidad necesaria", example = "{\"1\": 2, \"2\": 1, \"3\": 1}", requiredMode = Schema.RequiredMode.REQUIRED) @NotEmpty Map<Long, Integer> ingredientsIds,
        @Schema(description = "Imagen del producto en base64", example = "data:image/png;base64,iVBORw0KG...") String base64Image) {
    public Product asProduct(LongFunction<Ingredient> getIngredient) {
        Map<Ingredient, Integer> ingredients = ingredientsIds.entrySet().stream().collect(
                Collectors.toMap(ingredient -> getIngredient.apply(ingredient.getKey()),
                        Map.Entry::getValue));
        return new Product(name, description, price, categories, type, estimatedTime, ingredients, base64Image);
    }
}
