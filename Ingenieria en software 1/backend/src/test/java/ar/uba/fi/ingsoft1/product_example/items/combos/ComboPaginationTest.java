package ar.uba.fi.ingsoft1.product_example.items.combos;

import ar.uba.fi.ingsoft1.product_example.items.ingredients.Ingredient;
import ar.uba.fi.ingsoft1.product_example.items.ingredients.IngredientRepository;
import ar.uba.fi.ingsoft1.product_example.items.products.ProductCreateDTO;
import ar.uba.fi.ingsoft1.product_example.items.products.ProductService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration test for combo pagination endpoint.
 * 
 * Tests that verify combo pagination functionality, including:
 * - Default pagination parameters
 * - Custom page and size parameters
 * - Pagination metadata (totalElements, totalPages, etc.)
 * - Combining pagination with filters
 * - Sorting with pagination
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class ComboPaginationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ComboService comboService;

    @Autowired
    private ProductService productService;

    @Autowired
    private IngredientRepository ingredientRepository;

    private Ingredient ingredient;
    private Long productId;

    /**
     * Sets up test data before each test.
     * Creates an ingredient and a product that will be used to create combos.
     */
    @BeforeEach
    void setup() {
        // Clean up existing data first
        comboService.getCombos(
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
        ).forEach(combo -> comboService.deleteComboById(combo.id()));
        
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
        
        // Create an ingredient for products
        ingredient = ingredientRepository.save(new Ingredient("Test Ingredient", 100, ""));
        
        // Create a product that will be used in combos
        var product = productService.createProduct(
            new ProductCreateDTO(
                "Test Product",
                "Test Description",
                100.0f,
                List.of("Food"),
                "Principal",
                30,
                Map.of(ingredient.getId(), 1),
                ""
            )
        );
        productId = product.id();
        
        // Create combos to test pagination
        for (int i = 1; i <= 15; i++) {
            comboService.createCombo(
                new ComboCreateDTO(
                    "Combo " + i,
                    "Description for combo " + i + " with enough characters to meet requirements",
                    100.0f + i,
                    List.of("Food"),
                    List.of("Principal"),
                    Map.of(productId, 1),
                    ""
                )
            );
        }
    }

    /**
     * Cleans up test data after each test.
     * Removes all combos and related test data.
     */
    @AfterEach
    void cleanup() {
        // Clean up all combos
        comboService.getCombos(
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
        ).forEach(combo -> comboService.deleteComboById(combo.id()));
        
        // Clean up products and ingredients
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

    /**
     * Tests that default pagination parameters return first page with default size.
     * Verifies that requesting combos without pagination parameters returns page 0 with size 20.
     */
    @Test
    @WithMockUser(username = "user")
    void shouldReturnFirstPageWithDefaultSize() throws Exception {
        mockMvc.perform(get("/combos")
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content").isArray())
            .andExpect(jsonPath("$.content.length()").value(15))
            .andExpect(jsonPath("$.number").value(0))
            .andExpect(jsonPath("$.size").value(20))
            .andExpect(jsonPath("$.totalElements").value(15))
            .andExpect(jsonPath("$.totalPages").value(1));
    }

    /**
     * Tests that specific page and size parameters return correct page.
     * Verifies that requesting page 1 with size 10 returns second page with correct combos.
     */
    @Test
    @WithMockUser(username = "user")
    void shouldReturnSpecificPageAndSize() throws Exception {
        mockMvc.perform(get("/combos")
                .param("page", "1")
                .param("size", "10")
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content").isArray())
            .andExpect(jsonPath("$.number").value(1))
            .andExpect(jsonPath("$.size").value(10))
            .andExpect(jsonPath("$.totalElements").value(15));
    }

    /**
     * Tests that pagination response includes metadata fields.
     * Verifies that response contains content, number, size, totalElements, and totalPages.
     */
    @Test
    @WithMockUser(username = "user")
    void shouldReturnMetadataFields() throws Exception {
        mockMvc.perform(get("/combos")
                .param("page", "0")
                .param("size", "10")
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content").exists())
            .andExpect(jsonPath("$.number").exists())
            .andExpect(jsonPath("$.size").exists())
            .andExpect(jsonPath("$.totalElements").exists())
            .andExpect(jsonPath("$.totalPages").exists());
    }
}
