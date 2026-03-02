package ar.uba.fi.ingsoft1.product_example.promotions.interpreter.expression;

import ar.uba.fi.ingsoft1.product_example.promotions.interpreter.PromotionContext;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class QuantityExpression implements Expression {
    private final long productId;
    private final int minQuantity;

    @Override
    public boolean interpret(PromotionContext context) {
        int quantity = context.getItemQuantity(productId);
        return quantity >= minQuantity;
    }
}
