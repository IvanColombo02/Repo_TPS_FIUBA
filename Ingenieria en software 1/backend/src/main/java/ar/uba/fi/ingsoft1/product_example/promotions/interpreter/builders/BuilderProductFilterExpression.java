package ar.uba.fi.ingsoft1.product_example.promotions.interpreter.builders;

import ar.uba.fi.ingsoft1.product_example.promotions.interpreter.ExpressionFactory;
import ar.uba.fi.ingsoft1.product_example.promotions.interpreter.expression.Expression;
import ar.uba.fi.ingsoft1.product_example.promotions.interpreter.expression.ProductTypeExpression;
import com.fasterxml.jackson.databind.JsonNode;

public class BuilderProductFilterExpression implements BuilderExpression{
    private final String CATEGORY = "category";
    private final String TYPE = "productType";
    private final String FILTER_TYPE = "filterType";
    public Expression buildExpression(ExpressionFactory factory, JsonNode node) {
        String filterType = node.has(FILTER_TYPE) ? node.get(FILTER_TYPE).asText() : CATEGORY;
        if (filterType.equals(CATEGORY)) {
            String category = node.get(CATEGORY).asText();
            return factory.createProductCategoryExpression(category);
        } else {
            String productType = node.get(TYPE).asText();
            return factory.createProductTypeExpression(productType);
        }
    }
}
