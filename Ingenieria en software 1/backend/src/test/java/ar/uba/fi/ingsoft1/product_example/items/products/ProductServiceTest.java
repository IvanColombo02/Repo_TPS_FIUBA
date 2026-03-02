package ar.uba.fi.ingsoft1.product_example.items.products;

import ar.uba.fi.ingsoft1.product_example.items.ComponentSpecification;
import ar.uba.fi.ingsoft1.product_example.items.ingredients.Ingredient;
import ar.uba.fi.ingsoft1.product_example.items.ingredients.IngredientRepository;
import ar.uba.fi.ingsoft1.product_example.items.combos.ComboRepository;
import ar.uba.fi.ingsoft1.product_example.order.OrderRepository;
import ar.uba.fi.ingsoft1.product_example.promotions.PromotionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static ar.uba.fi.ingsoft1.product_example.items.ProductConstants.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

class ProductServiceTest {
    private IngredientRepository ingredientRepository;
    private ProductRepository productRepository;
    private ComboRepository comboRepository;
    private OrderRepository orderRepository;
    private PromotionRepository promotionRepository;

    private ProductService productService;

    private final List<Ingredient> INGREDIENTS = new ArrayList<>();
    private Product product1;

    @BeforeEach
    void setup() {
        productRepository = mock();
        ingredientRepository = mock();
        comboRepository = mock();

        INGREDIENTS.clear();
        INGREDIENTS.add(new Ingredient(INGREDIENT_ID, INGREDIENT_NAME, STOCK, IMAGE64));

        var ingredient1 = INGREDIENTS.getFirst();
        when(ingredientRepository.findById(INGREDIENT_ID)).thenReturn(Optional.of(ingredient1));
        when(ingredientRepository.getReferenceById(INGREDIENT_ID)).thenReturn(ingredient1);
        when(productRepository.save(any(Product.class))).thenAnswer(inv -> {
            Product product = inv.getArgument(0);
            product.setId(ID);
            return product;
        });
        product1 = new Product(ID, NAME, DESCRIPTION, PRICE,
                List.of(CATEGORY),TYPE,ESTIMATED_TIME,Map.of(INGREDIENTS.getFirst(),1), IMAGE64);
        when(productRepository.findById(ID)).thenReturn(Optional.of(product1));
        when(productRepository.existsById(ID)).thenReturn(true);
        when(productRepository.existsById(2L)).thenReturn(false);
        comboRepository = mock();
        when(comboRepository.countCombosWhereOnlyProductIs(anyLong())).thenReturn(0L);
        orderRepository = mock();
        when(orderRepository.existsActiveOrdersContainingComponent(anyLong(), any())).thenReturn(false);
        when(orderRepository.removeComponentFromFinalizedOrders(anyLong(), any())).thenReturn(0);
        promotionRepository = mock();
        when(promotionRepository.existsActivePromotionsReferencingComponent(any(), any())).thenReturn(false);
        productService = new ProductService(productRepository, ingredientRepository, comboRepository, orderRepository,
                promotionRepository);
    }

    @Test
    void getProductByIdReturnsDTO() {
        var result = productService.getProductById(ID);
        assertEquals(new ProductDTO(product1), result.get());
    }

    @Test
    void getProductByIdReturnsEmptyIfAbsent() {
        when(productRepository.findById(1111L)).thenReturn(Optional.empty());
        var result = productService.getProductById(1111L);
        assertTrue(result.isEmpty());
    }

    @Test
    void createWritesToDatabase() {
        var newProduct = new ProductCreateDTO(NAME, DESCRIPTION, PRICE, List.of(CATEGORY), TYPE, ESTIMATED_TIME,
                Map.of(INGREDIENT_ID, 1), IMAGE64);
        productService.createProduct(newProduct);
        verify(productRepository).save(any(Product.class));
    }

    @Test
    void createReturnsCreatedProduct() {
        var newProduct = new ProductCreateDTO(NAME, DESCRIPTION, PRICE, List.of(CATEGORY), TYPE, ESTIMATED_TIME,
                Map.of(INGREDIENT_ID, 1), IMAGE64);
        var response = productService.createProduct(newProduct);
        assertNotNull(response);
        assertEquals(NAME, response.name());
    }

    @Test
    void createProductWithDuplicateNameReturnsNull() {
        when(productRepository.findAll(any(Specification.class))).thenReturn(List.of(product1));
        var newProduct = new ProductCreateDTO(NAME, DESCRIPTION, PRICE, List.of(CATEGORY), TYPE, ESTIMATED_TIME,
                Map.of(INGREDIENT_ID, 1), IMAGE64);
        var response = productService.createProduct(newProduct);
        assertNull(response);
    }

    @Test
    void createHandlesNonExistsProducts(){
        var dto = new ProductCreateDTO(NAME, DESCRIPTION, PRICE, List.of(CATEGORY), TYPE, ESTIMATED_TIME,
                Map.of(13213213213213L, 1), IMAGE64);
        assertThrows(NullPointerException.class, () -> {
            productService.createProduct(dto);
        });
    }
    @Test
    void updateProductNameReturnsUpdatedProduct() {
        String newName = "New Name";
        var update = new ProductUpdateDTO(Optional.of(newName),
                Optional.empty(),
                Optional.empty(),
                Optional.empty(),
                Optional.empty(),
                Optional.empty(),
                Optional.empty(),
                Optional.empty(),
                Optional.empty());
        var result = productService.updateProduct(ID, update);
        assertEquals(newName, result.get().name());
    }

