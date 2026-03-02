package ar.uba.fi.ingsoft1.product_example.config;

import ar.uba.fi.ingsoft1.product_example.common.exception.ItemNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

class GlobalControllerExceptionHandlerTest {

    private GlobalControllerExceptionHandler handler;

    @BeforeEach
    void setUp() {
        handler = new GlobalControllerExceptionHandler();
    }

    @Test
    void handleItemNotFoundReturnsNotFoundWithCorrectMessage() {
        ItemNotFoundException ex = new ItemNotFoundException("Product", 123L);
        ResponseEntity<String> response = handler.handleItemNotFound(ex);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Failed to find Product with id 123", response.getBody());
    }

    @Test
    void handleAccessDeniedReturnsForbiddenWithCorrectMessage() {
        String errorMessage = "You do not have permission to access this resource";
        AccessDeniedException ex = new AccessDeniedException(errorMessage);
        ResponseEntity<String> response = handler.handleAccessDenied(ex);

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(errorMessage, response.getBody());
    }

    @ParameterizedTest
    @MethodSource("responseStatusExceptionTestCases")
    void handleResponseStatusException(HttpStatus status, String reason, String expectedMessage) {
        ResponseStatusException ex = new ResponseStatusException(status, reason);
        ResponseEntity<Map<String, String>> response = handler.handleResponseStatusException(ex);

        assertEquals(status, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().containsKey("message"));
        assertEquals(expectedMessage, response.getBody().get("message"));
    }

    private static Stream<Arguments> responseStatusExceptionTestCases() {
        return Stream.of(
                Arguments.of(HttpStatus.NOT_FOUND, "Resource not found", "Resource not found"),
                Arguments.of(HttpStatus.BAD_REQUEST, null, HttpStatus.BAD_REQUEST.toString()),
                Arguments.of(HttpStatus.INTERNAL_SERVER_ERROR, null, HttpStatus.INTERNAL_SERVER_ERROR.toString()),
                Arguments.of(HttpStatus.BAD_REQUEST, "Test reason", "Test reason"),
                Arguments.of(HttpStatus.UNAUTHORIZED, "Test reason", "Test reason"),
                Arguments.of(HttpStatus.FORBIDDEN, "Test reason", "Test reason"),
                Arguments.of(HttpStatus.NOT_FOUND, "Test reason", "Test reason"),
                Arguments.of(HttpStatus.CONFLICT, "Test reason", "Test reason"));
    }
}
