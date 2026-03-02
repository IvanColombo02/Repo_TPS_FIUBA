package ar.uba.fi.ingsoft1.product_example.promotions.interpreter.expression;

import ar.uba.fi.ingsoft1.product_example.promotions.interpreter.PromotionContext;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class ProductNameExpression implements Expression {
    private final String productName;

    @Override
    public boolean interpret(PromotionContext context) {
        return context.hasItemByName(productName);
    }
}
