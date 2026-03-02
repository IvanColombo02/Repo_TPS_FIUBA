package ar.uba.fi.ingsoft1.product_example.promotions.interpreter.expression;

import ar.uba.fi.ingsoft1.product_example.order.OrderItem;
import ar.uba.fi.ingsoft1.product_example.promotions.interpreter.PromotionContext;
import lombok.RequiredArgsConstructor;

import java.util.List;

@RequiredArgsConstructor
public class ProductCategoryExpression implements Expression {
    private final String filterValue;

    @Override
    public boolean interpret(PromotionContext context) {
        List<OrderItem> items = context.getItems();
        return items.stream().anyMatch(item -> {
                return item.getItemCategories() != null &&
                        item.getItemCategories().contains(filterValue);
        });
    }
}
