package ar.uba.fi.ingsoft1.product_example.promotions.interpreter.builders;

import ar.uba.fi.ingsoft1.product_example.promotions.interpreter.ExpressionFactory;
import ar.uba.fi.ingsoft1.product_example.promotions.interpreter.expression.Expression;
import ar.uba.fi.ingsoft1.product_example.promotions.interpreter.expression.ProductNameExpression;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class BuilderProductNameExpressionTest {

    private BuilderProductNameExpression builder;
    private ExpressionFactory factory;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        builder = new BuilderProductNameExpression();
        factory = ExpressionFactory.getExpressionFactory();
        objectMapper = new ObjectMapper();
    }

    @Test
    void buildExpressionWithValidProductName() throws Exception {
        String json = "{\"type\":\"productName\",\"productName\":\"Pizza\"}";
        JsonNode node = objectMapper.readTree(json);
        Expression result = builder.buildExpression(factory, node);
        assertNotNull(result);
        assertInstanceOf(ProductNameExpression.class, result);
    }

    @Test
    void buildExpressionWithDifferentProductNames() throws Exception {
        String[] names = {"Pizza", "Hamburguesa", "Ensalada", "Bebida"};
        for (String name : names) {
            String json = String.format("{\"type\":\"productName\",\"productName\":\"%s\"}", name);
            JsonNode node = objectMapper.readTree(json);
            Expression result = builder.buildExpression(factory, node);
            assertNotNull(result, "Failed for productName: " + name);
        }
    }
}