    @Test
    void updateProductDescriptionReturnsUpdatedProduct() {
        String newDescription = "New Description";
        var update = new ProductUpdateDTO(Optional.empty(),
                Optional.of(newDescription),
                Optional.empty(),
                Optional.empty(),
                Optional.empty(),
                Optional.empty(),
                Optional.empty(),
                Optional.empty(),
                Optional.empty());
        var result = productService.updateProduct(ID, update);
        assertEquals(newDescription, result.get().description());
    }

    @Test
    void updateProductPriceReturnsUpdatedProduct() {
        float newPrice = 20.0f;
        var update = new ProductUpdateDTO(Optional.empty(),
                Optional.empty(), Optional.of(newPrice),
                Optional.empty(), Optional.empty(),
                Optional.empty(),
                Optional.empty(),
                Optional.empty(), Optional.empty());
        var result = productService.updateProduct(ID, update);
        assertEquals(newPrice, result.get().price());
    }

    @Test
    void updateProductCategoriesReturnsUpdatedProduct() {
        List<String> newCategories = List.of("New Category");
        var update = new ProductUpdateDTO(Optional.empty(),
                Optional.empty(), Optional.empty(),
                Optional.of(newCategories), Optional.empty(),
                Optional.empty(), Optional.empty(),
                Optional.empty(), Optional.empty());
        var result = productService.updateProduct(ID, update);
        assertEquals(newCategories, result.get().categories());
    }

    @Test
    void updateProductTypeReturnsUpdatedProduct() {
        String newType = "New Type";
        var update = new ProductUpdateDTO(Optional.empty(),
                Optional.empty(), Optional.empty(),
                Optional.empty(), Optional.of(newType),
                Optional.empty(), Optional.empty(),
                Optional.empty(), Optional.empty());
        var result = productService.updateProduct(ID, update);
        assertEquals(newType, result.get().type());
    }

    @Test
    void updateProductEstimatedTimeReturnsUpdatedProduct() {
        int newTime = 45;
        var update = new ProductUpdateDTO(Optional.empty(),
                Optional.empty(), Optional.empty(),
                Optional.empty(), Optional.empty(),
                Optional.of(newTime), Optional.empty(),
                Optional.empty(), Optional.empty());
        var result = productService.updateProduct(ID, update);
        assertEquals(newTime, result.get().estimatedTime());
    }

    @Test
    void updateProductAddIngredientsReturnsUpdatedProduct() {
        var update = new ProductUpdateDTO(Optional.empty(),
                Optional.empty(), Optional.empty(),
                Optional.empty(), Optional.empty(),
                Optional.empty(), Optional.of(Map.of(2L, 2)),
                Optional.empty(), Optional.empty());
        var ingredient2 = new Ingredient(2L, "Ingredient 2", 100, "img");
        when(ingredientRepository.getReferenceById(2L)).thenReturn(ingredient2);
        var result = productService.updateProduct(ID, update);
        assertEquals(2, result.get().ingredients().size());
    }

    @Test
    void updateProductDeleteIngredientsReturnsUpdatedProduct() {
        var update = new ProductUpdateDTO(Optional.empty(),
                Optional.empty(), Optional.empty(),
                Optional.empty(), Optional.empty(),
                Optional.empty(), Optional.empty(),
                Optional.of(List.of(INGREDIENT_ID)), Optional.empty());
        var result = productService.updateProduct(ID, update);
        assertTrue(result.get().ingredients().isEmpty());
    }

    @Test
    void updateProductImageReturnsUpdatedProduct() {
        String newImage = "newImage";
        var update = new ProductUpdateDTO(Optional.empty(),
                Optional.empty(), Optional.empty(),
                Optional.empty(), Optional.empty(),
                Optional.empty(), Optional.empty(),
                Optional.empty(), Optional.of(newImage));
        var result = productService.updateProduct(ID, update);
        assertEquals(newImage, result.get().base64Image());
    }

    @Test
    void updateStockUpdatesAddAndReturnsDTO() {
        product1.recalculateStock();
        var dto = new ProductStockDTO(Optional.of(1));
        var result = productService.updateStock(ID, dto);
        product1.recalculateStock();
        assertEquals(product1.getStock(), result.get().stock());
        assertEquals(101, INGREDIENTS.getFirst().getStock());
    }

    @Test
    void updateStockUpdatesReduceAndReturnsDTO() {
        product1.recalculateStock();
        var dto = new ProductStockDTO(Optional.of(-1));
        var result = productService.updateStock(ID, dto);
        product1.recalculateStock();
        assertEquals(product1.getStock(), result.get().stock());
        assertEquals(99, INGREDIENTS.getFirst().getStock());
    }

    @Test
    void updateStockUpdatesNoneAndReturnsEmpty() {
        var dto = new ProductStockDTO(Optional.of(0));
        var result = productService.updateStock(ID, dto);
        assertTrue(result.isEmpty());
        assertEquals(100, INGREDIENTS.getFirst().getStock());
    }

    @Test
    void updateStockFailsIfProductStockIsInsufficient() {
        product1.recalculateStock();
        var initialStock = product1.getStock();
        var dto = new ProductStockDTO(Optional.of(-100000));
        var result = productService.updateStock(ID, dto);
        product1.recalculateStock();
        assertTrue(result.isEmpty());
        assertEquals(initialStock, product1.getStock());
    }
    @Test
    void deleteProductById() {
        boolean result = productService.deleteProductById(ID);
        assertTrue(result);
        verify(productRepository).deleteById(ID);
    }

    @Test
    void cantDeleteAbsentProduct() {
        boolean result = productService.deleteProductById(2L);
        assertFalse(result);
    }
}