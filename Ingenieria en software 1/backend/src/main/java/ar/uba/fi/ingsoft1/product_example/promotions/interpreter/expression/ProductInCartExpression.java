package ar.uba.fi.ingsoft1.product_example.promotions.interpreter.expression;

import ar.uba.fi.ingsoft1.product_example.promotions.interpreter.PromotionContext;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class ProductInCartExpression implements Expression {
    private final long productId;

    @Override
    public boolean interpret(PromotionContext context) {
        return context.hasItem(productId);
    }
}
