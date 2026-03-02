package ar.uba.fi.ingsoft1.product_example.promotions.interpreter.expression;

import ar.uba.fi.ingsoft1.product_example.promotions.interpreter.action.Action;
import ar.uba.fi.ingsoft1.product_example.promotions.interpreter.PromotionContext;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class IfExpression implements Expression {
    private final Expression condition;
    private final Action action;

    @Override
    public boolean interpret(PromotionContext context) {
        if (condition.interpret(context)) {
            action.apply(context, context.getOrder());
            return true;
        }
        return false;
    }
}
