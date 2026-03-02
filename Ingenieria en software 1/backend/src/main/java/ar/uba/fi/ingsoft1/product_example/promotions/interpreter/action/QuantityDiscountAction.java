package ar.uba.fi.ingsoft1.product_example.promotions.interpreter.action;

import static ar.uba.fi.ingsoft1.product_example.order.OrderConstants.DEFAULT_DISCOUNT;
import ar.uba.fi.ingsoft1.product_example.order.Order;
import ar.uba.fi.ingsoft1.product_example.order.OrderItem;
import ar.uba.fi.ingsoft1.product_example.promotions.interpreter.PromotionContext;
import lombok.RequiredArgsConstructor;

import java.util.List;

@RequiredArgsConstructor
public class QuantityDiscountAction implements Action {
    private final int buyQuantity;
    private final int payQuantity;

    @Override
    public void apply(PromotionContext context, Order order) {
        List<OrderItem> items = order.getItems();

        for (OrderItem item : items) {
            int quantity = item.getQuantity();
            int freeItems = (quantity / buyQuantity) * (buyQuantity - payQuantity);

            if (freeItems > 0) {
                double discountPerItem = item.getItemPrice();
                double totalDiscount = discountPerItem * freeItems;

                float currentDiscount = item.getDiscount() != null ? item.getDiscount() : DEFAULT_DISCOUNT;
                item.setDiscount(currentDiscount + (float) totalDiscount);
                item.addPromotionApplied("Lleva " + buyQuantity + " paga " + payQuantity);
                item.calculateSubtotal();
            }
        }

        order.calculateTotal();
    }
}
