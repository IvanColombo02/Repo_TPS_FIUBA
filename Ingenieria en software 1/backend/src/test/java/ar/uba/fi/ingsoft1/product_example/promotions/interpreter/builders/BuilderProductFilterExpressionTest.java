package ar.uba.fi.ingsoft1.product_example.promotions.interpreter.builders;

import ar.uba.fi.ingsoft1.product_example.promotions.interpreter.ExpressionFactory;
import ar.uba.fi.ingsoft1.product_example.promotions.interpreter.expression.Expression;
import ar.uba.fi.ingsoft1.product_example.promotions.interpreter.expression.ProductCategoryExpression;
import ar.uba.fi.ingsoft1.product_example.promotions.interpreter.expression.ProductTypeExpression;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class BuilderProductFilterExpressionTest {

    private BuilderProductFilterExpression builder;
    private ExpressionFactory factory;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        builder = new BuilderProductFilterExpression();
        factory = ExpressionFactory.getExpressionFactory();
        objectMapper = new ObjectMapper();
    }

    @Test
    void buildExpressionWithCategoryFilter() throws Exception {
        String json = "{\"type\":\"productType\",\"filterType\":\"category\",\"category\":\"Food\"}";
        JsonNode node = objectMapper.readTree(json);
        Expression result = builder.buildExpression(factory, node);
        assertNotNull(result);
        assertInstanceOf(ProductCategoryExpression.class, result);
    }

    @Test
    void buildExpressionWithTypeFilter() throws Exception {
        String json = "{\"type\":\"productType\",\"filterType\":\"type\",\"productType\":\"Principal\"}";
        JsonNode node = objectMapper.readTree(json);
        Expression result = builder.buildExpression(factory, node);
        assertNotNull(result);
        assertInstanceOf(ProductTypeExpression.class, result);
    }

    @Test
    void buildExpressionWithDefaultFilterTypeUsesCategory() throws Exception {
        String json = "{\"type\":\"productType\",\"category\":\"Food\"}";
        JsonNode node = objectMapper.readTree(json);
        Expression result = builder.buildExpression(factory, node);
        assertNotNull(result);
        assertInstanceOf(ProductCategoryExpression.class, result);
    }

    @Test
    void buildExpressionWithDifferentCategories() throws Exception {
        String[] categories = { "Food", "Drink", "Dessert", "Snack" };
        for (String category : categories) {
            String json = String.format("{\"type\":\"productType\",\"filterType\":\"category\",\"category\":\"%s\"}",
                    category);
            JsonNode node = objectMapper.readTree(json);
            Expression result = builder.buildExpression(factory, node);
            assertNotNull(result, "Failed for category: " + category);
        }
    }

    @Test
    void buildExpressionWithDifferentProductTypes() throws Exception {
        String[] types = { "Principal", "Bebida", "Postre", "Acompañamiento" };
        for (String type : types) {
            String json = String.format("{\"type\":\"productType\",\"filterType\":\"type\",\"productType\":\"%s\"}",
                    type);
            JsonNode node = objectMapper.readTree(json);
            Expression result = builder.buildExpression(factory, node);
            assertNotNull(result, "Failed for productType: " + type);
        }
    }
}
