package ar.uba.fi.ingsoft1.product_example.items.products;

import ar.uba.fi.ingsoft1.product_example.items.ingredients.Ingredient;
import ar.uba.fi.ingsoft1.product_example.items.ingredients.IngredientRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Map;

import static ar.uba.fi.ingsoft1.product_example.items.ProductConstants.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration test for product pagination endpoint.
 * 
 * User Story:
 * As a backend developer
 * I want the products endpoint to support pagination
 * So I can retrieve a specific quantity of products and handle large volumes without issues.
 * 
 * Acceptance Criteria:
 * - Endpoint accepts 'page' and 'size' parameters along with filters
 * - Default values: page=0, size=20 when not specified
 * - Response includes products and metadata (total elements, total pages, current page)
 * - Data is retrieved from the database
 * - Paginated search respects filters
 */
@SpringBootTest
@AutoConfigureMockMvc
class ProductPaginationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ProductService productService;

    @Autowired
    private IngredientRepository ingredientRepository;

    private Ingredient ingredient;

    @BeforeEach
    void setup() {
        ingredient = ingredientRepository.save(new Ingredient(INGREDIENT_NAME, STOCK, IMAGE64));
        
        // Create 35 products to test pagination
        for (int i = 1; i <= 35; i++) {
            productService.createProduct(
                new ProductCreateDTO(
                    "Product " + i,
                    "Description " + i,
                    100.0f + i,
                    List.of(CATEGORY),
                    TYPE,
                    ESTIMATED_TIME,
                    Map.of(ingredient.getId(), 1),
                    IMAGE64
                )
            );
        }
    }

    @AfterEach
    void cleanup() {
        // Clean up all products
        productService.getProducts(
            new ar.uba.fi.ingsoft1.product_example.items.ComponentSearchDTO(
                java.util.Optional.empty(),
                java.util.Optional.empty(),
                java.util.Optional.empty(),
                java.util.Optional.empty(),
                java.util.Optional.empty(),
                java.util.Optional.empty(),
                java.util.Optional.empty()
            ),
            org.springframework.data.domain.Pageable.unpaged()
        ).forEach(product -> productService.deleteProductById(product.id()));
        
        ingredientRepository.deleteAll();
    }

    @Test
    @WithMockUser(username = "user")
    void shouldReturnFirstPageWithDefaultSize() throws Exception {
        // When: Request without pagination parameters
        mockMvc.perform(get("/products")
                .contentType(MediaType.APPLICATION_JSON))
            // Then: Should return first page with default size (20)
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content").isArray())
            .andExpect(jsonPath("$.content.length()").value(20))
            .andExpect(jsonPath("$.number").value(0))  // Current page
            .andExpect(jsonPath("$.size").value(20))   // Page size
            .andExpect(jsonPath("$.totalElements").value(35))  // Total products
            .andExpect(jsonPath("$.totalPages").value(2));     // Total pages (35/20 = 2)
    }

    @Test
    @WithMockUser(username = "user")
    void shouldReturnSpecificPageAndSize() throws Exception {
        // When: Request page 1 with size 10
        mockMvc.perform(get("/products")
                .param("page", "1")
                .param("size", "10")
                .contentType(MediaType.APPLICATION_JSON))
            // Then: Should return second page with 10 items
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content").isArray())
            .andExpect(jsonPath("$.content.length()").value(10))
            .andExpect(jsonPath("$.number").value(1))  // Second page
            .andExpect(jsonPath("$.size").value(10))
            .andExpect(jsonPath("$.totalElements").value(35))
            .andExpect(jsonPath("$.totalPages").value(4));  // 35/10 = 4 pages
    }

    @Test
    @WithMockUser(username = "user")
    void shouldReturnLastPageWithRemainingElements() throws Exception {
        // When: Request last page with size 10
        mockMvc.perform(get("/products")
                .param("page", "3")
                .param("size", "10")
                .contentType(MediaType.APPLICATION_JSON))
            // Then: Should return last page with only 5 remaining items
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content").isArray())
            .andExpect(jsonPath("$.content.length()").value(5))  // Only 5 remaining
            .andExpect(jsonPath("$.number").value(3))
            .andExpect(jsonPath("$.size").value(10))
            .andExpect(jsonPath("$.totalElements").value(35))
            .andExpect(jsonPath("$.totalPages").value(4))
            .andExpect(jsonPath("$.last").value(true));  // Is last page
    }

    @Test
    @WithMockUser(username = "user")
    void shouldReturnEmptyPageWhenPageNumberExceedsTotalPages() throws Exception {
        // When: Request page beyond total pages
        mockMvc.perform(get("/products")
                .param("page", "10")
                .param("size", "20")
                .contentType(MediaType.APPLICATION_JSON))
            // Then: Should return empty page
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content").isArray())
            .andExpect(jsonPath("$.content.length()").value(0))
            .andExpect(jsonPath("$.number").value(10))
            .andExpect(jsonPath("$.totalElements").value(35))
            .andExpect(jsonPath("$.empty").value(true));
    }

    @Test
    @WithMockUser(username = "user")
    void shouldReturnAllMetadataFields() throws Exception {
        // When: Request products with pagination
        mockMvc.perform(get("/products")
                .param("page", "0")
                .param("size", "10")
                .contentType(MediaType.APPLICATION_JSON))
            // Then: Should include all pagination metadata
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content").exists())
            .andExpect(jsonPath("$.pageable").exists())
            .andExpect(jsonPath("$.number").exists())      // Current page
            .andExpect(jsonPath("$.size").exists())        // Page size
            .andExpect(jsonPath("$.totalElements").exists()) // Total products
            .andExpect(jsonPath("$.totalPages").exists())   // Total pages
            .andExpect(jsonPath("$.first").exists())        // Is first page
            .andExpect(jsonPath("$.last").exists())         // Is last page
            .andExpect(jsonPath("$.empty").exists());       // Is empty
    }

    @Test
    @WithMockUser(username = "user")
    void shouldRespectFiltersWithPagination() throws Exception {
        // Given: Create products with specific category
        String specialCategory = "SPECIAL_CATEGORY";
        for (int i = 1; i <= 5; i++) {
            productService.createProduct(
                new ProductCreateDTO(
                    "Special Product " + i,
                    "Special Description " + i,
                    200.0f + i,
                    List.of(specialCategory),
                    TYPE,
                    ESTIMATED_TIME,
                    Map.of(ingredient.getId(), 1),
                    IMAGE64
                )
            );
        }

        // When: Request with category filter and pagination
        mockMvc.perform(get("/products")
                .param("categories", specialCategory)
                .param("page", "0")
                .param("size", "3")
                .contentType(MediaType.APPLICATION_JSON))
            // Then: Should return only filtered products with pagination
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content").isArray())
            .andExpect(jsonPath("$.content.length()").value(3))
            .andExpect(jsonPath("$.totalElements").value(5))  // Only special products
            .andExpect(jsonPath("$.totalPages").value(2))
            .andExpect(jsonPath("$.content[0].categories[0]").value(specialCategory));
    }

    @Test
    @WithMockUser(username = "user")
    void shouldHandleCustomPageSize() throws Exception {
        // When: Request with custom page size of 5
        mockMvc.perform(get("/products")
                .param("page", "0")
                .param("size", "5")
                .contentType(MediaType.APPLICATION_JSON))
            // Then: Should return exactly 5 products
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content.length()").value(5))
            .andExpect(jsonPath("$.size").value(5))
            .andExpect(jsonPath("$.totalElements").value(35))
            .andExpect(jsonPath("$.totalPages").value(7));  // 35/5 = 7 pages
    }

    @Test
    @WithMockUser(username = "user")
    void shouldSortProductsInPagination() throws Exception {
        // When: Request with sorting by name
        mockMvc.perform(get("/products")
                .param("page", "0")
                .param("size", "10")
                .param("sort", "name,asc")
                .contentType(MediaType.APPLICATION_JSON))
            // Then: Should return sorted products
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content").isArray())
            .andExpect(jsonPath("$.content[0].name").value("Product 1"))
            .andExpect(jsonPath("$.sort.sorted").value(true));
    }

    @Test
    @WithMockUser(username = "user")
    void shouldHandlePriceRangeFilterWithPagination() throws Exception {
        // When: Request with price range filter (only from setUp products: 125-129 = products 25-29, total 5)
        mockMvc.perform(get("/products")
                .param("priceMin", "125")
                .param("priceMax", "129")
                .param("page", "0")
                .param("size", "10")
                .contentType(MediaType.APPLICATION_JSON))
            // Then: Should return only products in price range
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content").isArray())
            .andExpect(jsonPath("$.totalElements").value(5));  // Products 25-29
    }

    @Test
    @WithMockUser(username = "user")
    void shouldSortByStockAscending() throws Exception {
        // Given: Create products with different stock levels by varying ingredient stock
        for (int i = 1; i <= 5; i++) {
            // Create individual ingredient with specific stock
            Ingredient testIngredient = ingredientRepository.save(new Ingredient(
                "Ing " + i,
                i * 10,  // Stock: 10, 20, 30, 40, 50
                IMAGE64
            ));
            
            productService.createProduct(
                new ProductCreateDTO(
                    "Stock Product " + i,
                    "Description",
                    100.0f,
                    List.of(CATEGORY),
                    TYPE,
                    ESTIMATED_TIME,
                    Map.of(testIngredient.getId(), 1),  // 1:1 ratio so product stock = ingredient stock
                    IMAGE64
                )
            );
        }

        // When: Request with sort by stock ascending
        mockMvc.perform(get("/products")
                .param("page", "0")
                .param("size", "10")
                .param("sort", "stock,asc")
                .contentType(MediaType.APPLICATION_JSON))
            // Then: Should return products sorted by stock ascending
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content").isArray())
            .andExpect(jsonPath("$.sort.sorted").value(true));
    }

    @Test
    @WithMockUser(username = "user")
    void shouldSortByStockDescending() throws Exception {
        // Given: Create products with different stock levels by varying ingredient stock
        for (int i = 1; i <= 5; i++) {
            // Create individual ingredient with specific stock
            Ingredient testIngredient = ingredientRepository.save(new Ingredient(
                "Ing Desc " + i,
                i * 10,  // Stock: 10, 20, 30, 40, 50
                IMAGE64
            ));
            
            productService.createProduct(
                new ProductCreateDTO(
                    "Stock Product Desc " + i,
                    "Description",
                    100.0f,
                    List.of(CATEGORY),
                    TYPE,
                    ESTIMATED_TIME,
                    Map.of(testIngredient.getId(), 1),  // 1:1 ratio so product stock = ingredient stock
                    IMAGE64
                )
            );
        }

        // When: Request with sort by stock descending
        mockMvc.perform(get("/products")
                .param("page", "0")
                .param("size", "10")
                .param("sort", "stock,desc")
                .contentType(MediaType.APPLICATION_JSON))
            // Then: Should return products sorted by stock descending
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content").isArray())
            .andExpect(jsonPath("$.sort.sorted").value(true));
    }

    @Test
    @WithMockUser(username = "user")
    void shouldCombineStockSortingWithPaginationAndFilters() throws Exception {
        // Given: Create specific products with controlled stock values and unique names
        for (int i = 1; i <= 10; i++) {
            // Create individual ingredient with specific stock
            Ingredient testIngredient = ingredientRepository.save(new Ingredient(
                "UniqueStockFilter Ing " + i,
                i * 5,  // Stock: 5, 10, 15, 20, 25, 30, 35, 40, 45, 50
                IMAGE64
            ));
            
            productService.createProduct(
                new ProductCreateDTO(
                    i % 2 == 0 ? "UniqueStockSpecial " + i : "UniqueStockRegular " + i,  // Unique names
                    "Description",
                    200.0f + i,
                    List.of(CATEGORY),
                    TYPE,
                    ESTIMATED_TIME,
                    Map.of(testIngredient.getId(), 1),  // 1:1 ratio so product stock = ingredient stock
                    IMAGE64
                )
            );
        }

        // When: Filter by "UniqueStockSpecial" name and sort by stock descending
        mockMvc.perform(get("/products")
                .param("name", "UniqueStockSpecial")
                .param("sort", "stock,desc")
                .param("page", "0")
                .param("size", "10")
                .contentType(MediaType.APPLICATION_JSON))
            // Then: Should return UniqueStockSpecial products sorted by stock descending
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content").isArray())
            .andExpect(jsonPath("$.content.length()").value(5))  // 5 "UniqueStockSpecial" products
            .andExpect(jsonPath("$.content[0].stock").value(50))  // Highest stock first
            .andExpect(jsonPath("$.content[4].stock").value(10)); // Lowest stock last
    }
}
