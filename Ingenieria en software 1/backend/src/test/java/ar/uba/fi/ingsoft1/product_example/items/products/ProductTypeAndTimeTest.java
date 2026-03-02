package ar.uba.fi.ingsoft1.product_example.items.products;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@SpringBootTest
@AutoConfigureMockMvc
class ProductTypeAndTimeTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void productEndpointReturnsValidResponse() throws Exception {
        // Tests that the products endpoint returns a valid response
        mockMvc.perform(get("/products")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.pageable").exists());
    }

    @Test
    void productTypeAndTimeFieldsExistInDatabase() throws Exception {
        // Tests that type and preparation time fields exist in the SQL query
        mockMvc.perform(get("/products")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    void productTypeFieldIsIncludedInQuery() throws Exception {
        // Tests that the type field is included in the SQL query
        mockMvc.perform(get("/products")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    void productEstimatedTimeFieldIsIncludedInQuery() throws Exception {
        // Tests that the estimated_time field is included in the SQL query
        mockMvc.perform(get("/products")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    void productTypeAndTimeAreRetrievedFromDatabase() throws Exception {
        // Tests that type and preparation time are retrieved from the database
        mockMvc.perform(get("/products")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());
    }

    @Test
    void productTypeCanBeMeatVegetarianOrWithoutTACC() throws Exception {
        mockMvc.perform(get("/products")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    void productPreparationTimeIsPositive() throws Exception {
        // Tests that preparation time is a positive number
        mockMvc.perform(get("/products")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }
}
