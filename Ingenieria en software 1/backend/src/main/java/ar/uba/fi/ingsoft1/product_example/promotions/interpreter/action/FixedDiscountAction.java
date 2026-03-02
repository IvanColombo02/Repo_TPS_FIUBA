package ar.uba.fi.ingsoft1.product_example.promotions.interpreter.action;

import static ar.uba.fi.ingsoft1.product_example.order.OrderConstants.DEFAULT_DISCOUNT;
import ar.uba.fi.ingsoft1.product_example.order.Order;
import ar.uba.fi.ingsoft1.product_example.order.OrderItem;
import ar.uba.fi.ingsoft1.product_example.promotions.interpreter.PromotionContext;
import lombok.RequiredArgsConstructor;

import java.util.List;

@RequiredArgsConstructor
public class FixedDiscountAction implements Action {
    private final String targetType; // "ORDER" o "ORDER_ITEM"
    private final Long targetItemId;
    private final String targetCategory;
    private final String targetProductType;
    private final double amount;

    @Override
    public void apply(PromotionContext context, Order order) {
        if ("ORDER".equals(targetType)) {
            applyToOrder(order);
        } else if ("ORDER_ITEM".equals(targetType)) {
            applyToItems(context, order);
        }
        order.calculateTotal();
    }

    private void applyToOrder(Order order) {
        List<OrderItem> items = order.getItems();
        if (items.isEmpty())
            return;

        double currentSubtotal = items.stream()
                .mapToDouble(item -> {
                    double itemTotal = item.getItemPrice() * item.getQuantity();
                    float itemDiscount = item.getDiscount() != null ? item.getDiscount() : DEFAULT_DISCOUNT;
                    return itemTotal - itemDiscount;
                })
                .sum();

        double remaining = Math.min(amount, currentSubtotal);
        if (remaining <= 0)
            return;

        for (OrderItem item : items) {
            if (remaining <= 0)
                break;

            double itemPrice = item.getItemPrice();
            int quantity = item.getQuantity();
            float itemDiscount = item.getDiscount() != null ? item.getDiscount() : 0f;
            double itemTotal = (itemPrice * quantity) - itemDiscount; 

            if (itemTotal <= 0)
                continue;

            double appliedDiscount = Math.min(itemTotal, remaining);
            if (appliedDiscount <= 0)
                continue;

            item.setDiscount(itemDiscount + (float) appliedDiscount);
            item.addPromotionApplied("Descuento fijo: $" + amount);
            item.calculateSubtotal();

            remaining -= appliedDiscount;
        }
    }

    private void applyToItems(PromotionContext context, Order order) {
        List<OrderItem> targetItems;
       // TODO: strategy 
        if (targetItemId != null) {
            targetItems = context.getItemsByProductId(targetItemId);
        } else if (targetCategory != null) {
            targetItems = context.getItemsByCategory(targetCategory);
        } else if (targetProductType != null) {
            targetItems = context.getItemsByType(targetProductType);
        } else {
            targetItems = order.getItems();
        }

        for (OrderItem item : targetItems) {
            double itemPrice = item.getItemPrice();
            float itemDiscount = item.getDiscount() != null ? item.getDiscount() : 0f;
            double currentPrice = itemPrice - (itemDiscount / item.getQuantity());

            double perUnitDiscount = Math.min(amount, currentPrice);
            if (perUnitDiscount <= 0)
                continue;

            double discountAmount = perUnitDiscount * item.getQuantity();

            item.setDiscount(itemDiscount + (float) discountAmount);
            item.addPromotionApplied("Descuento fijo: $" + amount);
            item.calculateSubtotal();
        }
    }
}
