package ar.uba.fi.ingsoft1.product_example.items.products;

import ar.uba.fi.ingsoft1.product_example.config.security.JwtService;
import ar.uba.fi.ingsoft1.product_example.config.security.SecurityConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import ar.uba.fi.ingsoft1.product_example.items.ingredients.IngredientDTO;

import static ar.uba.fi.ingsoft1.product_example.items.ProductConstants.*;
import static org.mockito.Mockito.when;

@WebMvcTest(controllers = ProductRestController.class)
@Import({SecurityConfig.class, JwtService.class})
class ProductRestControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ProductService productService;

    ProductDTO resultProduct;
    IngredientDTO ingredientDTO;
    @BeforeEach
    void setUp() {
        Map<Long, Integer> ingredients = Map.of(INGREDIENT_ID, 1);
        ingredientDTO = new IngredientDTO(INGREDIENT_ID, INGREDIENT_NAME, STOCK, IMAGE64);
        var dto = new ProductDTO(ID, NAME, DESCRIPTION, PRICE, STOCK, List.of(CATEGORY), TYPE, ESTIMATED_TIME,
                Map.of(ingredientDTO, 1), IMAGE64);
        when(productService.getProductById(ID)).thenReturn(Optional.of(dto));
        var newProduct = new ProductCreateDTO(NAME, DESCRIPTION, PRICE, List.of(CATEGORY), TYPE, ESTIMATED_TIME,
                ingredients, IMAGE64);
        resultProduct = new ProductDTO(ID, NAME, DESCRIPTION, PRICE, STOCK,
                List.of(CATEGORY), TYPE, ESTIMATED_TIME, Map.of(ingredientDTO, 1), IMAGE64);
        when(productService.createProduct(newProduct)).thenReturn(resultProduct);
    }
    @Test
    @WithMockUser
    void getExistingProductById() throws Exception {
        var request = get("/products/" + ID)
                .contentType(MediaType.APPLICATION_JSON);
        mockMvc.perform(request).andExpectAll(
                status().isOk(),
                jsonPath("$.id").value(ID),
                jsonPath("$.name").value(NAME),
                jsonPath("$.description").value(DESCRIPTION)
        );
    }

    @Test
    @WithMockUser
    void getExistingProductByIdWithCorrectIngredients() throws Exception {
        var request = get("/products/" + ID)
                .contentType(MediaType.APPLICATION_JSON);
        mockMvc.perform(request).andExpectAll(
                status().isOk(),
                jsonPath("$.ingredients").isMap(),
                jsonPath("$.ingredients").isNotEmpty()
        );
    }

    @Test
    @WithMockUser
    void getAbsentProductById() throws Exception {
        when(productService.getProductById(ID)).thenReturn(Optional.empty());

        var request = get("/products/" + ID)
                .contentType(MediaType.APPLICATION_JSON);
        mockMvc.perform(request).andExpectAll(
                status().isNotFound()
        );
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    void createProductSuccessfully() throws Exception {
        var request = post("/products")
                .content("{\"name\":\"" + NAME + "\",\"description\":\"" + DESCRIPTION + "\",\"price\":" + PRICE +
                        ",\"categories\":[\"" + CATEGORY + "\"],\"type\":\"" + TYPE + "\",\"estimatedTime\":" + ESTIMATED_TIME + 
                        ",\"ingredientsIds\":{\"" + INGREDIENT_ID + "\":1},\"base64Image\":\"" + IMAGE64 + "\"}")
                .contentType(MediaType.APPLICATION_JSON);
        mockMvc.perform(request).andExpectAll(
                status().isCreated(),
                jsonPath("$.id").value(resultProduct.id()),
                jsonPath("$.name").value(resultProduct.name()),
                jsonPath("$.description").value(resultProduct.description()),
                jsonPath("$.price").value(resultProduct.price()),
                jsonPath("$.categories").isArray(),
                jsonPath("$.ingredients").isMap()
        );
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    void createProductSuccessfullyWithCorrectIngredients() throws Exception {
        var request = post("/products")
                .content("{\"name\":\"" + NAME + "\",\"description\":\"" + DESCRIPTION + "\",\"price\":" + PRICE +
                        ",\"categories\":[\"" + CATEGORY + "\"],\"type\":\"" + TYPE + "\",\"estimatedTime\":" + ESTIMATED_TIME + 
                        ",\"ingredientsIds\":{\"" + INGREDIENT_ID + "\":1},\"base64Image\":\"" + IMAGE64 + "\"}")
                .contentType(MediaType.APPLICATION_JSON);
        mockMvc.perform(request).andExpectAll(
                status().isCreated(),
                jsonPath("$.ingredients").isMap(),
                jsonPath("$.ingredients").isNotEmpty()
        );
    }
    @Test
    @WithMockUser(roles = {"ADMIN"})
    void cantCreateProductWithEmptyIngredients() throws Exception {
        var request = post("/products")
                .content("{\"name\":\"" + NAME + "\",\"description\":\"" + DESCRIPTION + "\",\"price\":" + PRICE +
                        ",\"categories\":[\"" + CATEGORY + "\"],\"type\":\"" + TYPE + "\",\"estimatedTime\":" + ESTIMATED_TIME +
                        ",\"ingredientsIds\":{\"\"},\"base64Image\":\"" + IMAGE64 + "\"}")
                .contentType(MediaType.APPLICATION_JSON);
                mockMvc.perform(request).andExpectAll(
                        status().isBadRequest()
                );
            }
            @Test
            @WithMockUser(roles = {"ADMIN"})
            void createProductWithDuplicateNameReturnsConflict() throws Exception {
                when(productService.createProduct(any(ProductCreateDTO.class))).thenReturn(null);
                var request = post("/products")
                        .content("{\"name\":\"" + NAME + "\",\"description\":\"" + DESCRIPTION + "\",\"price\":" + PRICE +
                                ",\"categories\":[\"" + CATEGORY + "\"],\"type\":\"" + TYPE + "\",\"estimatedTime\":" + ESTIMATED_TIME +
                                ",\"ingredientsIds\":{\"" + INGREDIENT_ID + "\":1},\"base64Image\":\"" + IMAGE64 + "\"}")
                        .contentType(MediaType.APPLICATION_JSON);
                mockMvc.perform(request).andExpect(status().isConflict());
            }

            @Test
            @WithMockUser
            void createProductWithRegularUser() throws Exception {
            var request = post("/products")
                .content("{\"name\":\"Name\",\"description\":\"Description\"}")
                .contentType(MediaType.APPLICATION_JSON);
            mockMvc.perform(request).andExpectAll(
                status().isForbidden()
        );
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    void createProductWithMalformedJson() throws Exception {
        var request = post("/products")
                .content("{")
                .contentType(MediaType.APPLICATION_JSON);
        mockMvc.perform(request).andExpectAll(
                status().isBadRequest()
        );
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    void createProductWithBadJson() throws Exception {
        var request = post("/products")
                .content("{\"error\":1}")
                .contentType(MediaType.APPLICATION_JSON);
        mockMvc.perform(request).andExpectAll(
                status().isBadRequest()
        );
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    void deleteExistingProductById() throws Exception {
        when(productService.deleteProductById(ID)).thenReturn(true);

        var request = delete("/products/" + ID)
                .contentType(MediaType.APPLICATION_JSON);
        mockMvc.perform(request).andExpectAll(
                status().isOk()
        );
    }

    @Test
    @WithMockUser
    void deleteProductWithRegularUser() throws Exception {
        var request = delete("/products/" + ID)
                .contentType(MediaType.APPLICATION_JSON);
        mockMvc.perform(request).andExpectAll(
                status().isForbidden()
        );
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    void updateProductById() throws Exception {
        var updatedProduct = new ProductDTO(ID, NAME2, DESCRIPTION, PRICE, STOCK,
                List.of(CATEGORY), TYPE, ESTIMATED_TIME, Map.of(ingredientDTO, 1), IMAGE64);
        when(productService.updateProduct(ID, 
                new ProductUpdateDTO(
                    Optional.of(NAME2), 
                    Optional.empty(), 
                    Optional.empty(), 
                    Optional.empty(), 
                    Optional.empty(), 
                    Optional.empty(),
                        Optional.empty(),
                        Optional.empty(),
                        Optional.empty()
                )
        )).thenReturn(Optional.of(updatedProduct));

        var request = patch("/products/" + ID)
                .content("{\"name\":\"" + NAME2 + "\"}")
                .contentType(MediaType.APPLICATION_JSON);
        mockMvc.perform(request).andExpectAll(
                status().isOk(),
                jsonPath("$.name").value(NAME2)
        );
    }

    @Test
    @WithMockUser
    void updateProductAsRegularUser() throws Exception {
        var request = patch("/products/" + ID)
                .content("{\"name\":\"" + NAME2 + "\"}")
                .contentType(MediaType.APPLICATION_JSON);
        mockMvc.perform(request).andExpectAll(
                status().isForbidden()
        );
    }
}
