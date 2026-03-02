package ar.uba.fi.ingsoft1.product_example.promotions.interpreter.expression;

import ar.uba.fi.ingsoft1.product_example.promotions.interpreter.PromotionContext;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class AndExpression implements Expression {
    private final Expression left;
    private final Expression right;

    @Override
    public boolean interpret(PromotionContext context) {
        return left.interpret(context) && right.interpret(context);
    }
}
