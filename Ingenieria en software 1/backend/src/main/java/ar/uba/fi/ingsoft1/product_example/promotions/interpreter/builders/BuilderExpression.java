package ar.uba.fi.ingsoft1.product_example.promotions.interpreter.builders;

import ar.uba.fi.ingsoft1.product_example.promotions.interpreter.ExpressionFactory;
import ar.uba.fi.ingsoft1.product_example.promotions.interpreter.expression.Expression;
import com.fasterxml.jackson.databind.JsonNode;

public interface BuilderExpression {
    Expression buildExpression(ExpressionFactory factory, JsonNode node);
}
