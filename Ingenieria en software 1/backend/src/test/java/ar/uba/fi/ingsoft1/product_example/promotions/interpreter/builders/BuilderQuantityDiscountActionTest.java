package ar.uba.fi.ingsoft1.product_example.promotions.interpreter.builders;

import ar.uba.fi.ingsoft1.product_example.promotions.interpreter.ActionFactory;
import ar.uba.fi.ingsoft1.product_example.promotions.interpreter.action.Action;
import ar.uba.fi.ingsoft1.product_example.promotions.interpreter.action.QuantityDiscountAction;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class BuilderQuantityDiscountActionTest {

    @Autowired
    private ActionFactory actionFactory;

    private BuilderQuantityDiscountAction builder;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        builder = new BuilderQuantityDiscountAction();
        objectMapper = new ObjectMapper();
    }

    @Test
    void buildActionWithBuyAndPayQuantities() throws Exception {
        String json = "{\"type\":\"quantityDiscount\",\"buyQuantity\":3,\"payQuantity\":2}";
        JsonNode node = objectMapper.readTree(json);
        Action result = builder.buildAction(actionFactory, node);
        assertNotNull(result);
        assertInstanceOf(QuantityDiscountAction.class, result);
    }

    @Test
    void buildActionWithDifferentBuyPayCombinations() throws Exception {
        int[][] combinations = {{2, 1}, {3, 2}, {4, 3}, {5, 4}, {10, 7}};
        for (int[] combo : combinations) {
            String json = String.format("{\"type\":\"quantityDiscount\",\"buyQuantity\":%d,\"payQuantity\":%d}", combo[0], combo[1]);
            JsonNode node = objectMapper.readTree(json);
            Action result = builder.buildAction(actionFactory, node);
            assertNotNull(result, "Failed for buy: " + combo[0] + ", pay: " + combo[1]);
        }
    }
}

