package ar.uba.fi.ingsoft1.product_example.user;

import ar.uba.fi.ingsoft1.product_example.config.security.JwtService;
import ar.uba.fi.ingsoft1.product_example.config.security.JwtUserDetails;
import ar.uba.fi.ingsoft1.product_example.user.refresh_token.RefreshToken;
import ar.uba.fi.ingsoft1.product_example.user.refresh_token.RefreshTokenService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import java.security.Principal;
import java.util.Optional;

@Service
@Transactional
public class UserService implements UserDetailsService {

    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;
    private final RefreshTokenService refreshTokenService;
    private VerificationService verificationService;

    @Autowired
    UserService(
            JwtService jwtService,
            PasswordEncoder passwordEncoder,
            UserRepository userRepository,
            RefreshTokenService refreshTokenService,
            VerificationService verificationService) {
        this.jwtService = jwtService;
        this.passwordEncoder = passwordEncoder;
        this.userRepository = userRepository;
        this.refreshTokenService = refreshTokenService;
        this.verificationService = verificationService;
    }

    UserService(
            JwtService jwtService,
            PasswordEncoder passwordEncoder,
            UserRepository userRepository,
            RefreshTokenService refreshTokenService) {
        this(jwtService, passwordEncoder, userRepository, refreshTokenService, null);
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return userRepository
                .findByUsername(username)
                .orElseThrow(() -> {
                    var msg = String.format("Username '%s' not found", username);
                    return new UsernameNotFoundException(msg);
                });
    }

    Optional<TokenDTO> createUser(UserCreateDTO data) {
        String emailLower = data.email().toLowerCase();
        if (!emailLower.endsWith("@fi.uba.ar")) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El email debe ser @fi.uba.ar");
        }
        if (userRepository.findByUsername(data.username()).isPresent()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "El nombre de usuario ya existe");
        }
        if (userRepository.findByEmail(data.email()).isPresent()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "El email ya está registrado");
        }
        var user = data.asUser(passwordEncoder::encode);
        user.setEmail(data.email());
        user.setEmailVerified(false);
        userRepository.save(user);
        if (verificationService != null) {
            verificationService.sendVerificationEmail(user);
        }
        return Optional.of(generateTokens(user));
    }

    Optional<TokenDTO> loginUser(UserCredentials data) {
        return userRepository.findByUsername(data.username())
                .filter(user -> passwordEncoder.matches(data.password(), user.getPassword()))
                .filter(User::isEmailVerified)
                .map(this::generateTokens);
    }

    TokenDTO loginUserOrThrow(UserCredentials data) {
        var maybeUser = userRepository.findByUsername(data.username())
                .or(() -> userRepository.findByEmail(data.username()));

        if (maybeUser.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Usuario o email inexistente");
        }
        var user = maybeUser.get();
        if (!passwordEncoder.matches(data.password(), user.getPassword())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Contraseña incorrecta");
        }
        if (!user.isEmailVerified()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "EMAIL_NOT_VERIFIED");
        }
        return generateTokens(user);
    }

    Optional<TokenDTO> refresh(RefreshDTO data) {
        return refreshTokenService.findByValue(data.refreshToken())
                .map(RefreshToken::user)
                .map(this::generateTokens);
    }

    private TokenDTO generateTokens(User user) {
        String accessToken = jwtService.createToken(new JwtUserDetails(
                user.getUsername(),
                user.getRole()));
        RefreshToken refreshToken = refreshTokenService.createFor(user);
        return new TokenDTO(accessToken, refreshToken.value());
    }

    public Optional<UserDTO> getByUsername(Principal principal) {
        return userRepository.findByUsername(principal.getName()).map(userRepository::save).map(UserDTO::new);
    }

    public Optional<UserDTO> updateMe(Principal principal, UserUpdateDTO data) {
        return userRepository.findByUsername(principal.getName()).map(data::applyTo).map(userRepository::save)
                .map(UserDTO::new);
    }

    /**
     * Finds a user by ID.
     * Used by OrderService to validate user exists when creating orders.
     */
    public Optional<User> findById(Long userId) {
        return userRepository.findById(userId);
    }

    /**
     * Finds a user by username.
     */
    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    public java.util.List<UserDTO> getAllUsers() {
        return userRepository.findAll().stream().map(UserDTO::new).toList();
    }

    public Optional<UserDTO> updateUserRole(Long id, String role) {
        return userRepository.findById(id)
                .map(user -> {
                    user.setRole(role);
                    return userRepository.save(user);
                })
                .map(UserDTO::new);
    }
}
