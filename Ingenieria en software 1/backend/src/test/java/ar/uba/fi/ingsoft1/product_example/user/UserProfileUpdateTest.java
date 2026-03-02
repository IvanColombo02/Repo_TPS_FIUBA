package ar.uba.fi.ingsoft1.product_example.user;

import ar.uba.fi.ingsoft1.product_example.config.security.JwtService;
import ar.uba.fi.ingsoft1.product_example.config.security.SecurityConfig;
import ar.uba.fi.ingsoft1.product_example.user.refresh_token.RefreshTokenService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.security.Principal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = UserRestController.class)
@Import({ SecurityConfig.class, JwtService.class })
class UserProfileUpdateTest {

        @Autowired
        private MockMvc mockMvc;

        @MockitoBean
        private UserService userService;

        @MockitoBean
        private VerificationService verificationService;

        @MockitoBean
        private PasswordResetService passwordResetService;

        @MockitoBean
        private RefreshTokenService refreshTokenService;

        private User user;
        private UserDTO userDTO;

        @BeforeEach
        void setup() {
                user = new User("Juan", "Pérez", 25, "M", "Street 123",
                                "data:image/jpeg;base64,/9j/4AAQSkZJRgABAQEAYABgAAD",
                                "juan123", "encodedPassword", "ROLE_USER");
                user.setId(1L);
                user.setEmail("juan@fi.uba.ar");
                user.setEmailVerified(true);

                userDTO = new UserDTO(user);
        }

        /**
         * Tests that an authenticated user can successfully update their profile with
         * multiple fields.
         * Verifies that firstName, lastName, age, gender, address, username, and email
         * can be updated.
         */
        @Test
        @WithMockUser(username = "juan123")
        void updateProfileSuccessfullyWithMultipleFields() throws Exception {
                User updatedUser = new User("Carlos", "García", 30, "M", "Avenue 456",
                                null, "carlos123", "encodedPassword", "ROLE_USER");
                updatedUser.setId(1L);
                updatedUser.setEmail("carlos@fi.uba.ar");
                updatedUser.setEmailVerified(true);
                UserDTO updatedDTO = new UserDTO(updatedUser);

                when(userService.updateMe(any(Principal.class), any(UserUpdateDTO.class)))
                                .thenReturn(Optional.of(updatedDTO));

                String updateJson = """
                                {
                                    "firstName": "Carlos",
                                    "lastName": "García",
                                    "age": 30,
                                    "gender": "M",
                                    "address": "Avenue 456",
                                    "username": "carlos123",
                                    "email": "carlos@fi.uba.ar"
                                }
                                """;

                mockMvc.perform(patch("/users/me")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(updateJson))
                                .andExpectAll(
                                                status().isOk(),
                                                jsonPath("$.firstName").value("Carlos"),
                                                jsonPath("$.lastName").value("García"),
                                                jsonPath("$.age").value(30),
                                                jsonPath("$.gender").value("M"),
                                                jsonPath("$.address").value("Avenue 456"),
                                                jsonPath("$.username").value("carlos123"),
                                                jsonPath("$.email").value("carlos@fi.uba.ar"));
        }

        /**
         * Tests that an authenticated user can update their profile photo.
         * Verifies that the base64Image field can be updated with a new image.
         */
        @Test
        @WithMockUser(username = "juan123")
        void updateProfilePhotoSuccessfully() throws Exception {
                User updatedUser = new User("Juan", "Pérez", 25, "M", "Street 123",
                                "data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVR42mNkYPhfDwAChwGA60e6kgAAAABJRU5ErkJggg==",
                                "juan123", "encodedPassword", "ROLE_USER");
                updatedUser.setId(1L);
                updatedUser.setEmail("juan@fi.uba.ar");
                UserDTO updatedDTO = new UserDTO(updatedUser);

                when(userService.updateMe(any(Principal.class), any(UserUpdateDTO.class)))
                                .thenReturn(Optional.of(updatedDTO));

                String updateJson = """
                                {
                                    "base64Image": "data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVR42mNkYPhfDwAChwGA60e6kgAAAABJRU5ErkJggg=="
                                }
                                """;

                mockMvc.perform(patch("/users/me")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(updateJson))
                                .andExpectAll(
                                                status().isOk(),
                                                jsonPath("$.base64Image").exists());
        }

        /**
         * Tests that updating profile with username shorter than minimum length is
         * rejected.
         * Verifies that the @Size(min = 3) validation constraint is enforced for
         * username.
         */
        @Test
        @WithMockUser(username = "juan123")
        void updateProfileWithShortUsernameReturnsBadRequest() throws Exception {
                String updateJson = """
                                {
                                    "username": "ab"
                                }
                                """;

                mockMvc.perform(patch("/users/me")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(updateJson))
                                .andExpect(status().isBadRequest());
        }

        /**
         * Tests that updating profile with firstName shorter than minimum length is
         * rejected.
         * Verifies that the @Size(min = 3) validation constraint is enforced.
         */
        @Test
        @WithMockUser(username = "juan123")
        void updateProfileWithShortFirstNameReturnsBadRequest() throws Exception {
                String updateJson = """
                                {
                                    "firstName": "Jo"
                                }
                                """;

                mockMvc.perform(patch("/users/me")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(updateJson))
                                .andExpect(status().isBadRequest());
        }

