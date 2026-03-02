package ar.uba.fi.ingsoft1.product_example.items.ingredients;

import ar.uba.fi.ingsoft1.product_example.config.security.JwtService;
import ar.uba.fi.ingsoft1.product_example.config.security.SecurityConfig;
import ar.uba.fi.ingsoft1.product_example.items.products.ProductCreateDTO;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;

import static ar.uba.fi.ingsoft1.product_example.items.ProductConstants.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = IngredientRestController.class)
@Import({SecurityConfig.class, JwtService.class})
class IngredientRestControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private IngredientService ingredientService;

    @Test
    @WithMockUser(roles = {"ADMIN"})
    void getExistingIngredientById() throws Exception {
        var dto = new IngredientDTO(ID, NAME, STOCK, IMAGE64);
        when(ingredientService.getIngredientById(ID)).thenReturn(Optional.of(dto));

        mockMvc.perform(get("/ingredients/" + ID)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpectAll(
                        status().isOk(),
                        jsonPath("$.id").value(ID),
                        jsonPath("$.name").value(NAME),
                        jsonPath("$.stock").value(STOCK)
                );
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    void getAbsentIngredientById() throws Exception {
        when(ingredientService.getIngredientById(ID)).thenReturn(Optional.empty());

        var request = get("/ingredients/" + ID)
                .contentType(MediaType.APPLICATION_JSON);
        mockMvc.perform(request).andExpectAll(
                status().isNotFound()
        );
    }
    @Test
    @WithMockUser(roles = {"ADMIN"})
    void createIngredientSuccessfully() throws Exception {
        var newIngredient = new IngredientCreateDTO(INGREDIENT_NAME, STOCK, IMAGE64);
        var resultIngredient = new IngredientDTO(INGREDIENT_ID, INGREDIENT_NAME, STOCK, IMAGE64);
        when(ingredientService.createIngredient(newIngredient)).thenReturn(resultIngredient);

        var request = post("/ingredients")
                .content("{\"name\":\"Ingredient Name\",\"stock\":100,\"base64Image\":\"base64ImageString\"}")
                .contentType(MediaType.APPLICATION_JSON);
        mockMvc.perform(request).andExpectAll(
                status().isCreated(),
                jsonPath("$.id").value(resultIngredient.id()),
                jsonPath("$.name").value(resultIngredient.name()),
                jsonPath("$.stock").value(resultIngredient.stock())
        );
    }

    @Test
    @WithMockUser
    void createIngredientWithRegularUser() throws Exception {
        var newIngredient = new IngredientCreateDTO(NAME, STOCK, IMAGE64);
        var resultIngredient = new IngredientDTO(ID, NAME, STOCK, IMAGE64);
        when(ingredientService.createIngredient(newIngredient)).thenReturn(resultIngredient);

        var request = post("/ingredients")
                .content("{\"name\":\"Name\",\"stock\":10,\"base64Image\":\"Image64\"}")
                .contentType(MediaType.APPLICATION_JSON);
        mockMvc.perform(request).andExpectAll(
                status().isForbidden()
        );
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    void createIngredientWithMalformedJson() throws Exception {
        var request = post("/ingredients")
                .content("{")
                .contentType(MediaType.APPLICATION_JSON);
        mockMvc.perform(request).andExpectAll(
                status().isBadRequest()
        );
    }
    @Test
    @WithMockUser(roles = {"ADMIN"})
    void createIngredientWithDuplicateNameReturnsConflict() throws Exception {
        when(ingredientService.createIngredient(any(IngredientCreateDTO.class))).thenReturn(null);
        var request = post("/ingredients")
                .content("{\"name\":\"Name\",\"stock\":10,\"base64Image\":\"Image64\"}")
                .contentType(MediaType.APPLICATION_JSON);
        mockMvc.perform(request).andExpect(status().isConflict());
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    void createIngredientWithBadJson() throws Exception {
        var request = post("/ingredients")
                .content("{\"error\":1}")
                .contentType(MediaType.APPLICATION_JSON);
        mockMvc.perform(request).andExpectAll(
                status().isBadRequest()
        );
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    void deleteExistingIngredient() throws Exception {
        when(ingredientService.deleteIngredientById(ID)).thenReturn(true);

        mockMvc.perform(delete("/ingredients/" + ID)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpectAll(
                        status().isOk()
                );
    }

    @Test
    @WithMockUser
    void deleteIngredientWithRegularUser() throws Exception {
        mockMvc.perform(delete("/ingredients/" + ID)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpectAll(
                        status().isForbidden()
                );
    }
}
