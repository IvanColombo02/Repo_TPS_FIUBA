package ar.uba.fi.ingsoft1.product_example.items.combos;

import ar.uba.fi.ingsoft1.product_example.items.Component;
import ar.uba.fi.ingsoft1.product_example.items.ingredients.Ingredient;
import ar.uba.fi.ingsoft1.product_example.items.ingredients.IngredientRepository;
import ar.uba.fi.ingsoft1.product_example.items.products.ProductCreateDTO;
import ar.uba.fi.ingsoft1.product_example.items.products.ProductService;
import com.fasterxml.jackson.databind.ObjectMapper;
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

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class ComboStructureTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private IngredientRepository ingredientRepository;

    @Autowired
    private ProductService productService;

    private Long productId;

    @BeforeEach
    void setup() {
        // Create ingredient with stock 0 so the product and combo will also have stock
        // 0
        Ingredient ingredient = ingredientRepository.save(new Ingredient("Test Ingredient", 0, ""));
        var product = productService.createProduct(
                new ProductCreateDTO("Test Product", "Test Description", 100.0f,
                        List.of("Food"), "Principal", 30, Map.of(ingredient.getId(), 1), ""));
        productId = product.id();
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testComboHasAllRequiredFields() throws Exception {
        // Verifies that a created combo has all required fields with correct structure
        String createComboJson = String.format(
                """
                        {
                            "name": "Complete combo test",
                            "description": "A complete compbination",
                            "price": 3500.0,
                            "categories": ["Food", "Drink"],
                            "types": ["Principal", "Especial"],
                            "productsIds": {"%d": 1},
                            "base64Image": "iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVR42mP8/5+hHgAHggJ/PchI7wAAAABJRU5ErkJggg=="
                        }
                        """,
                productId);

        var result = mockMvc.perform(post("/combos")
                .contentType(MediaType.APPLICATION_JSON)
                .content(createComboJson))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.name").value("Complete combo test"))
                .andExpect(jsonPath("$.description").value("A complete compbination"))
                .andExpect(jsonPath("$.price").value(3500.0))
                .andExpect(jsonPath("$.stock").value(0))
                .andExpect(jsonPath("$.categories").isArray())
                .andExpect(jsonPath("$.types").isArray())
                .andExpect(jsonPath("$.products").exists())
                .andExpect(jsonPath("$.products").isMap())
                .andExpect(jsonPath("$.base64Image").exists())
                .andReturn();
        String responseBody = result.getResponse().getContentAsString();
        var responseJson = objectMapper.readTree(responseBody);
        long comboId = responseJson.get("id").asLong();
        assertTrue(comboId > 0, "The id combination must be greater than 0");
        assertFalse(responseJson.has("discount"),
                "the field 'discount' must not exist in combo structure");
    }
}