package ar.uba.fi.ingsoft1.product_example.promotions.interpreter.builders;

import ar.uba.fi.ingsoft1.product_example.promotions.interpreter.ExpressionFactory;
import ar.uba.fi.ingsoft1.product_example.promotions.interpreter.expression.Expression;
import com.fasterxml.jackson.databind.JsonNode;

public class BuilderQuantityExpression implements BuilderExpression{

    @Override
    public Expression buildExpression(ExpressionFactory factory, JsonNode node) {
        long quantityProductId = node.get("productId").asLong();
        int minQuantity = node.get("minQuantity").asInt();
        return factory.createQuantity(quantityProductId, minQuantity);
    }
}
