package ar.uba.fi.ingsoft1.product_example.config.security;

import static ar.uba.fi.ingsoft1.product_example.config.ConfigConstants.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableWebSecurity(debug = false)
public class SecurityConfig {

    public static final String[] PUBLIC_ENDPOINTS = { 
            USERS_ENDPOINT, 
            USERS_FORGOT_PASSWORD_ENDPOINT, 
            USERS_RESET_PASSWORD_ENDPOINT,
            USERS_VERIFY_ENDPOINT, 
            SESSIONS_ENDPOINT 
    };

    private final JwtAuthFilter authFilter;

    @Autowired
    SecurityConfig(JwtAuthFilter authFilter) {
        this.authFilter = authFilter;
    }

    @Bean
    AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(ERROR_ENDPOINT).permitAll()
                        .requestMatchers(SWAGGER_UI_ENDPOINT).permitAll()
                        .requestMatchers(API_DOCS_ENDPOINT).permitAll()
                        .requestMatchers(PUBLIC_ENDPOINTS).permitAll()
                        .requestMatchers(HttpMethod.POST, "/products").hasRole(ADMIN_ROLE)
                        .requestMatchers(HttpMethod.PATCH, "/products/**").hasRole(ADMIN_ROLE)
                        .requestMatchers(HttpMethod.DELETE, "/products/**").hasRole(ADMIN_ROLE)
                        .requestMatchers(HttpMethod.GET, "/products/**").permitAll()

                        .requestMatchers(HttpMethod.POST, "/ingredients").hasRole(ADMIN_ROLE)
                        .requestMatchers(HttpMethod.PATCH, "/ingredients/**").hasRole(ADMIN_ROLE)
                        .requestMatchers(HttpMethod.DELETE, "/ingredients/**").hasRole(ADMIN_ROLE)
                        .requestMatchers(HttpMethod.GET, "/ingredients/**").hasRole(ADMIN_ROLE)

                        .requestMatchers(HttpMethod.POST, "/combos").hasRole(ADMIN_ROLE)
                        .requestMatchers(HttpMethod.PATCH, "/combos/**").hasRole(ADMIN_ROLE)
                        .requestMatchers(HttpMethod.DELETE, "/combos/**").hasRole(ADMIN_ROLE)
                        .requestMatchers(HttpMethod.GET, "/combos/**").permitAll()

                        .requestMatchers(HttpMethod.POST, "/promotions").hasRole(ADMIN_ROLE)
                        .requestMatchers(HttpMethod.PUT, "/promotions/priorities").hasRole(ADMIN_ROLE)
                        .requestMatchers(HttpMethod.PATCH, "/promotions/**").hasRole(ADMIN_ROLE)
                        .requestMatchers(HttpMethod.DELETE, "/promotions/**").hasRole(ADMIN_ROLE)
                        .requestMatchers(HttpMethod.GET, "/promotions/active").hasAnyRole(USER_ROLE, ADMIN_ROLE, EMPLOYEE_ROLE)
                        .requestMatchers(HttpMethod.GET, "/promotions/**").hasRole(ADMIN_ROLE)

                        .requestMatchers(HttpMethod.GET, "/orders/active").hasAnyRole(ADMIN_ROLE, EMPLOYEE_ROLE)
                        .requestMatchers(HttpMethod.GET, "/orders/status/**").hasAnyRole(ADMIN_ROLE, EMPLOYEE_ROLE)
                        .requestMatchers(HttpMethod.PATCH, "/orders/*/status").hasAnyRole(ADMIN_ROLE, EMPLOYEE_ROLE)
                        .requestMatchers(HttpMethod.POST, "/orders/calculate-discounts")
                        .hasAnyRole(USER_ROLE, ADMIN_ROLE, EMPLOYEE_ROLE)

                        .requestMatchers(HttpMethod.POST, "/orders").hasAnyRole(USER_ROLE, ADMIN_ROLE, EMPLOYEE_ROLE)
                        .requestMatchers(HttpMethod.GET, "/orders").hasAnyRole(USER_ROLE, ADMIN_ROLE, EMPLOYEE_ROLE)
                        .requestMatchers(HttpMethod.GET, "/orders/*").hasAnyRole(USER_ROLE, ADMIN_ROLE, EMPLOYEE_ROLE)
                        .requestMatchers(HttpMethod.DELETE, "/orders/*").hasAnyRole(USER_ROLE, ADMIN_ROLE, EMPLOYEE_ROLE)

                        .requestMatchers(HttpMethod.GET, "/users").hasRole(ADMIN_ROLE)
                        .requestMatchers(HttpMethod.PATCH, "/users/*/role").hasRole(ADMIN_ROLE)
                        .requestMatchers("/users/me").authenticated()
                        .requestMatchers("/products/**").authenticated()
                        .requestMatchers("/ingredients/**").authenticated()
                        .requestMatchers("/combos/**").authenticated()
                        .requestMatchers("/promotions/**").authenticated()
                        .anyRequest().denyAll())
                .sessionManagement(sessionManager -> sessionManager
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .addFilterBefore(authFilter, UsernamePasswordAuthenticationFilter.class)
                .build();
    }

    @Bean
    UrlBasedCorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(List.of(CORS_ALL_ORIGINS));
        configuration.setAllowedMethods(List.of(CORS_ALL_METHODS));
        configuration.setAllowedHeaders(List.of(CORS_ALL_HEADERS));
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration(ALL_PATHS, configuration);
        return source;
    }

    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
