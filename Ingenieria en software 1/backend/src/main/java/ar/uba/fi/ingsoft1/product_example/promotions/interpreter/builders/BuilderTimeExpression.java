package ar.uba.fi.ingsoft1.product_example.promotions.interpreter.builders;

import ar.uba.fi.ingsoft1.product_example.promotions.interpreter.ExpressionFactory;
import ar.uba.fi.ingsoft1.product_example.promotions.interpreter.expression.Expression;
import com.fasterxml.jackson.databind.JsonNode;

import java.time.LocalTime;

public class BuilderTimeExpression implements BuilderExpression{
    @Override
    public Expression buildExpression(ExpressionFactory factory, JsonNode node) {
        // format hh:mm:ss
        String hourText = node.get("hour").asText();
        LocalTime hour = LocalTime.parse(hourText);
        String operator = node.get("operator").asText();
        return factory.createTimeExpression(hour, operator);
    }
}
