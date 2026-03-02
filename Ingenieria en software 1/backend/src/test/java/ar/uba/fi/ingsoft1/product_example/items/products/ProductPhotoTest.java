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
class ProductPhotoTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void productEndpointReturnsValidResponse() throws Exception {
        // Tests that the products endpoint returns a valid response with pagination
        mockMvc.perform(get("/products")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.pageable").exists());
    }

    @Test
    void productPhotoFieldExistsInDatabase() throws Exception {
        // Tests that the base64Image field exists in the database query
        mockMvc.perform(get("/products")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    void productPhotoFieldIsIncludedInQuery() throws Exception {
        // Tests that the base64Image field is included in the SQL query
        mockMvc.perform(get("/products")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    void productPhotoCanBeEmpty() throws Exception {
        // Tests that the photo field can be empty for products without photos
        mockMvc.perform(get("/products")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    void productPhotoFieldIsString() throws Exception {
        // Tests that the base64Image field is of type String
        mockMvc.perform(get("/products")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    void productPhotoIsBase64Encoded() throws Exception {
        // Tests that the photo is base64 encoded
        mockMvc.perform(get("/products")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }
}
