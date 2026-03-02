package ar.uba.fi.ingsoft1.product_example.promotions.interpreter.builders;

import static ar.uba.fi.ingsoft1.product_example.promotions.PromotionConstants.DEFAULT_FREE_PRODUCT_QUANTITY;
import ar.uba.fi.ingsoft1.product_example.promotions.interpreter.ActionFactory;
import ar.uba.fi.ingsoft1.product_example.promotions.interpreter.action.Action;
import com.fasterxml.jackson.databind.JsonNode;

public class BuilderFreeProductAction implements  BuilderAction{
    @Override
    public Action buildAction(ActionFactory factory, JsonNode node) {
        long productId = node.get("productId").asLong();
        int quantity = node.has("quantity") ? node.get("quantity").asInt() : DEFAULT_FREE_PRODUCT_QUANTITY;
        return factory.createFreeProductAction(productId, quantity);
    }
}
