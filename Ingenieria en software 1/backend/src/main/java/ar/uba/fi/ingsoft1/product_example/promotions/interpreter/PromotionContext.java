package ar.uba.fi.ingsoft1.product_example.promotions.interpreter;

import ar.uba.fi.ingsoft1.product_example.order.Order;
import ar.uba.fi.ingsoft1.product_example.order.OrderItem;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.List;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Getter
public class PromotionContext {
    private static final ZoneId ARGENTINA_ZONE = ZoneId.of("America/Argentina/Buenos_Aires");
    
    private final Order order;
    private final LocalDate currentDate;
    private final LocalTime time;

    public PromotionContext(Order order) {
        this.order = order;
        this.time = LocalTime.now(ARGENTINA_ZONE);
        this.currentDate = LocalDate.now(ARGENTINA_ZONE);
    }

    public double getTotalAmount() {
        return order.getItems().stream()
                .mapToDouble(OrderItem::getSubtotal)
                .sum();
    }

    public List<OrderItem> getItems() {
        return order.getItems();
    }

    public boolean hasItem(Long productId) {
        return order.getItems().stream()
                .anyMatch(item -> item.getComponent().getId().equals(productId));
    }

    public boolean hasItemByName(String productName) {
        return order.getItems().stream()
                .anyMatch(item -> item.getItemName().equalsIgnoreCase(productName));
    }

    public int getItemQuantity(Long productId) {
        return order.getItems().stream()
                .filter(item -> item.getComponent().getId().equals(productId))
                .mapToInt(OrderItem::getQuantity)
                .sum();
    }

    public List<OrderItem> getItemsByCategory(String category) {
        return order.getItems().stream()
                .filter(item -> item.getItemCategories() != null &&
                        item.getItemCategories().contains(category))
                .collect(Collectors.toList());
    }

    public List<OrderItem> getItemsByType(String type) {
        return order.getItems().stream()
                .filter(item -> item.getItemTypes() != null &&
                        item.getItemTypes().contains(type))
                .collect(Collectors.toList());
    }

    public List<OrderItem> getItemsByProductId(Long productId) {
        return order.getItems().stream()
                .filter(item -> item.getComponent().getId().equals(productId))
                .collect(Collectors.toList());
    }
}
