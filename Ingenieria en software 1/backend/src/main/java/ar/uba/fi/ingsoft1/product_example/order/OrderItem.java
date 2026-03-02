package ar.uba.fi.ingsoft1.product_example.order;

import static ar.uba.fi.ingsoft1.product_example.order.OrderConstants.DEFAULT_DISCOUNT;
import ar.uba.fi.ingsoft1.product_example.items.Component;
import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "order_items")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    @JsonBackReference
    private Order order;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "component_id", nullable = true)
    private Component component;

    @Column(nullable = false, length = 255)
    private String itemName;

    @Column(nullable = false)
    private float itemPrice;

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "order_item_categories", foreignKey = @ForeignKey(foreignKeyDefinition = "FOREIGN KEY (order_item_id) REFERENCES order_items(id) ON DELETE CASCADE"))
    @Column(name = "category")
    private List<String> itemCategories;

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "order_item_types", foreignKey = @ForeignKey(foreignKeyDefinition = "FOREIGN KEY (order_item_id) REFERENCES order_items(id) ON DELETE CASCADE"))
    @Column(name = "type")
    private List<String> itemTypes;

    @Column(nullable = false)
    private int quantity;

    @Column(nullable = false)
    private float subtotal;

    @Column(nullable = true)
    private Float discount;

    @Column(nullable = true, length = 255)
    private String promotionApplied;

    public OrderItem(Order order, Component component, int quantity) {
        this.order = order;
        this.component = component;
        this.quantity = quantity;

        this.itemName = component.getName();
        this.itemPrice = component.getPrice();
        this.itemCategories = new ArrayList<>(component.getCategories());
        this.itemTypes = new ArrayList<>(component.getTypes());

        calculateSubtotal();
    }

    @PrePersist
    @PreUpdate
    public void calculateSubtotal() {
        float discountAmount = (discount != null) ? discount : DEFAULT_DISCOUNT;
        this.subtotal = (itemPrice * quantity) - discountAmount;
    }

    public void addPromotionApplied(String promotionText) {
        if (promotionText == null || promotionText.trim().isEmpty()) {
            return;
        }
        String currentPromotion = this.promotionApplied;
        if (currentPromotion != null && !currentPromotion.trim().isEmpty()) {
            this.promotionApplied = currentPromotion + "; " + promotionText;
        } else {
            this.promotionApplied = promotionText;
        }
    }

}
