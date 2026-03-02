package ar.uba.fi.ingsoft1.product_example.promotions.interpreter.builders;

import ar.uba.fi.ingsoft1.product_example.promotions.interpreter.ActionFactory;
import ar.uba.fi.ingsoft1.product_example.promotions.interpreter.action.Action;
import com.fasterxml.jackson.databind.JsonNode;

public class BuilderPercentageDiscountAction implements  BuilderAction {
    @Override
    public Action buildAction(ActionFactory factory, JsonNode node) {
        String targetType = node.get("targetType").asText();
        double percentage = node.get("percentage").asDouble();

        if ("ORDER_ITEM".equals(targetType)) {
            String targetFilterType = node.has("targetFilterType") ? node.get("targetFilterType").asText()
                    : "product";
            if ("product".equals(targetFilterType)) {
                long targetItemId = node.get("targetItemId").asLong();
                return factory.createPercentageDiscountAction(targetType, targetItemId, null, null,
                        percentage);
            } else if ("category".equals(targetFilterType)) {
                String targetCategory = node.get("targetCategory").asText();
                return factory.createPercentageDiscountAction(targetType, null, targetCategory, null,
                        percentage);
            } else {
                String targetProductType = node.get("targetProductType").asText();
                return factory.createPercentageDiscountAction(targetType, null, null, targetProductType,
                        percentage);
            }
        } else {
            return factory.createPercentageDiscountAction(targetType, null, null, null, percentage);
        }
    }
}
