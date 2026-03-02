package ar.uba.fi.ingsoft1.product_example.items.products;

import ar.uba.fi.ingsoft1.product_example.items.Component;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.LongFunction;

@Schema(description = "DTO para actualizar un producto. Todos los campos son opcionales. Solo se actualizan los campos enviados.", example = "{\"name\":\"Espaguetti\",\"description\":\"Espagueti con tomate\",\"price\":100,\"categories\":[\"Comida\"],\"type\":\"Vegetariano\",\"estimatedTime\":1,\"addIngredients\":{\"12\":4}}")
public record ProductUpdateDTO(
        @Schema(description = "Nuevo nombre del producto", example = "Espaguetti") Optional<@Size(min = 3, max = 100) String> name,
        @Schema(description = "Nueva descripción del producto", example = "Espagueti con tomate") Optional<@Size(min = 10, max = 500) String> description,
        @Schema(description = "Nuevo precio del producto", example = "100") Optional<@Min(1) Float> price,
        @Schema(description = "Nueva lista de categorías", example = "[\"Comida\"]") Optional<List<String>> categories,
        @Schema(description = "Nuevo tipo del producto", example = "Vegetariano") Optional<String> type,
        @Schema(description = "Nuevo tiempo estimado de preparación en minutos", example = "1") Optional<@Min(1) Integer> estimatedTime,
        @Schema(description = "Mapa de ingredientes a agregar o actualizar: ingredientId a cantidad", example = "{\"12\": 4}") Optional<Map<Long, Integer>> addIngredients,
        @Schema(description = "Lista de IDs de ingredientes a eliminar", example = "[3, 4]") Optional<List<Long>> deleteIngredients,
        @Schema(description = "Nueva imagen del producto en base64", example = "data:image/png;base64,iVBORw0KG...") Optional<String> base64Image) {
    public Product applyTo(Product product, LongFunction<Component> getIngredient) {
        name.ifPresent(product::setName);
        description.ifPresent(product::setDescription);
        price.ifPresent(product::setPrice);
        categories.ifPresent(product::setCategories);
        type.ifPresent(product::setType);
        estimatedTime.ifPresent(product::setEstimatedTime);
        addIngredients(product, getIngredient);
        deleteIngredients(product, getIngredient);
        base64Image.ifPresent(product::setBase64Image);
        return product;
    }

    private void addIngredients(Product product, LongFunction<Component> getIngredient) {
        if (addIngredients.isEmpty())
            return;
        Map<Long, Integer> map = addIngredients.get();
        for (Map.Entry<Long, Integer> entry : map.entrySet()) {
            Component component = getIngredient.apply(entry.getKey());
            if (component == null)
                continue;
            product.add(component, entry.getValue());
        }
    }

    private void deleteIngredients(Product product, LongFunction<Component> getIngredient) {
        for (Long id : deleteIngredients.orElse(List.of())) {
            Component component = getIngredient.apply(id);
            if (component != null)
                product.remove(component);
        }
    }
}
