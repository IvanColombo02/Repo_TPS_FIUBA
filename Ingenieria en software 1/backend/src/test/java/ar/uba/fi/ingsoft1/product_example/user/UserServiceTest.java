package ar.uba.fi.ingsoft1.product_example.user;

import ar.uba.fi.ingsoft1.product_example.config.security.JwtService;
import ar.uba.fi.ingsoft1.product_example.user.refresh_token.RefreshTokenService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.security.Principal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class UserServiceTest {

    private UserService userService;

    private static final String USERNAME = "user";
    private static final String PASSWORD = "password";

    @BeforeEach
    void setup() {
        var passwordEncoder = new BCryptPasswordEncoder();
        var passwordHash = passwordEncoder.encode(PASSWORD);

        UserRepository userRepository = mock();
        when(userRepository.findByUsername(anyString()))
                .thenReturn(Optional.empty());
        var user = new User("Test", "User", 25, "male", "Test Address", null, USERNAME, passwordHash, "ROLE_USER");
        user.setEmail(USERNAME + "@fi.uba.ar");
        user.setEmailVerified(true); // verified email true to pass the test without n8n
        when(userRepository.findByUsername(USERNAME))
                .thenReturn(Optional.of(user));
        when(userRepository.findByEmail(anyString()))
                .thenReturn(Optional.empty());
        when(userRepository.findByEmail(USERNAME + "@fi.uba.ar"))
                .thenReturn(Optional.of(user));

        var key = "0".repeat(64);
        userService = new UserService(
                new JwtService(key, 1L),
                new BCryptPasswordEncoder(),
                userRepository,
                new RefreshTokenService(1L, 20, mock()),
                null
        );
    }

    @Test
    void loginUser() {
        var response = userService.loginUser(new UserLoginDTO(USERNAME, PASSWORD));
        assertNotNull(response.orElseThrow());
    }

    @Test
    void loginWithWrongPassword() {
        var response = userService.loginUser(new UserLoginDTO(USERNAME, PASSWORD + "_wrong"));
        assertEquals(Optional.empty(), response);
    }

    @Test
    void loginNonexistentUser() {
        var response = userService.loginUser(new UserLoginDTO(USERNAME + "_wrong", PASSWORD));
        assertEquals(Optional.empty(), response);
    }

    @Test
    void loadUserByUsernameThrowsExceptionWhenNotFound() {
        // Tests that loadUserByUsername throws UsernameNotFoundException when user doesn't exist
        assertThrows(UsernameNotFoundException.class, () -> {
            userService.loadUserByUsername("nonexistent");
        });
    }

    @Test
    void getByUsernameReturnsUserDTOWhenUserExists() {
        // Tests that getByUsername returns UserDTO when user exists
        Principal principal = mock(Principal.class);
        when(principal.getName()).thenReturn(USERNAME);

        UserRepository userRepository = mock();
        User user = new User("Test", "User", 25, "male", "Test Address", null, USERNAME, "password", "ROLE_USER");
        user.setEmail(USERNAME + "@fi.uba.ar");
        when(userRepository.findByUsername(USERNAME)).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var key = "0".repeat(64);
        var service = new UserService(
                new JwtService(key, 1L),
                new BCryptPasswordEncoder(),
                userRepository,
                new RefreshTokenService(1L, 20, mock()),
                null
        );

        Optional<UserDTO> result = service.getByUsername(principal);
        assertTrue(result.isPresent());
        assertEquals(USERNAME, result.get().username());
    }

    @Test
    void getByUsernameReturnsEmptyWhenUserDoesNotExist() {
        // Tests that getByUsername returns empty when user doesn't exist
        Principal principal = mock(Principal.class);
        when(principal.getName()).thenReturn("nonexistent");

        UserRepository userRepository = mock();
        when(userRepository.findByUsername("nonexistent")).thenReturn(Optional.empty());

        var key = "0".repeat(64);
        var service = new UserService(
                new JwtService(key, 1L),
                new BCryptPasswordEncoder(),
                userRepository,
                new RefreshTokenService(1L, 20, mock()),
                null
        );

        Optional<UserDTO> result = service.getByUsername(principal);
        assertTrue(result.isEmpty());
    }

    @Test
    void updateMeReturnsUserDTOWhenUserExists() {
        // Tests that updateMe returns UserDTO when user exists and updates are applied
        Principal principal = mock(Principal.class);
        when(principal.getName()).thenReturn(USERNAME);

        UserRepository userRepository = mock();
        User user = new User("Test", "User", 25, "male", "Test Address", null, USERNAME, "password", "ROLE_USER");
        user.setEmail(USERNAME + "@fi.uba.ar");
        when(userRepository.findByUsername(USERNAME)).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var key = "0".repeat(64);
        var service = new UserService(
                new JwtService(key, 1L),
                new BCryptPasswordEncoder(),
                userRepository,
                new RefreshTokenService(1L, 20, mock()),
                null
        );

        UserUpdateDTO updateDTO = new UserUpdateDTO(
                Optional.of("Updated"),
                Optional.empty(),
                Optional.empty(),
                Optional.empty(),
                Optional.empty(),
                null,
                Optional.empty(),
                Optional.empty()
        );

        Optional<UserDTO> result = service.updateMe(principal, updateDTO);
        assertTrue(result.isPresent());
        assertEquals("Updated", result.get().firstName());
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void updateMeReturnsEmptyWhenUserDoesNotExist() {
        // Tests that updateMe returns empty when user doesn't exist
        Principal principal = mock(Principal.class);
        when(principal.getName()).thenReturn("nonexistent");

        UserRepository userRepository = mock();
        when(userRepository.findByUsername("nonexistent")).thenReturn(Optional.empty());

        var key = "0".repeat(64);
        var service = new UserService(
                new JwtService(key, 1L),
                new BCryptPasswordEncoder(),
                userRepository,
                new RefreshTokenService(1L, 20, mock()),
                null
        );

        UserUpdateDTO updateDTO = new UserUpdateDTO(
                Optional.of("Updated"),
                Optional.empty(),
                Optional.empty(),
                Optional.empty(),
                Optional.empty(),
                null,
                Optional.empty(),
                Optional.empty()
        );

        Optional<UserDTO> result = service.updateMe(principal, updateDTO);
        assertTrue(result.isEmpty());
    }

    @Test
    void findByIdReturnsUserWhenExists() {
        // Tests that findById returns user when it exists
        UserRepository userRepository = mock();
        User user = new User("Test", "User", 25, "male", "Test Address", null, USERNAME, "password", "ROLE_USER");
        user.setId(1L);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        var key = "0".repeat(64);
        var service = new UserService(
                new JwtService(key, 1L),
                new BCryptPasswordEncoder(),
                userRepository,
                new RefreshTokenService(1L, 20, mock()),
                null
        );

        Optional<User> result = service.findById(1L);
        assertTrue(result.isPresent());
        assertEquals(1L, result.get().getId());
    }

    @Test
    void findByIdReturnsEmptyWhenUserDoesNotExist() {
        // Tests that findById returns empty when user doesn't exist
        UserRepository userRepository = mock();
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        var key = "0".repeat(64);
        var service = new UserService(
                new JwtService(key, 1L),
                new BCryptPasswordEncoder(),
                userRepository,
                new RefreshTokenService(1L, 20, mock()),
                null
        );

        Optional<User> result = service.findById(999L);
        assertTrue(result.isEmpty());
    }

    @Test
    void findByUsernameReturnsUserWhenExists() {
        // Tests that findByUsername returns user when it exists
        UserRepository userRepository = mock();
        User user = new User("Test", "User", 25, "male", "Test Address", null, USERNAME, "password", "ROLE_USER");
        when(userRepository.findByUsername(USERNAME)).thenReturn(Optional.of(user));

        var key = "0".repeat(64);
        var service = new UserService(
                new JwtService(key, 1L),
                new BCryptPasswordEncoder(),
                userRepository,
                new RefreshTokenService(1L, 20, mock()),
                null
        );

        Optional<User> result = service.findByUsername(USERNAME);
        assertTrue(result.isPresent());
        assertEquals(USERNAME, result.get().getUsername());
    }

    @Test
    void getAllUsersReturnsListOfUserDTOs() {
        // Tests that getAllUsers returns a list of all users as UserDTOs
        UserRepository userRepository = mock();
        User user1 = new User("Test1", "User1", 25, "M", "Address1", null, "user1", "password", "ROLE_USER");
        User user2 = new User("Test2", "User2", 30, "F", "Address2", null, "user2", "password", "ROLE_USER");
        when(userRepository.findAll()).thenReturn(List.of(user1, user2));

        var key = "0".repeat(64);
        var service = new UserService(
                new JwtService(key, 1L),
                new BCryptPasswordEncoder(),
                userRepository,
                new RefreshTokenService(1L, 20, mock()),
                null
        );

        List<UserDTO> result = service.getAllUsers();
        assertEquals(2, result.size());
        assertEquals("user1", result.get(0).username());
        assertEquals("user2", result.get(1).username());
    }
}