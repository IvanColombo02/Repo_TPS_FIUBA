package ar.uba.fi.ingsoft1.product_example.promotions.interpreter.builders;

import ar.uba.fi.ingsoft1.product_example.promotions.interpreter.ExpressionFactory;
import ar.uba.fi.ingsoft1.product_example.promotions.interpreter.expression.Expression;
import ar.uba.fi.ingsoft1.product_example.promotions.interpreter.expression.QuantityExpression;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class BuilderQuantityExpressionTest {

    private BuilderQuantityExpression builder;
    private ExpressionFactory factory;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        builder = new BuilderQuantityExpression();
        factory = ExpressionFactory.getExpressionFactory();
        objectMapper = new ObjectMapper();
    }

    @Test
    void buildExpressionWithValidData() throws Exception {
        String json = "{\"type\":\"quantity\",\"productId\":1,\"minQuantity\":3}";
        JsonNode node = objectMapper.readTree(json);
        Expression result = builder.buildExpression(factory, node);
        assertNotNull(result);
        assertInstanceOf(QuantityExpression.class, result);
    }

    @Test
    void buildExpressionWithDifferentProductIds() throws Exception {
        long[] productIds = {1, 10, 100, 1000};
        for (long productId : productIds) {
            String json = String.format("{\"type\":\"quantity\",\"productId\":%d,\"minQuantity\":2}", productId);
            JsonNode node = objectMapper.readTree(json);
            Expression result = builder.buildExpression(factory, node);
            assertNotNull(result, "Failed for productId: " + productId);
        }
    }

    @Test
    void buildExpressionWithDifferentMinQuantities() throws Exception {
        int[] quantities = {1, 2, 3, 5, 10};
        for (int quantity : quantities) {
            String json = String.format("{\"type\":\"quantity\",\"productId\":1,\"minQuantity\":%d}", quantity);
            JsonNode node = objectMapper.readTree(json);
            Expression result = builder.buildExpression(factory, node);
            assertNotNull(result, "Failed for minQuantity: " + quantity);
        }
    }
}

