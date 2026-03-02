package ar.uba.fi.ingsoft1.product_example.order;

import ar.uba.fi.ingsoft1.product_example.items.Component;
import ar.uba.fi.ingsoft1.product_example.user.User;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;


@Entity
@Table(name = "orders")
@Data
@NoArgsConstructor
public class Order {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @JsonManagedReference
    private List<OrderItem> items = new ArrayList<>();
    

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private OrderStatus status;
    

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private PaymentMethod paymentMethod;
    

    @Column(nullable = false)
    private float totalPrice;
    

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
    

    @Column(nullable = false)
    private LocalDateTime updatedAt;
    

    
    /**
     * Adds an item to the order.
     */
    public void addItem(Component component, int quantity) {
        OrderItem item = new OrderItem(this, component, quantity);
        items.add(item);
        calculateTotal();
    }
    
    /**
     * Removes an item from the order.
     */
    public void removeItem(OrderItem item) {
        items.remove(item);
        item.setOrder(null);
        calculateTotal();
    }
    
    /**
     * Calculates the total price of the order.
     */
    public void calculateTotal() {
        this.totalPrice = items.stream()
            .map(OrderItem::getSubtotal)
            .reduce(0f, Float::sum);
    }
    
    /**
     * Checks if the order can be cancelled.
     */
    public boolean canBeCancelled() {
        return this.status == OrderStatus.PENDING;
    }
    
    /**
     * Cancels the order.
     */
    public void cancel() {
        if (!canBeCancelled()) {
            throw new IllegalStateException(
                "Cannot cancel order in status: " + this.status + 
                ". Only PENDING orders can be cancelled."
            );
        }
        this.status = OrderStatus.CANCELLED;
    }
    
    /**
     * Gets the user ID associated with this order.
     * Delegates to User to maintain encapsulation.
     */
    public Long getUserId() {
        return user != null ? user.getId() : null;
    }
    
    /**
     * Gets the user email associated with this order.
     * Delegates to User to maintain encapsulation.
     */
    public String getUserEmail() {
        return user != null ? user.getEmail() : null;
    }
    
    
    /**
     * Lifecycle callback before insert.
     */
    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        this.createdAt = now;
        this.updatedAt = now;
        calculateTotal();
    }
    
    /**
     * Lifecycle callback before update.
     */
    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
