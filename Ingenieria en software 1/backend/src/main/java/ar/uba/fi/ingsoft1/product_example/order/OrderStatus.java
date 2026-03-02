package ar.uba.fi.ingsoft1.product_example.order;

/**
 * Possible states of an order in the cafeteria system.
 * Flow: PENDING → IN_PREPARATION → READY → DELIVERED or CANCELLED
 */
public enum OrderStatus {
    PENDING,
    
    IN_PREPARATION,
    
    READY,
    
    DELIVERED,
    
    CANCELLED
}
