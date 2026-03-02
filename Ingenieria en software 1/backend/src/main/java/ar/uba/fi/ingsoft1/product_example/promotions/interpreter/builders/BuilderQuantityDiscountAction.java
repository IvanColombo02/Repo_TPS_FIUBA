package ar.uba.fi.ingsoft1.product_example.promotions.interpreter.builders;

import ar.uba.fi.ingsoft1.product_example.promotions.interpreter.ActionFactory;
import ar.uba.fi.ingsoft1.product_example.promotions.interpreter.action.Action;
import com.fasterxml.jackson.databind.JsonNode;

public class BuilderQuantityDiscountAction implements BuilderAction {
    public Action buildAction(ActionFactory factory, JsonNode node) {
        int buyQuantity = node.get("buyQuantity").asInt();
        int payQuantity = node.get("payQuantity").asInt();
        return factory.createQuantityDiscountAction(buyQuantity, payQuantity);

    }
}
