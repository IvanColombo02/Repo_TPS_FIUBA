package ar.uba.fi.ingsoft1.product_example.promotions.interpreter.builders;

import ar.uba.fi.ingsoft1.product_example.promotions.interpreter.ActionFactory;
import ar.uba.fi.ingsoft1.product_example.promotions.interpreter.action.Action;
import ar.uba.fi.ingsoft1.product_example.promotions.interpreter.action.PercentageDiscountAction;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class BuilderPercentageDiscountActionTest {

    @Autowired
    private ActionFactory actionFactory;

    private BuilderPercentageDiscountAction builder;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        builder = new BuilderPercentageDiscountAction();
        objectMapper = new ObjectMapper();
    }

    @Test
    void buildActionForOrder() throws Exception {
        String json = "{\"type\":\"percentageDiscount\",\"targetType\":\"ORDER\",\"percentage\":10}";
        JsonNode node = objectMapper.readTree(json);
        Action result = builder.buildAction(actionFactory, node);
        assertNotNull(result);
        assertInstanceOf(PercentageDiscountAction.class, result);
    }

    @Test
    void buildActionForOrderItemByProduct() throws Exception {
        String json = "{\"type\":\"percentageDiscount\",\"targetType\":\"ORDER_ITEM\",\"targetFilterType\":\"product\",\"targetItemId\":1,\"percentage\":15}";
        JsonNode node = objectMapper.readTree(json);
        Action result = builder.buildAction(actionFactory, node);
        assertNotNull(result);
        assertInstanceOf(PercentageDiscountAction.class, result);
    }

    @Test
    void buildActionForOrderItemByCategory() throws Exception {
        String json = "{\"type\":\"percentageDiscount\",\"targetType\":\"ORDER_ITEM\",\"targetFilterType\":\"category\",\"targetCategory\":\"Food\",\"percentage\":15}";
        JsonNode node = objectMapper.readTree(json);
        Action result = builder.buildAction(actionFactory, node);
        assertNotNull(result);
        assertInstanceOf(PercentageDiscountAction.class, result);
    }

    @Test
    void buildActionForOrderItemByType() throws Exception {
        String json = "{\"type\":\"percentageDiscount\",\"targetType\":\"ORDER_ITEM\",\"targetFilterType\":\"type\",\"targetProductType\":\"Principal\",\"percentage\":10}";
        JsonNode node = objectMapper.readTree(json);
        Action result = builder.buildAction(actionFactory, node);
        assertNotNull(result);
        assertInstanceOf(PercentageDiscountAction.class, result);
    }

    @Test
    void buildActionForOrderItemWithDefaultFilterType() throws Exception {
        String json = "{\"type\":\"percentageDiscount\",\"targetType\":\"ORDER_ITEM\",\"targetItemId\":1,\"percentage\":15}";
        JsonNode node = objectMapper.readTree(json);
        Action result = builder.buildAction(actionFactory, node);
        assertNotNull(result);
        assertInstanceOf(PercentageDiscountAction.class, result);
    }

    @Test
    void buildActionWithDifferentPercentages() throws Exception {
        double[] percentages = { 5, 10, 15, 20, 25, 50 };
        for (double percentage : percentages) {
            String json = String.format(
                    "{\"type\":\"percentageDiscount\",\"targetType\":\"ORDER\",\"percentage\":%.0f}", percentage);
            JsonNode node = objectMapper.readTree(json);
            Action result = builder.buildAction(actionFactory, node);
            assertNotNull(result, "Failed for percentage: " + percentage);
        }
    }
}
