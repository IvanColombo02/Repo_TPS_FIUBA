package ar.uba.fi.ingsoft1.product_example.order;

/**
 * Exception thrown when there is insufficient stock to fulfill an order.
 */
public class InsufficientStockException extends RuntimeException {
    
    public InsufficientStockException(String message) {
        super(message);
    }
    
    public InsufficientStockException(String message, Throwable cause) {
        super(message, cause);
    }
}
