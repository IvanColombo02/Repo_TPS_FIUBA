package ar.uba.fi.ingsoft1.product_example.promotions.interpreter.builders;

import ar.uba.fi.ingsoft1.product_example.promotions.interpreter.ExpressionFactory;
import ar.uba.fi.ingsoft1.product_example.promotions.interpreter.expression.Expression;
import ar.uba.fi.ingsoft1.product_example.promotions.interpreter.expression.TotalAmountExpression;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

class BuilderTotalAmountExpressionTest {

    private BuilderTotalAmountExpression builder;
    private ExpressionFactory factory;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        builder = new BuilderTotalAmountExpression();
        factory = ExpressionFactory.getExpressionFactory();
        objectMapper = new ObjectMapper();
    }

    @ParameterizedTest(name = "buildExpression with operator: {0}")
    @ValueSource(strings = {">", ">=", "<", "<=", "==", "!="})
    void buildExpressionWithOperators(String operator) throws Exception {
        String json = String.format("{\"type\":\"totalAmount\",\"operator\":\"%s\",\"value\":1000}", operator);
        JsonNode node = objectMapper.readTree(json);
        Expression result = builder.buildExpression(factory, node);
        assertNotNull(result, "Failed for operator: " + operator);
        assertInstanceOf(TotalAmountExpression.class, result);
    }

    @Test
    void buildExpressionWithDifferentValues() throws Exception {
        double[] values = {0, 100, 1000, 5000, 10000};
        for (double value : values) {
            String json = String.format("{\"type\":\"totalAmount\",\"operator\":\">=\",\"value\":%.0f}", value);
            JsonNode node = objectMapper.readTree(json);
            Expression result = builder.buildExpression(factory, node);
            assertNotNull(result, "Failed for value: " + value);
        }
    }
}

