package ar.uba.fi.ingsoft1.product_example.items.products;

import ar.uba.fi.ingsoft1.product_example.items.ingredients.Ingredient;
import ar.uba.fi.ingsoft1.product_example.items.ingredients.IngredientDTO;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Schema(description = "DTO con la información de un producto pero más simple")
public record ProductSimpleDTO(
        @Schema(description = "ID del producto", example = "1") long id,
        @Schema(description = "Nombre del producto", example = "Hamburguesa Completa") String name,
        @Schema(description = "Descripción del producto", example = "Hamburguesa con carne, lechuga, tomate y queso") String description,
        @Schema(description = "Precio del producto", example = "2500.0") float price,
        @Schema(description = "Stock disponible", example = "100") int stock,
        @Schema(description = "Lista de categorías", example = "[\"MAIN_COURSE\", \"FAST_FOOD\"]") List<String> categories,
        @Schema(description = "Tipo del producto", example = "FOOD") String type,
        @Schema(description = "Tiempo estimado de preparación en minutos", example = "15") int estimatedTime,
        @Schema(description = "Imagen del producto en base64", example = "data:image/png;base64,iVBORw0KG...") String base64Image) {
    public ProductSimpleDTO(Product product) {
        this(product.getId(),
                product.getName(),
                product.getDescription(),
                product.getPrice(),
                product.getStock(),
                product.getCategories(),
                product.getType(),
                product.getEstimatedTime(),
                product.getBase64Image());
    }
}
