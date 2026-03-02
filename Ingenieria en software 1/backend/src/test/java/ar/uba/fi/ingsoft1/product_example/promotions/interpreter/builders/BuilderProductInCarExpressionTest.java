package ar.uba.fi.ingsoft1.product_example.promotions.interpreter.builders;

import ar.uba.fi.ingsoft1.product_example.promotions.interpreter.ExpressionFactory;
import ar.uba.fi.ingsoft1.product_example.promotions.interpreter.expression.Expression;
import ar.uba.fi.ingsoft1.product_example.promotions.interpreter.expression.ProductInCartExpression;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class BuilderProductInCarExpressionTest {

    private BuilderProductInCarExpression builder;
    private ExpressionFactory factory;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        builder = new BuilderProductInCarExpression();
        factory = ExpressionFactory.getExpressionFactory();
        objectMapper = new ObjectMapper();
    }

    @Test
    void buildExpressionWithValidProductId() throws Exception {
        String json = "{\"type\":\"productInCart\",\"productId\":1}";
        JsonNode node = objectMapper.readTree(json);
        Expression result = builder.buildExpression(factory, node);
        assertNotNull(result);
        assertInstanceOf(ProductInCartExpression.class, result);
    }

    @Test
    void buildExpressionWithDifferentProductIds() throws Exception {
        long[] productIds = {1, 10, 100, 1000};
        for (long productId : productIds) {
            String json = String.format("{\"type\":\"productInCart\",\"productId\":%d}", productId);
            JsonNode node = objectMapper.readTree(json);
            Expression result = builder.buildExpression(factory, node);
            assertNotNull(result, "Failed for productId: " + productId);
        }
    }
}

