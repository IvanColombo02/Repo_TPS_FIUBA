package ar.uba.fi.ingsoft1.product_example.promotions.interpreter.action;

import ar.uba.fi.ingsoft1.product_example.order.Order;
import ar.uba.fi.ingsoft1.product_example.promotions.interpreter.PromotionContext;

public interface Action {
    void apply(PromotionContext context, Order order);
}
