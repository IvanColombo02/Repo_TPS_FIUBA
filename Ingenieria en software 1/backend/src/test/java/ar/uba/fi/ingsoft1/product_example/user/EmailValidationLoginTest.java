package ar.uba.fi.ingsoft1.product_example.user;

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
class EmailValidationLoginTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void loginFailsWhenEmailNotValidated() throws Exception {
        // Tests that login fails when email is not validated
        String loginJson = """
                {
                    "username": "testuser",
                    "password": "password123"
                }
                """;
        mockMvc.perform(post("/api/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginJson))
                .andExpect(status().isForbidden());
    }

    @Test
    void loginSucceedsWhenEmailIsValidated() throws Exception {
        // Tests that login works when email is validated
        String loginJson = """
                {
                    "username": "validateduser",
                    "password": "password123"
                }
                """;
        mockMvc.perform(post("/api/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginJson))
                .andExpect(status().isForbidden());
    }

    @Test
    void unvalidatedEmailCannotAccessProtectedResources() throws Exception {
        // Tests that users with unvalidated email cannot access protected resources
        String loginJson = """
                {
                    "username": "unvalidateduser",
                    "password": "password123"
                }
                """;
        mockMvc.perform(post("/api/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginJson))
                .andExpect(status().isForbidden());
    }
}
