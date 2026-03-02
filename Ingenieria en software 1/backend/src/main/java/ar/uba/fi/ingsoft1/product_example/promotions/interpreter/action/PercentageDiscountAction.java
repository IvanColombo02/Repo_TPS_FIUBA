package ar.uba.fi.ingsoft1.product_example.promotions.interpreter.action;

import static ar.uba.fi.ingsoft1.product_example.order.OrderConstants.DEFAULT_DISCOUNT;
import static ar.uba.fi.ingsoft1.product_example.promotions.PromotionConstants.PERCENTAGE_DIVISOR;
import ar.uba.fi.ingsoft1.product_example.order.Order;
import ar.uba.fi.ingsoft1.product_example.order.OrderItem;
import ar.uba.fi.ingsoft1.product_example.promotions.interpreter.PromotionContext;
import lombok.RequiredArgsConstructor;

import java.util.List;

@RequiredArgsConstructor
public class PercentageDiscountAction implements Action {
    private final String targetType; // "ORDER" or "ORDER_ITEM"
    private final Long targetItemId;
    private final String targetCategory;
    private final String targetProductType;
    private final double percentage;

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

        double subtotalBefore = items.stream()
                .mapToDouble(item -> {
                    double itemPrice = item.getItemPrice();
                    int quantity = item.getQuantity();
                    float discount = item.getDiscount() != null ? item.getDiscount() : DEFAULT_DISCOUNT;
                    return (itemPrice * quantity) - discount;
                })
                .sum();

        double totalDiscountGoal = (subtotalBefore * percentage) / PERCENTAGE_DIVISOR;
        if (totalDiscountGoal <= 0 || subtotalBefore <= 0)
            return;

        double distributed = 0;
        for (int index = 0; index < items.size(); index++) {
            OrderItem item = items.get(index);

            if (distributed >= totalDiscountGoal)
                break;

            double itemPrice = item.getItemPrice();
            int quantity = item.getQuantity();
            float itemDiscount = item.getDiscount() != null ? item.getDiscount() : 0f;
            double itemTotal = (itemPrice * quantity) - itemDiscount;

            if (itemTotal <= 0)
                continue;

            double itemDiscountAmount;
            if (index == items.size() - 1) {
                itemDiscountAmount = totalDiscountGoal - distributed;
            } else {
                itemDiscountAmount = (itemTotal / subtotalBefore) * totalDiscountGoal;
                distributed += itemDiscountAmount;
            }

            item.setDiscount(itemDiscount + (float) itemDiscountAmount);
            item.addPromotionApplied("Descuento " + percentage + "%");
            item.calculateSubtotal();
        }
    }

    private void applyToItems(PromotionContext context, Order order) {
        List<OrderItem> targetItems;

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
            int quantity = item.getQuantity();
            float itemDiscount = item.getDiscount() != null ? item.getDiscount() : 0f;
            double currentPrice = itemPrice - (itemDiscount / quantity);

            double perUnitDiscount = currentPrice * (percentage / PERCENTAGE_DIVISOR);
            if (perUnitDiscount <= 0)
                continue;

            double discountAmount = perUnitDiscount * quantity;

            item.setDiscount(itemDiscount + (float) discountAmount);
            item.addPromotionApplied("Descuento " + percentage + "%");
            item.calculateSubtotal();
        }
    }
}
