package ar.uba.fi.ingsoft1.product_example.items.combos;

import ar.uba.fi.ingsoft1.product_example.items.Component;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.LongFunction;

import static ar.uba.fi.ingsoft1.product_example.items.combos.ComboConstants.MIN_COMBO_QUANTITY;

@Schema(description = "DTO para actualizar un combo. Todos los campos son opcionales. Solo se actualizan los campos enviados.", example = "{\"name\":\"Combo Premium\",\"description\":\"Hamburguesa doble con papas y gaseosa\",\"price\":4500,\"categories\":[\"MAIN_COURSE\"],\"types\":[\"FOOD\"],\"base64Image\":\"data:image/png;base64,iVBORw0KG...\"}")
public record ComboUpdateDTO(
        @Schema(description = "Nuevo nombre del combo", example = "Combo Premium") Optional<@Size(min = 3, max = 100) String> name,
        @Schema(description = "Nueva descripción del combo", example = "Hamburguesa doble con papas y gaseosa") Optional<@Size(min = 10, max = 500) String> description,
        @Schema(description = "Nuevo precio del combo", example = "4500") Optional<@Min(1) Float> price,
        @Schema(description = "Nueva lista de categorías", example = "[\"MAIN_COURSE\"]") Optional<List<String>> categories,
        @Schema(description = "Nueva lista de tipos", example = "[\"FOOD\"]") Optional<List<String>> types,
        @Schema(description = "Mapa de productos a agregar o actualizar: productId a cantidad", example = "{\"1\": 2, \"2\": 1}") Optional<Map<Long, Integer>> addProducts,
        @Schema(description = "Lista de IDs de productos a eliminar", example = "[3]") Optional<List<Long>> deleteProducts,
        @Schema(description = "Nueva imagen del combo en base64", example = "data:image/png;base64,iVBORw0KG...") Optional<String> base64Image) {
    public Combo applyTo(Combo combo, LongFunction<Component> getCombo) {
        name.ifPresent(combo::setName);
        description.ifPresent(combo::setDescription);
        price.ifPresent(combo::setPrice);
        Map<Component, Integer> products = combo.getProducts();
        removeProducts(products);
        addOrUpdateProducts(products, getCombo);

        types.ifPresent(combo::setTypes);
        categories.ifPresent(combo::setCategories);
        base64Image.ifPresent(combo::setBase64Image);
        return combo;
    }

    private void addOrUpdateProducts(Map<Component, Integer> products, LongFunction<Component> getCombo) {
        if (addProducts.isEmpty())
            return;

        for (Map.Entry<Long, Integer> entry : addProducts.get().entrySet()) {
            Long productId = entry.getKey();
            Integer quantity = entry.getValue();

            if (quantity == null || quantity <= MIN_COMBO_QUANTITY)
                continue;

            Map.Entry<Component, Integer> existingEntry = products.entrySet().stream()
                    .filter(e -> e.getKey().getId().equals(productId))
                    .findFirst()
                    .orElse(null);

            if (existingEntry != null) {

                if (!quantity.equals(existingEntry.getValue())) {
                    existingEntry.setValue(quantity);
                }
                continue;
            }

            Component component = getCombo.apply(productId);
            if (component == null)
                continue;

            products.put(component, quantity);
        }
    }

    private void removeProducts(Map<Component, Integer> products) {
        if (deleteProducts.isEmpty())
            return;

        var idsToRemove = deleteProducts.get();
        products.entrySet().removeIf(entry -> idsToRemove.contains(entry.getKey().getId()));
    }
}
