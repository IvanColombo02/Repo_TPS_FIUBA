package ar.uba.fi.ingsoft1.product_example.promotions.interpreter.builders;

import ar.uba.fi.ingsoft1.product_example.promotions.interpreter.ActionFactory;
import ar.uba.fi.ingsoft1.product_example.promotions.interpreter.ExpressionFactory;
import ar.uba.fi.ingsoft1.product_example.promotions.interpreter.action.Action;
import com.fasterxml.jackson.databind.JsonNode;

public interface BuilderAction {
    Action buildAction(ActionFactory factory, JsonNode node);
}
