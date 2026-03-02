package ar.uba.fi.ingsoft1.product_example.items.ingredients;

import ar.uba.fi.ingsoft1.product_example.config.security.JwtService;
import ar.uba.fi.ingsoft1.product_example.config.security.SecurityConfig;
import ar.uba.fi.ingsoft1.product_example.user.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Optional;

import static ar.uba.fi.ingsoft1.product_example.items.ProductConstants.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = IngredientRestController.class)
@Import({SecurityConfig.class, JwtService.class})
class StockManagementTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private IngredientService ingredientService;

    @MockitoBean
    private UserService userService;

    private IngredientDTO ingredientDTO;
    private Ingredient ingredient;

    @BeforeEach
    void setup() {
        ingredient = new Ingredient(INGREDIENT_ID, INGREDIENT_NAME, STOCK, IMAGE64);
        ingredientDTO = new IngredientDTO(ingredient);
    }

    /**
     * Tests that an admin user can retrieve a list of ingredients with their current stock.
     * Verifies that the endpoint returns a paginated list containing ingredient details including stock.
     */
    @Test
    @WithMockUser(roles = {"ADMIN"})
    void getIngredientsListWithStock() throws Exception {
        Page<IngredientDTO> page = new PageImpl<>(List.of(ingredientDTO), PageRequest.of(0, 10), 1);
        when(ingredientService.getIngredients(any(), any())).thenReturn(page);

        mockMvc.perform(get("/ingredients")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpectAll(
                        status().isOk(),
                        jsonPath("$.content[0].id").value(INGREDIENT_ID),
                        jsonPath("$.content[0].name").value(INGREDIENT_NAME),
                        jsonPath("$.content[0].stock").value(STOCK)
                );
    }

    /**
     * Tests that an admin user can successfully update (increase) the stock of an ingredient.
     * Verifies that the stock is correctly incremented and the updated ingredient is returned.
     */
    @Test
    @WithMockUser(roles = {"ADMIN"})
    void updateIngredientStockSuccessfully() throws Exception {
        IngredientDTO updatedDTO = new IngredientDTO(INGREDIENT_ID, INGREDIENT_NAME, STOCK + 50, IMAGE64);
        when(ingredientService.updateStock(eq(INGREDIENT_ID), any(IngredientStockDTO.class)))
                .thenReturn(Optional.of(updatedDTO));

        mockMvc.perform(patch("/ingredients/" + INGREDIENT_ID + "/stock")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"stock\":50}"))
                .andExpectAll(
                        status().isOk(),
                        jsonPath("$.id").value(INGREDIENT_ID),
                        jsonPath("$.stock").value(STOCK + 50)
                );
    }

    /**
     * Tests that a non-admin user cannot update ingredient stock.
     * Verifies that the endpoint returns 403 Forbidden when accessed without admin role.
     */
    @Test
    @WithMockUser
    void updateStockWithoutAdminPermissionsReturnsForbidden() throws Exception {
        mockMvc.perform(patch("/ingredients/" + INGREDIENT_ID + "/stock")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"stock\":50}"))
                .andExpect(status().isForbidden());
    }

    /**
     * Tests that updating stock with a negative value is handled correctly.
     * The DTO allows negative values to reduce stock, but validation may reject them.
     * If validation passes, the service may return empty, resulting in 304 Not Modified.
     */
    @Test
    @WithMockUser(roles = {"ADMIN"})
    void updateStockWithNegativeValueReturnsNotModified() throws Exception {
        when(ingredientService.updateStock(eq(INGREDIENT_ID), any(IngredientStockDTO.class)))
                .thenReturn(Optional.empty());
        
        mockMvc.perform(patch("/ingredients/" + INGREDIENT_ID + "/stock")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"stock\":-10}"))
                .andExpect(status().isNotModified());
    }

    /**
     * Tests that updating stock with zero value does not modify the ingredient.
     * Verifies that when stock delta is zero, the service returns empty and status is 304 Not Modified.
     */
    @Test
    @WithMockUser(roles = {"ADMIN"})
    void updateStockWithZeroValueDoesNotModify() throws Exception {
        when(ingredientService.updateStock(eq(INGREDIENT_ID), any(IngredientStockDTO.class)))
                .thenReturn(Optional.empty());

        mockMvc.perform(patch("/ingredients/" + INGREDIENT_ID + "/stock")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"stock\":0}"))
                .andExpect(status().isNotModified());
    }

    /**
     * Tests that an admin user can successfully reduce ingredient stock using a negative value.
     * Verifies that the stock is correctly decremented and the updated ingredient is returned.
     */
    @Test
    @WithMockUser(roles = {"ADMIN"})
    void reduceIngredientStockSuccessfully() throws Exception {
        IngredientDTO updatedDTO = new IngredientDTO(INGREDIENT_ID, INGREDIENT_NAME, STOCK - 20, IMAGE64);
        when(ingredientService.updateStock(eq(INGREDIENT_ID), any(IngredientStockDTO.class)))
                .thenReturn(Optional.of(updatedDTO));

        mockMvc.perform(patch("/ingredients/" + INGREDIENT_ID + "/stock")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"stock\":-20}"))
                .andExpectAll(
                        status().isOk(),
                        jsonPath("$.stock").value(STOCK - 20)
                );
    }

    /**
     * Tests that accessing the ingredients list without authentication is denied.
     * Verifies that the endpoint returns 403 Forbidden for unauthenticated requests.
     */
    @Test
    void getIngredientsListWithoutAuthenticationReturnsForbidden() throws Exception {
        mockMvc.perform(get("/ingredients")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    /**
     * Tests that an admin user can retrieve a specific ingredient by ID with its stock information.
     * Verifies that the endpoint returns the correct ingredient details including current stock.
     */
    @Test
    @WithMockUser(roles = {"ADMIN"})
    void getIngredientByIdWithStock() throws Exception {
        when(ingredientService.getIngredientById(eq(INGREDIENT_ID)))
                .thenReturn(Optional.of(ingredientDTO));

        mockMvc.perform(get("/ingredients/" + INGREDIENT_ID)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpectAll(
                        status().isOk(),
                        jsonPath("$.id").value(INGREDIENT_ID),
                        jsonPath("$.name").value(INGREDIENT_NAME),
                        jsonPath("$.stock").value(STOCK)
                );
    }

    /**
     * Tests that reducing stock fails when there is insufficient stock available.
     * Verifies that the reduceStock method returns false and the stock remains unchanged.
     */
    @Test
    void cannotReduceStockWhenInsufficientAvailable() {
        Ingredient ingredientWithLowStock = new Ingredient(INGREDIENT_ID, INGREDIENT_NAME, 5, IMAGE64);
        
        boolean result = ingredientWithLowStock.reduceStock(10);
        
        assertFalse(result);
        assertEquals(5, ingredientWithLowStock.getStock());
    }

    /**
     * Tests that reducing stock with a negative value is not allowed at the model level.
     * Verifies that the reduceStock method returns false and the stock remains unchanged when given a negative value.
     */
    @Test
    void cannotReduceStockWithNegativeValue() {
        Ingredient ingredient = new Ingredient(INGREDIENT_ID, INGREDIENT_NAME, STOCK, IMAGE64);
        
        boolean result = ingredient.reduceStock(-10);
        
        assertFalse(result);
        assertEquals(STOCK, ingredient.getStock());
    }

    /**
     * Tests that updating stock for a non-existent ingredient returns 304 Not Modified.
     * Verifies that when the ingredient is not found, the service returns empty and the endpoint responds accordingly.
     */
    @Test
    @WithMockUser(roles = {"ADMIN"})
    void updateStockOfNonExistentIngredientReturnsNotModified() throws Exception {
        when(ingredientService.updateStock(eq(999L), any(IngredientStockDTO.class)))
                .thenReturn(Optional.empty());

        mockMvc.perform(patch("/ingredients/999/stock")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"stock\":50}"))
                .andExpect(status().isNotModified());
    }
}

