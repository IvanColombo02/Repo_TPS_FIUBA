package ar.uba.fi.ingsoft1.product_example.items.combos;

import ar.uba.fi.ingsoft1.product_example.items.Component;
import ar.uba.fi.ingsoft1.product_example.items.products.Product;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.NonNull;
import org.springframework.validation.annotation.Validated;

import java.util.List;
import java.util.Map;
import java.util.function.LongFunction;
import java.util.stream.Collectors;

@Schema(description = "DTO para crear un nuevo combo", example = "{\"name\":\"Combo Clásico\",\"description\":\"Hamburguesa con papas y gaseosa\",\"price\":3500,\"categories\":[\"MAIN_COURSE\"],\"types\":[\"FOOD\"],\"productsIds\":{\"26\":1,\"23\":1},\"base64Image\":\"data:image/png;base64,iVBORw0KG...\"}")
public record ComboCreateDTO(
        @Schema(description = "Nombre del combo", example = "Combo Clásico", requiredMode = Schema.RequiredMode.REQUIRED) @Size(min = 3, max = 100) String name,
        @Schema(description = "Descripción del combo", example = "Hamburguesa con papas y gaseosa", requiredMode = Schema.RequiredMode.REQUIRED) @Size(min = 10, max = 500) String description,
        @Schema(description = "Precio del combo", example = "3500", requiredMode = Schema.RequiredMode.REQUIRED) @Min(1) float price,
        @Schema(description = "Lista de categorías del combo", example = "[\"MAIN_COURSE\"]") List<String> categories,
        @Schema(description = "Lista de tipos del combo", example = "[\"FOOD\"]") List<String> types,
        @Schema(description = "Mapa de productos: productId a cantidad", example = "{\"26\": 1, \"23\": 1}", requiredMode = Schema.RequiredMode.REQUIRED) @NotEmpty Map<Long, Integer> productsIds,
        @Schema(description = "Imagen del combo en base64", example = "data:image/png;base64,iVBORw0KG...") String base64Image) {
    public Combo asCombo(LongFunction<Component> getProduct) {
        Map<Component, Integer> products = productsIds.entrySet().stream().collect(
                Collectors.toMap(product -> getProduct.apply(product.getKey()),
                        Map.Entry::getValue));
        return new Combo(name, description, price, categories, types, products, base64Image);
    }
}
