package ar.uba.fi.ingsoft1.product_example.promotions.interpreter.builders;

import ar.uba.fi.ingsoft1.product_example.promotions.interpreter.ActionFactory;
import ar.uba.fi.ingsoft1.product_example.promotions.interpreter.action.Action;
import com.fasterxml.jackson.databind.JsonNode;

public class BuildderFixedDiscountAction implements BuilderAction{
    @Override
    public Action buildAction(ActionFactory factory, JsonNode node) {

        String targetType = node.get("targetType").asText();
        double amount = node.get("amount").asDouble();

        // filtro por itemss
        if ("ORDER_ITEM".equals(targetType)) {
            String targetFilterType = node.has("targetFilterType") ? node.get("targetFilterType").asText()
                    : "product";
            if ("product".equals(targetFilterType)) {
                long targetItemId = node.get("targetItemId").asLong();
                return factory.createFixedDiscountAction(targetType, targetItemId, null, null, amount);
            } else if ("category".equals(targetFilterType)) {
                String targetCategory = node.get("targetCategory").asText();
                return factory.createFixedDiscountAction(targetType, null, targetCategory, null, amount);
            } else {
                String targetProductType = node.get("targetProductType").asText();
                return factory.createFixedDiscountAction(targetType, null, null, targetProductType,
                        amount);
            }
        } else {
            return factory.createFixedDiscountAction(targetType, null, null, null, amount);
        }
    }
}
