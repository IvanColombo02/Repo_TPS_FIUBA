package ar.uba.fi.ingsoft1.product_example.config;

import static ar.uba.fi.ingsoft1.product_example.config.ConfigConstants.*;
import ar.uba.fi.ingsoft1.product_example.common.exception.ItemNotFoundException;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalControllerExceptionHandler {

    @ExceptionHandler(value = MethodArgumentNotValidException.class, produces = CONTENT_TYPE_TEXT_PLAIN)
    @ApiResponse(responseCode = RESPONSE_BAD_REQUEST, description = "Invalid arguments supplied", content = @Content(mediaType = CONTENT_TYPE_TEXT_PLAIN, schema = @Schema(implementation = String.class, example = "Validation failed because x, y, z")))
    public ResponseEntity<String> handleMethodArgumentInvalid(MethodArgumentNotValidException ex) {
        return new ResponseEntity<>(ex.getMessage(), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(value = ItemNotFoundException.class, produces = CONTENT_TYPE_TEXT_PLAIN)
    @ApiResponse(responseCode = RESPONSE_NOT_FOUND, description = "Referenced entity not found", content = @Content(mediaType = CONTENT_TYPE_TEXT_PLAIN, schema = @Schema(implementation = String.class, example = "Failed to find foo with id 42")))
    public ResponseEntity<String> handleItemNotFound(ItemNotFoundException ex) {
        return new ResponseEntity<>(ex.getMessage(), HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(AccessDeniedException.class)
    @ApiResponse(responseCode = RESPONSE_FORBIDDEN, description = "Invalid jwt access token supplied", content = @Content)
    public ResponseEntity<String> handleAccessDenied(AccessDeniedException ex) {
        return new ResponseEntity<>(ex.getMessage(), HttpStatus.FORBIDDEN);
    }

    @ExceptionHandler(ResponseStatusException.class)
    @ApiResponse(responseCode = RESPONSE_4XX, description = "Response status exception", content = @Content)
    public ResponseEntity<Map<String, String>> handleResponseStatusException(ResponseStatusException ex) {
        Map<String, String> error = new HashMap<>();
        String reason = ex.getReason();
        error.put(ERROR_MESSAGE_KEY, reason != null ? reason : ex.getStatusCode().toString());
        return new ResponseEntity<>(error, ex.getStatusCode());
    }
}