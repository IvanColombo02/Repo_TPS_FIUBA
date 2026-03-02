package ar.uba.fi.ingsoft1.product_example.items;

import ar.uba.fi.ingsoft1.product_example.items.ingredients.Ingredient;
import ar.uba.fi.ingsoft1.product_example.items.ingredients.IngredientRepository;
import ar.uba.fi.ingsoft1.product_example.items.products.ProductCreateDTO;
import ar.uba.fi.ingsoft1.product_example.items.products.ProductUpdateDTO;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static ar.uba.fi.ingsoft1.product_example.items.ProductConstants.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
@AutoConfigureMockMvc
class ProductIntegrationTest {

    @Autowired
    private IngredientRepository ingredientRepository;

    @Autowired
    private ar.uba.fi.ingsoft1.product_example.items.products.ProductService productService;

    @Autowired
    private MockMvc mockMvc;

    Ingredient ingredient;
    Ingredient ingredient2;

    @BeforeEach
    void setup() {
        ingredient = ingredientRepository.save(new Ingredient(INGREDIENT_NAME, STOCK, IMAGE64));
        ingredient2 = ingredientRepository.save(new Ingredient(INGREDIENT_NAME + "2", STOCK, IMAGE64));
    }

    @AfterEach
    void clean() {
        productService.getProducts(new ComponentSearchDTO(Optional.empty(),
                Optional.empty(),
                Optional.empty(), Optional.empty(),
                Optional.empty(), Optional.empty(), Optional.empty()),
                org.springframework.data.domain.Pageable.unpaged())
                .forEach(product -> productService.deleteProductById(product.id()));
        ingredientRepository.deleteAll();
    }

    @Test
    @WithMockUser(username = "user")
    void getAllReturnsCreatedProduct() throws Exception {
        var ingredients = new ArrayList<Long>();
        ingredients.add(ingredient.getId());

        var created = productService.createProduct(
                new ProductCreateDTO(NAME, DESCRIPTION, PRICE, List.of(CATEGORY), TYPE, ESTIMATED_TIME,
                        Map.of(ingredient.getId(), 1), IMAGE64)
        );
        var request = get("/products").contentType(MediaType.APPLICATION_JSON);
        mockMvc.perform(request).andExpectAll(
                status().isOk(),
                jsonPath("$.content[0].id").isNumber(),
                jsonPath("$.content[0].name").value(NAME),
                jsonPath("$.content[0].description").value(DESCRIPTION),
                jsonPath("$.content[0].price").value(PRICE),
                jsonPath("$.content[0].categories").isArray()
        );

        var detailRequest = get("/products/" + created.id()).contentType(MediaType.APPLICATION_JSON);
        mockMvc.perform(detailRequest).andExpectAll(
                status().isOk(),
                jsonPath("$.id").value(created.id()),
                jsonPath("$.ingredients").isMap(),
                jsonPath("$.ingredients").isNotEmpty()
        );
    }

    @Test
    void getReturnsLatestUpdateName() {
        var ingredients = new ArrayList<Long>();
        ingredients.add(ingredient.getId());

        var id = productService.createProduct(
                new ProductCreateDTO(NAME, DESCRIPTION, PRICE, List.of(CATEGORY), TYPE, ESTIMATED_TIME,
                        Map.of(ingredient.getId(), 1), IMAGE64)
        ).id();
        productService.updateProduct(
                id,
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
        );
        var response = productService.getProductById(id);

        assertEquals(NAME2, response.orElseThrow().name());
        assertEquals(DESCRIPTION, response.orElseThrow().description());
    }

    @Test
    void getReturnsLatestUpdateIngredientsAdd() {
        var ingredients = new ArrayList<Long>();
        ingredients.add(ingredient.getId());

        var id = productService.createProduct(
                new ProductCreateDTO(NAME, DESCRIPTION, PRICE, List.of(CATEGORY), TYPE, ESTIMATED_TIME,
                        Map.of(ingredient.getId(), 1), IMAGE64)
        ).id();
        productService.updateProduct(
                id,
                new ProductUpdateDTO(
                        Optional.of(NAME2),
                        Optional.empty(),
                        Optional.empty(),
                        Optional.empty(),
                        Optional.empty(),
                        Optional.empty(),
                        Optional.of(Map.of(ingredient2.getId(), 1)),
                        Optional.empty(),
                        Optional.empty()
                        )
        );
        var response = productService.getProductById(id);
        var resultIngredients = response.orElseThrow().ingredients();
        assertEquals(2, resultIngredients.size(), "Should have 2 ingredients after adding ingredient2");
    }

    @Test
    void getReturnsLatestUpdateIngredientsRemove() {
        var ingredients = new ArrayList<Long>();
        ingredients.add(ingredient.getId());
        ingredients.add(ingredient2.getId());

        var id = productService.createProduct(
                new ProductCreateDTO(NAME, DESCRIPTION, PRICE, List.of(CATEGORY), TYPE, ESTIMATED_TIME,
                        Map.of(ingredient.getId(), 1, ingredient2.getId(), 1), IMAGE64)
        ).id();
        productService.updateProduct(
                id,
                new ProductUpdateDTO(
                        Optional.of(NAME2),
                        Optional.empty(),
                        Optional.empty(),
                        Optional.empty(),
                        Optional.empty(),
                        Optional.empty(),
                        Optional.empty(),
                        Optional.of(List.of(ingredient2.getId())),
                        Optional.empty()
                )
        );
        var response = productService.getProductById(id);
        var resultIngredients = response.orElseThrow().ingredients();
        assertEquals(1, resultIngredients.size(), "Should have only 1 ingredient after removing ingredient2");
    }
}
