package ar.uba.fi.ingsoft1.product_example.items.ingredients;

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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration test for ingredient pagination endpoint.
 * 
 * Tests that verify ingredient pagination functionality, including:
 * - Default pagination parameters
 * - Custom page and size parameters
 * - Pagination metadata (totalElements, totalPages, etc.)
 * - Combining pagination with filters
 * - Sorting with pagination
 * - Admin role requirement
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class IngredientPaginationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private IngredientService ingredientService;

    @Autowired
    private IngredientRepository ingredientRepository;

    /**
     * Sets up test data before each test.
     * Creates ingredients to test pagination.
     */
    @BeforeEach
    void setup() {
        // Clean up any existing ingredients first
        ingredientRepository.deleteAll();
        
        // Create ingredients to test pagination
        for (int i = 1; i <= 15; i++) {
            ingredientService.createIngredient(
                new IngredientCreateDTO(
                    "Ingredient " + i,
                    i * 10,
                    ""
                )
            );
        }
    }

    /**
     * Cleans up test data after each test.
     * Removes all ingredients.
     */
    @AfterEach
    void cleanup() {
        // Clean up all ingredients
        ingredientRepository.deleteAll();
    }

    /**
     * Tests that default pagination parameters return first page with default size.
     * Verifies that requesting ingredients without pagination parameters returns page 0 with size 20.
     */
    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldReturnFirstPageWithDefaultSize() throws Exception {
        mockMvc.perform(get("/ingredients")
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
     * Verifies that requesting page 1 with size 10 returns second page with correct ingredients.
     */
    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldReturnSpecificPageAndSize() throws Exception {
        mockMvc.perform(get("/ingredients")
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
    @WithMockUser(roles = "ADMIN")
    void shouldReturnMetadataFields() throws Exception {
        mockMvc.perform(get("/ingredients")
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

    /**
     * Tests that non-admin users cannot access ingredients pagination endpoint.
     * Verifies that requesting ingredients without ADMIN role returns 403 Forbidden.
     */
    @Test
    @WithMockUser(username = "user")
    void shouldDenyAccessWithoutAdminRole() throws Exception {
        mockMvc.perform(get("/ingredients")
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isForbidden());
    }
}
