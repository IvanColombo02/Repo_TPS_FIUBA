package ar.uba.fi.ingsoft1.product_example.promotions.interpreter;

import ar.uba.fi.ingsoft1.product_example.promotions.interpreter.action.Action;
import ar.uba.fi.ingsoft1.product_example.promotions.interpreter.builders.*;
import ar.uba.fi.ingsoft1.product_example.promotions.interpreter.expression.*;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@RequiredArgsConstructor
public class PromotionInterpreter {
    private final ObjectMapper objectMapper;
    private final ExpressionFactory expressionFactory = ExpressionFactory.getExpressionFactory();
    private final ActionFactory actionFactory;
    private final Map<String, BuilderExpression> builderExpressions;
    private final Map<String, BuilderAction> builderActions;

    public IfExpression parseExpression(String expressionJson) {
        try {
            JsonNode root = objectMapper.readTree(expressionJson);

            JsonNode conditionNode = root.get("condition");
            JsonNode actionNode = root.get("action");

            if (conditionNode == null || actionNode == null) {
                throw new IllegalArgumentException("JSON debe contener 'condition' y 'action'");
            }
            Expression condition = buildExpression(conditionNode);

            Action action = buildAction(actionNode);

            return new IfExpression(condition, action);

        } catch (JsonProcessingException e) {
            throw new RuntimeException("Error parsing expression JSON: " + e.getMessage(), e);
        }
    }

    private void chargeExpressionBuilders() {
        builderExpressions.put("dayOfWeek", new BuilderDayOfWeekExpression());
        builderExpressions.put("quantity", new BuilderQuantityExpression());
        builderExpressions.put("totalAmount", new BuilderTotalAmountExpression());
        builderExpressions.put("productType", new BuilderProductFilterExpression());
        builderExpressions.put("productName", new BuilderProductNameExpression());
        builderExpressions.put("productInCart", new BuilderProductInCarExpression());
        builderExpressions.put("mailContains", new BuilderMailContainsExpression());
        builderExpressions.put("time", new BuilderTimeExpression());
    }

    private void chargeActionBuilders() {
        builderActions.put("fixedDiscount", new BuildderFixedDiscountAction());
        builderActions.put("quantityDiscount", new BuilderQuantityDiscountAction());
        builderActions.put("percentageDiscount", new BuilderPercentageDiscountAction());
        builderActions.put("freeProduct", new BuilderFreeProductAction());
    }

    private Expression buildExpression(JsonNode node) {
        chargeExpressionBuilders();
        String type = node.get("type").asText();
        switch (type) {
            case "and":
                Expression left = buildExpression(node.get("left"));
                Expression right = buildExpression(node.get("right"));
                return expressionFactory.createAndExpression(left, right);

            case "or":
                left = buildExpression(node.get("left"));
                right = buildExpression(node.get("right"));
                return expressionFactory.createOrExpression(left, right);
            default:
                return builderExpressions.get(type).buildExpression(expressionFactory, node);
        }
    }

    private Action buildAction(JsonNode node) {
        chargeActionBuilders();
        String type = node.get("type").asText();
        return builderActions.get(type).buildAction(actionFactory, node);
    }
}
