package ar.uba.fi.ingsoft1.product_example.promotions.interpreter.builders;

import ar.uba.fi.ingsoft1.product_example.promotions.interpreter.ExpressionFactory;
import ar.uba.fi.ingsoft1.product_example.promotions.interpreter.expression.Expression;
import com.fasterxml.jackson.databind.JsonNode;

public class BuilderProductNameExpression implements BuilderExpression{
    @Override
    public Expression buildExpression(ExpressionFactory factory, JsonNode node) {
        String productName = node.get("productName").asText();
        return factory.createProductName(productName);
    }
}
