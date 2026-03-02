package ar.uba.fi.ingsoft1.product_example.items.combos;

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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class ComboCRUDTest {

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
                Ingredient ingredient = ingredientRepository.save(new Ingredient("Test Ingredient", 100, ""));
                var product = productService.createProduct(
                                new ProductCreateDTO("Test Product", "Test Description", 100.0f,
                                                List.of("Food"), "Principal", 30, Map.of(ingredient.getId(), 1), ""));
                productId = product.id();
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        void testCreateModifyAndDeleteCombo() throws Exception {
                // Tests complete CRUD operations for combos: create, read, update, delete
                String createComboJson = String.format("""
                                {
                                    "name": "Combo Test",
                                    "description": "A Combo test",
                                    "price": 1500.0,
                                    "categories": ["Food", "Test"],
                                    "types": ["Principal"],
                                    "productsIds": {"%d": 1},
                                    "base64Image": ""
                                }
                                """, productId);

                var createResult = mockMvc.perform(post("/combos")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(createComboJson))
                                .andExpect(status().isCreated())
                                .andExpect(jsonPath("$.name").value("Combo Test"))
                                .andExpect(jsonPath("$.description").value("A Combo test"))
                                .andExpect(jsonPath("$.price").value(1500.0))
                                .andReturn();

                String responseBody = createResult.getResponse().getContentAsString();
                var responseJson = objectMapper.readTree(responseBody);
                long comboId = responseJson.get("id").asLong();

                String updateComboJson = """
                                {
                                    "name": "Combo test modified",
                                    "description": "Actualizated description of combo",
                                    "price": 2000.0,
                                    "categories": ["Food", "Test", "Modified"],
                                    "types": ["Principal", "Especial"]
                                }
                                """;

                mockMvc.perform(patch("/combos/" + comboId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(updateComboJson))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.name").value("Combo test modified"))
                                .andExpect(jsonPath("$.description").value("Actualizated description of combo"))
                                .andExpect(jsonPath("$.price").value(2000.0))
                                .andExpect(jsonPath("$.categories").isArray())
                                .andExpect(jsonPath("$.types").isArray());
                mockMvc.perform(get("/combos/" + comboId))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.id").value(comboId))
                                .andExpect(jsonPath("$.name").value("Combo test modified"));
                mockMvc.perform(delete("/combos/" + comboId))
                                .andExpect(status().isOk());
                mockMvc.perform(get("/combos/" + comboId))
                                .andExpect(status().isNotFound());
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        void testUpdateNonExistentCombo() throws Exception {
                // Tests updating a non-existent combo returns appropriate error
                String updateComboJson = """
                                {
                                    "name": "non-existent Combo",
                                    "description": "This combo not exists"
                                }
                                """;

                mockMvc.perform(patch("/combos/99999")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(updateComboJson))
                                .andExpect(status().isNotModified());
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        void testDeleteNonExistentCombo() throws Exception {
                // Tests deleting a non-existent combo returns appropriate error
                mockMvc.perform(delete("/combos/99999"))
                                .andExpect(status().isNotFound());
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        void testGetNonExistentCombo() throws Exception {
                // Tests getting a non-existent combo returns appropriate error
                mockMvc.perform(get("/combos/99999"))
                                .andExpect(status().isNotFound());
        }
}
