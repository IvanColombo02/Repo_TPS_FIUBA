package ar.uba.fi.ingsoft1.product_example.promotions.interpreter.builders;

import ar.uba.fi.ingsoft1.product_example.promotions.interpreter.ExpressionFactory;
import ar.uba.fi.ingsoft1.product_example.promotions.interpreter.expression.Expression;
import com.fasterxml.jackson.databind.JsonNode;

public class BuilderTotalAmountExpression implements BuilderExpression {
    public Expression buildExpression(ExpressionFactory factory, JsonNode node) {
        String operator = node.get("operator").asText();
        double value = node.get("value").asDouble();
        return factory.createTotalAmountExpression(operator, value);
    }
}
