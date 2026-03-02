package ar.uba.fi.ingsoft1.product_example.promotions.interpreter.expression;

import static ar.uba.fi.ingsoft1.product_example.promotions.PromotionConstants.FLOATING_POINT_TOLERANCE;
import ar.uba.fi.ingsoft1.product_example.promotions.interpreter.PromotionContext;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class TotalAmountExpression implements Expression {
    private final String operator;
    private final double value;
    @Override
    public boolean interpret(PromotionContext context) {
        double totalAmount = context.getTotalAmount();

        return switch (operator) {
            case ">" -> totalAmount > value;
            case ">=" -> totalAmount >= value;
            case "<" -> totalAmount < value;
            case "<=" -> totalAmount <= value;
            case "==" -> Math.abs(totalAmount - value) < FLOATING_POINT_TOLERANCE;
            case "!=" -> Math.abs(totalAmount - value) >= FLOATING_POINT_TOLERANCE;
            default -> throw new IllegalArgumentException("Unknown operator: " + operator);
        };
    }
}
