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
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = UserRestController.class)
@Import({ SecurityConfig.class, JwtService.class })
class UserRegistrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private VerificationService verificationService;

    @MockitoBean
    private PasswordResetService passwordResetService;

    @MockitoBean
    private UserRepository userRepository;

    @MockitoBean
    private RefreshTokenService refreshTokenService;

    private BCryptPasswordEncoder passwordEncoder;

    @BeforeEach
    void setup() {
        passwordEncoder = new BCryptPasswordEncoder();
    }

    @Test
    void testRegistrationFormIncludesAllRequiredFields() throws Exception {
        // Tests that the registration form includes all required fields

        String validRegistrationJson = """
                {
                    "firstName": "Juan",
                    "lastName": "Pérez",
                    "age": 25,
                    "gender": "M",
                    "address": "Street 123, New York",
                    "base64Image": "data:image/jpeg;base64,/9j/4AAQSkZJRgABAQEAYABgAAD",
                    "username": "juan123",
                    "password": "password123",
                    "role": "ROLE_USER",
                    "email": "juan@fi.uba.ar"
                }
                """;
        when(userService.createUser(any(UserCreateDTO.class)))
                .thenReturn(Optional.of(new TokenDTO("mock-token", "mock-refresh-token")));

        mockMvc.perform(post("/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(validRegistrationJson))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.accessToken").exists())
                .andExpect(jsonPath("$.refreshToken").exists());
    }

    @Test
    void testDataStoredCorrectlyInDatabase() throws Exception {
        // Tests that user data is stored correctly in the database

        String registrationJson = """
                {
                    "firstName": "María",
                    "lastName": "García",
                    "age": 30,
                    "gender": "F",
                    "address": "Avenue 456, Charlotte",
                    "base64Image": "data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVR42mNkYPhfDwAChwGA60e6kgAAAABJRU5ErkJggg==",
                    "username": "maria123",
                    "password": "password123",
                    "role": "ROLE_USER",
                    "email": "maria@fi.uba.ar"
                }
                """;
        when(userService.createUser(any(UserCreateDTO.class)))
                .thenAnswer(invocation -> {
                    UserCreateDTO dto = invocation.getArgument(0);
                    assert dto.firstName().equals("María");
                    assert dto.lastName().equals("García");
                    assert dto.age().equals(30);
                    assert dto.gender().equals("F");
                    assert dto.address().equals("Avenue 456, Charlotte");
                    assert dto.base64Image() != null;
                    assert dto.username().equals("maria123");
                    assert dto.email().equals("maria@fi.uba.ar");
                    return Optional.of(new TokenDTO("mock-token", "mock-refresh-token"));
                });
        mockMvc.perform(post("/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(registrationJson))
                .andExpect(status().isCreated());
    }

    @Test
    void testEmailMustBeUnique() throws Exception {
        // Tests that email addresses must be unique
        String firstUserJson = """
                {
                    "firstName": "Carlos",
                    "lastName": "López",
                    "age": 28,
                    "gender": "M",
                    "address": "Plaza 789, Times Square",
                    "base64Image": null,
                    "username": "carlos123",
                    "password": "password123",
                    "role": "ROLE_USER",
                    "email": "carlos@fi.uba.ar"
                }
                """;
        String duplicateEmailJson = """
                {
                    "firstName": "Ana",
                    "lastName": "Martínez",
                    "age": 26,
                    "gender": "F",
                    "address": "Street 321, New Jersey",
                    "base64Image": null,
                    "username": "ana123",
                    "password": "password456",
                    "role": "ROLE_USER",
                    "email": "carlos@fi.uba.ar"
                }
                """;
        when(userService.createUser(any(UserCreateDTO.class)))
                .thenReturn(Optional.of(new TokenDTO("mock-token", "mock-refresh-token")));

        mockMvc.perform(post("/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(firstUserJson))
                .andExpect(status().isCreated());

        when(userService.createUser(any(UserCreateDTO.class)))
                .thenReturn(Optional.empty());

        mockMvc.perform(post("/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(duplicateEmailJson))
                .andExpect(status().isConflict());
    }

    @Test
    void testValidateDuplicateEmail() throws Exception {
        // Tests validation of duplicate email addresses

        String existingEmail = "existing@fi.uba.ar";
        when(userRepository.findByEmail(existingEmail))
                .thenReturn(Optional.of(
                        new User("Existing", "User", 25, "M", "Address", null, "existing", "password", "ROLE_USER")));

        String duplicateEmailJson = """
                {
                    "firstName": "New",
                    "lastName": "User",
                    "age": 22,
                    "gender": "M",
                    "address": "west coast",
                    "base64Image": null,
                    "username": "newuser123",
                    "password": "password123",
                    "role": "ROLE_USER",
                    "email": "existing@fi.uba.ar"
                }
                """;
        when(userService.createUser(any(UserCreateDTO.class)))
                .thenReturn(Optional.empty());

        mockMvc.perform(post("/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(duplicateEmailJson))
                .andExpect(status().isConflict());
    }

    @Test
    void testServiceCreateUserWithAllExtendedFields() {
        // Tests that the service can create a user with all extended fields

        UserCreateDTO userData = new UserCreateDTO(
                "Juan", "Pérez", 25, "M", "Street 123, New York",
                "data:image/jpeg;base64,/9j/4AAQSkZJRgABAQEAYABgAAD",
                "juan123", "password123", "ROLE_USER", "juan@fi.uba.ar");
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());
        when(userRepository.findByUsername(anyString())).thenReturn(Optional.empty());
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            user.setId(1L);
            return user;
        });
        var jwtService = new JwtService("0".repeat(64), 1L);
        var refreshTokenService = new RefreshTokenService(1L, 20, mock());
        var realUserService = new UserService(
                jwtService,
                passwordEncoder,
                userRepository,
                refreshTokenService,
                verificationService);
        Optional<TokenDTO> result = realUserService.createUser(userData);
        assertTrue(result.isPresent());
        assertNotNull(result.get().accessToken());
        assertNotNull(result.get().refreshToken());
        verify(userRepository).save(argThat(user -> user.getFirstName().equals("Juan") &&
                user.getLastName().equals("Pérez") &&
                user.getAge() == 25 &&
                user.getGender().equals("M") &&
                user.getAddress().equals("Street 123, New York") &&
                user.getBase64Image() != null &&
                user.getUsername().equals("juan123") &&
                user.getEmail().equals("juan@fi.uba.ar")));
    }

    @Test
    void testServiceCreateUserWithDuplicateUsername() {
        // Tests that the service throws exception with duplicate username

        UserCreateDTO userData = new UserCreateDTO(
                "Juan", "Pérez", 25, "M", "Street 123",
                null, "existinguser", "password123", "ROLE_USER", "juan@fi.uba.ar");
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());
        when(userRepository.findByUsername("existinguser"))
                .thenReturn(Optional.of(new User("Existing", "User", 30, "F", "Address", null, "existinguser",
                        "password", "ROLE_USER")));
        var jwtService = new JwtService("0".repeat(64), 1L);
        var refreshTokenService = new RefreshTokenService(1L, 20, mock());
        var realUserService = new UserService(
                jwtService,
                passwordEncoder,
                userRepository,
                refreshTokenService,
                verificationService);
        assertThrows(org.springframework.web.server.ResponseStatusException.class, () -> {
            realUserService.createUser(userData);
        });
        verify(userRepository, never()).save(any(User.class));
    }

}
