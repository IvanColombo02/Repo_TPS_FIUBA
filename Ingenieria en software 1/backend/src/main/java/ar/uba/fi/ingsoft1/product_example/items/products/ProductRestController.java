package ar.uba.fi.ingsoft1.product_example.items.products;

import ar.uba.fi.ingsoft1.product_example.items.ComponentSearchDTO;
import static ar.uba.fi.ingsoft1.product_example.items.products.ProductConstants.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping(PRODUCTS_ENDPOINT)
@Validated
@RequiredArgsConstructor
@Tag(name = "4 - Products", description = "Endpoints para gestión de productos")
class ProductRestController {
    private final ProductService productService;

    @GetMapping
    @Operation(summary = "Listar productos", description = "Lista productos con paginación y filtros.", parameters = {
            @Parameter(name = "filter", description = "Filtros opcionales para productos", in = ParameterIn.QUERY, required = false, schema = @Schema(implementation = ComponentSearchDTO.class))
    })
    @ApiResponse(responseCode = RESPONSE_OK, description = "Lista de productos obtenida exitosamente")
    @Transactional(readOnly = true)
    public Page<ProductSimpleDTO> getProducts(
            @Parameter(hidden = true) ComponentSearchDTO filter,
            Pageable pageable) {
        return productService.getProducts(filter, pageable);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener producto por ID", description = "Obtiene un producto específico por su ID.")
    @ApiResponse(responseCode = RESPONSE_OK, description = "Producto obtenido exitosamente")
    @ApiResponse(responseCode = RESPONSE_NOT_FOUND, description = "Producto no encontrado")
    public ResponseEntity<ProductDTO> getProductById(@PathVariable long id) {
        return productService.getProductById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND).build());
    }

    @PostMapping
    @Operation(summary = "Crear producto", description = "Crea un nuevo producto. Solo ADMIN. Requiere autenticación.")
    @ApiResponse(responseCode = RESPONSE_CREATED, description = "Producto creado exitosamente")
    @ApiResponse(responseCode = RESPONSE_BAD_REQUEST, description = ERROR_INVALID_DATA)
    @ApiResponse(responseCode = RESPONSE_UNAUTHORIZED, description = ERROR_NOT_AUTHENTICATED)
    @ApiResponse(responseCode = RESPONSE_FORBIDDEN, description = ERROR_UNAUTHORIZED_ADMIN)
    @ResponseStatus(HttpStatus.CREATED)
    public ProductDTO createProduct(
            @Validated @RequestBody ProductCreateDTO data) {
        var product = productService.createProduct(data);
        if (product == null) throw new ResponseStatusException(HttpStatus.CONFLICT,
                ERROR_DUPLICATE_NAME);
        return product;
    }

    @PatchMapping("/{id}")
    @Operation(summary = "Actualizar producto", description = "Actualiza un producto existente. Solo ADMIN. Requiere autenticación.")
    @ApiResponse(responseCode = RESPONSE_OK, description = "Producto actualizado exitosamente")
    @ApiResponse(responseCode = RESPONSE_NOT_MODIFIED, description = "Producto no modificado")
    @ApiResponse(responseCode = RESPONSE_UNAUTHORIZED, description = ERROR_NOT_AUTHENTICATED)
    @ApiResponse(responseCode = RESPONSE_FORBIDDEN, description = ERROR_UNAUTHORIZED_ADMIN)
    public ResponseEntity<ProductDTO> updateProduct(
            @PathVariable Long id,
            @Validated @RequestBody ProductUpdateDTO data) {
        return productService.updateProduct(id, data)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.status(HttpStatus.NOT_MODIFIED).build());
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar producto", description = "Elimina un producto. Solo ADMIN. Requiere autenticación.")
    @ApiResponse(responseCode = RESPONSE_OK, description = "Producto eliminado exitosamente")
    @ApiResponse(responseCode = RESPONSE_NOT_FOUND, description = "Producto no encontrado")
    @ApiResponse(responseCode = RESPONSE_UNAUTHORIZED, description = ERROR_NOT_AUTHENTICATED)
    @ApiResponse(responseCode = RESPONSE_FORBIDDEN, description = ERROR_UNAUTHORIZED_ADMIN)
    public ResponseEntity<Void> deleteProductById(@PathVariable long id) {
        boolean deleted = productService.deleteProductById(id);
        return deleted ? ResponseEntity.ok().build() : ResponseEntity.notFound().build();
    }

    @PatchMapping("/{id}/stock")
    @io.swagger.v3.oas.annotations.Hidden
    public ResponseEntity<ProductDTO> updateStockProduct(
            @PathVariable Long id,
            @Validated @RequestBody ProductStockDTO data) {
        return productService.updateStock(id, data)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.status(HttpStatus.NOT_MODIFIED).build());
    }
}
