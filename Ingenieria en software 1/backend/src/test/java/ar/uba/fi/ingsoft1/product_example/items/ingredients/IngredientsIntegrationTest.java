package ar.uba.fi.ingsoft1.product_example.items.ingredients;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static ar.uba.fi.ingsoft1.product_example.items.ProductConstants.*;

@SpringBootTest
@AutoConfigureMockMvc
class IngredientsIntegrationTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private IngredientService ingredientService;
    @BeforeEach
    void setup() {}
    @Test
    @WithMockUser(roles = {"ADMIN"})
    void getAllReturnsCreatedIngredient() throws Exception {
        ingredientService.createIngredient(
                new IngredientCreateDTO(NAME, STOCK,IMAGE64)
        );
        var request = get("/ingredients").contentType(MediaType.APPLICATION_JSON);
        mockMvc.perform(request).andExpectAll(
                status().isOk(),
                jsonPath("$.content[0].id").isNumber(),
                jsonPath("$.content[0].name").value(NAME),
                jsonPath("$.content[0].stock").value(STOCK)
        );
    }
}