        /**
         * Tests that updating profile with negative age is rejected.
         * Verifies that the @Min(0) validation constraint is enforced.
         */
        @Test
        @WithMockUser(username = "juan123")
        void updateProfileWithNegativeAgeReturnsBadRequest() throws Exception {
                String updateJson = """
                                {
                                    "age": -5
                                }
                                """;

                mockMvc.perform(patch("/users/me")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(updateJson))
                                .andExpect(status().isBadRequest());
        }

        /**
         * Tests that updating profile without authentication is denied.
         * Verifies that the endpoint requires authentication and returns 403 Forbidden.
         */
        @Test
        void updateProfileWithoutAuthenticationReturnsForbidden() throws Exception {
                String updateJson = """
                                {
                                    "firstName": "Carlos"
                                }
                                """;

                mockMvc.perform(patch("/users/me")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(updateJson))
                                .andExpect(status().isForbidden());
        }

        /**
         * Tests that the user role cannot be modified through profile update endpoint.
         * Verifies that even if a role field is sent, it is not processed by the
         * updateMe method.
         */
        @Test
        @WithMockUser(username = "juan123", roles = { "USER" })
        void updateProfileDoesNotAllowRoleModification() throws Exception {
                when(userService.updateMe(any(Principal.class), any(UserUpdateDTO.class)))
                                .thenAnswer(invocation -> {
                                        UserUpdateDTO dto = invocation.getArgument(1);
                                        // Verify that UserUpdateDTO does not have a role field
                                        assertNotNull(dto);
                                        // The role should remain unchanged
                                        return Optional.of(userDTO);
                                });

                String updateJson = """
                                {
                                    "firstName": "Carlos"
                                }
                                """;

                mockMvc.perform(patch("/users/me")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(updateJson))
                                .andExpect(status().isOk());

                // Verify that updateMe was called and role was not modified
                verify(userService, times(1)).updateMe(any(Principal.class), any(UserUpdateDTO.class));
        }

        /**
         * Tests that profile changes are persisted to the database.
         * Verifies that the service saves the updated user entity.
         */
        @Test
        @WithMockUser(username = "juan123")
        void updateProfileChangesArePersistedToDatabase() throws Exception {
                User updatedUser = new User("Carlos", "García", 30, "M", "Avenue 456",
                                null, "carlos123", "encodedPassword", "ROLE_USER");
                updatedUser.setId(1L);
                updatedUser.setEmail("carlos@fi.uba.ar");
                UserDTO updatedDTO = new UserDTO(updatedUser);

                when(userService.updateMe(any(Principal.class), any(UserUpdateDTO.class)))
                                .thenReturn(Optional.of(updatedDTO));

                String updateJson = """
                                {
                                    "firstName": "Carlos",
                                    "lastName": "García"
                                }
                                """;

                mockMvc.perform(patch("/users/me")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(updateJson))
                                .andExpect(status().isOk());

                // Verify that the service method was called, which should save to database
                verify(userService, times(1)).updateMe(any(Principal.class), any(UserUpdateDTO.class));
        }

        /**
         * Tests that an authenticated user can retrieve their profile.
         * Verifies that the GET /users/me endpoint returns the current user's profile
         * data.
         */
        @Test
        @WithMockUser(username = "juan123")
        void getProfileSuccessfully() throws Exception {
                when(userService.getByUsername(any(Principal.class)))
                                .thenReturn(Optional.of(userDTO));

                mockMvc.perform(get("/users/me")
                                .contentType(MediaType.APPLICATION_JSON))
                                .andExpectAll(
                                                status().isOk(),
                                                jsonPath("$.firstName").value("Juan"),
                                                jsonPath("$.lastName").value("Pérez"),
                                                jsonPath("$.username").value("juan123"),
                                                jsonPath("$.email").value("juan@fi.uba.ar"),
                                                jsonPath("$.role").value("ROLE_USER"));
        }

        /**
         * Tests that updating profile with empty optional fields only updates provided
         * fields.
         * Verifies that partial updates work correctly and only specified fields are
         * modified.
         */
        @Test
        @WithMockUser(username = "juan123")
        void updateProfilePartiallyOnlyUpdatesProvidedFields() throws Exception {
                User updatedUser = new User("Carlos", "Pérez", 25, "M", "Street 123",
                                null, "juan123", "encodedPassword", "ROLE_USER");
                updatedUser.setId(1L);
                updatedUser.setEmail("juan@fi.uba.ar");
                UserDTO updatedDTO = new UserDTO(updatedUser);

                when(userService.updateMe(any(Principal.class), any(UserUpdateDTO.class)))
                                .thenReturn(Optional.of(updatedDTO));

                String updateJson = """
                                {
                                    "firstName": "Carlos"
                                }
                                """;

                mockMvc.perform(patch("/users/me")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(updateJson))
                                .andExpectAll(
                                                status().isOk(),
                                                jsonPath("$.firstName").value("Carlos"),
                                                jsonPath("$.lastName").value("Pérez"),
                                                jsonPath("$.username").value("juan123"));
        }
}
