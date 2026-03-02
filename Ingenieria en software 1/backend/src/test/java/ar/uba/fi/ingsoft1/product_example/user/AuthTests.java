package ar.uba.fi.ingsoft1.product_example.user;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@SpringBootTest
@AutoConfigureMockMvc
class AuthTests {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void unauthenticatedUserCannotAccessUserProfile() throws Exception {
        // Tests that unauthenticated user cannot access user profile
        mockMvc.perform(get("/users/me"))
                .andExpect(status().isForbidden());
    }

    @Test
    void unauthenticatedUserCannotAccessUserProfileEndpoint() throws Exception {
        // Tests that unauthenticated user cannot access profile endpoint
        mockMvc.perform(get("/users/profile"))
                .andExpect(status().isForbidden());
    }

    @Test
    void unauthenticatedUserCannotCreateProducts() throws Exception {
        // Tests that unauthenticated user cannot create products
        String productJson = """
                {
                    "name": "Test Product",
                    "description": "Test Description",
                    "price": 15.99,
                    "categories": ["Main Course"],
                    "ingredients": []
                }
                """;

        mockMvc.perform(post("/products")
                        .contentType("application/json")
                        .content(productJson))
                .andExpect(status().isForbidden());
    }

    @Test
    void unauthenticatedUserCannotUpdateProducts() throws Exception {
        // Tests that unauthenticated user cannot update products
        String productJson = """
                {
                    "name": "Updated Product",
                    "description": "Updated Description",
                    "price": 20.99
                }
                """;

        mockMvc.perform(post("/products/1")
                        .contentType("application/json")
                        .content(productJson))
                .andExpect(status().isForbidden());
    }

    @Test
    void unauthenticatedUserCannotDeleteProducts() throws Exception {
        // Tests that unauthenticated user cannot delete products
        mockMvc.perform(post("/products/1/delete"))
                .andExpect(status().isForbidden());
    }

    @Test
    void unauthenticatedUserCannotAccessIngredients() throws Exception {
        // Tests that unauthenticated user cannot access ingredients (management)
        mockMvc.perform(get("/ingredients"))
                .andExpect(status().isForbidden());
    }

    @Test
    void unauthenticatedUserCannotCreateIngredients() throws Exception {
        // Tests that unauthenticated user cannot create ingredients
        String ingredientJson = """
                {
                    "name": "Test Ingredient",
                    "stock": 100,
                    "image": "base64_image"
                }
                """;

        mockMvc.perform(post("/ingredients")
                        .contentType("application/json")
                        .content(ingredientJson))
                .andExpect(status().isForbidden());
    }

    @Test
    void unauthenticatedUserCanAccessPublicProducts() throws Exception {
        // Tests that products are public (no authentication required)
        mockMvc.perform(get("/products"))
                .andExpect(status().isOk());
    }

    @Test
    void unauthenticatedUserCannotUpdateProfile() throws Exception {
        // Tests that unauthenticated user cannot update their profile
        String updateJson = """
                {
                    "firstName": "Updated"
                }
                """;
        mockMvc.perform(patch("/users/me")
                        .contentType("application/json")
                        .content(updateJson))
                .andExpect(status().isForbidden());
    }
}
