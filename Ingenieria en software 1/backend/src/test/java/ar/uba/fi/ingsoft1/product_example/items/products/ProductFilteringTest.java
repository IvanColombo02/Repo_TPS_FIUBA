package ar.uba.fi.ingsoft1.product_example.items.products;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class ProductFilteringTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void filterByType() throws Exception {
        // Tests filtering products by type
        mockMvc.perform(get("/products")
                        .param("type", "Meat")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    void filterByCategory() throws Exception {
        // Tests filtering products by category
        mockMvc.perform(get("/products")
                        .param("category", "Breakfast")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    void filterByName() throws Exception {
        // Tests filtering products by name
        mockMvc.perform(get("/products")
                        .param("name", "Pizza")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    void filterByPrice() throws Exception {
        // Tests filtering products by price range
        mockMvc.perform(get("/products")
                        .param("minPrice", "100")
                        .param("maxPrice", "500")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    void filterByStock() throws Exception {
        // Tests filtering products by minimum stock
        mockMvc.perform(get("/products")
                        .param("minStock", "10")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    void filterByMultipleCriteria() throws Exception {
        // Tests filtering products by multiple criteria simultaneously
        mockMvc.perform(get("/products")
                        .param("type", "Vegetarian")
                        .param("category", "Lunch")
                        .param("minPrice", "200")
                        .param("maxPrice", "800")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    void filterByAllTypes() throws Exception {
        // Tests filtering by all available product types
        mockMvc.perform(get("/products")
                        .param("type", "Meat")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        mockMvc.perform(get("/products")
                        .param("type", "Vegetarian")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        mockMvc.perform(get("/products")
                        .param("type", "Without TACC")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    void filterByAllCategories() throws Exception {
        // Tests filtering by all available product categories
        mockMvc.perform(get("/products")
                        .param("category", "Breakfast")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        mockMvc.perform(get("/products")
                        .param("category", "Lunch")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        mockMvc.perform(get("/products")
                        .param("category", "Dinner")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }
}
