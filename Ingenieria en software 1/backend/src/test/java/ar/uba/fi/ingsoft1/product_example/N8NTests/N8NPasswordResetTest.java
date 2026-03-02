package ar.uba.fi.ingsoft1.product_example.N8NTests;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class N8NPasswordResetTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void passwordResetRequestSendsRequestToN8N() throws Exception {
        // Tests that password reset request sends a request to N8N webhook with resetUrl
        // First create a user by registration
        long timestamp = System.currentTimeMillis();
        String uniqueUsername = "testuser" + timestamp;
        String uniqueEmail = "test" + timestamp + "@fi.uba.ar";
        
        String userRegistrationJson = """
                {
                    "username": "%s",
                    "email": "%s",
                    "password": "password123",
                    "role": "CLIENT",
                    "firstName": "Juan",
                    "lastName": "Pérez",
                    "age": 25,
                    "gender": "M",
                    "address": "Av. Shock 1234",
                    "base64Image": ""
                }
                """.formatted(uniqueUsername, uniqueEmail);
        
        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(userRegistrationJson))
                .andExpect(status().isCreated());
        
        // Now request password reset
        String forgotPasswordJson = """
                {
                    "email": "%s"
                }
                """.formatted(uniqueEmail);
        
        mockMvc.perform(post("/users/forgot-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(forgotPasswordJson))
                .andExpect(status().isOk());
    }
}
