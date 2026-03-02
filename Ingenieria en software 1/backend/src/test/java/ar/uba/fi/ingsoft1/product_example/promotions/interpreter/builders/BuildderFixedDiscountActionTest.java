package ar.uba.fi.ingsoft1.product_example.promotions.interpreter.builders;

import ar.uba.fi.ingsoft1.product_example.promotions.interpreter.ActionFactory;
import ar.uba.fi.ingsoft1.product_example.promotions.interpreter.action.Action;
import ar.uba.fi.ingsoft1.product_example.promotions.interpreter.action.FixedDiscountAction;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class BuildderFixedDiscountActionTest {

    @Autowired
    private ActionFactory actionFactory;

    private BuildderFixedDiscountAction builder;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        builder = new BuildderFixedDiscountAction();
        objectMapper = new ObjectMapper();
    }

    @Test
    void buildActionForOrder() throws Exception {
        String json = "{\"type\":\"fixedDiscount\",\"targetType\":\"ORDER\",\"amount\":100}";
        JsonNode node = objectMapper.readTree(json);
        Action result = builder.buildAction(actionFactory, node);
        assertNotNull(result);
        assertInstanceOf(FixedDiscountAction.class, result);
    }

    @Test
    void buildActionForOrderItemByProduct() throws Exception {
        String json = "{\"type\":\"fixedDiscount\",\"targetType\":\"ORDER_ITEM\",\"targetFilterType\":\"product\",\"targetItemId\":1,\"amount\":50}";
        JsonNode node = objectMapper.readTree(json);
        Action result = builder.buildAction(actionFactory, node);
        assertNotNull(result);
        assertInstanceOf(FixedDiscountAction.class, result);
    }

    @Test
    void buildActionForOrderItemByCategory() throws Exception {
        String json = "{\"type\":\"fixedDiscount\",\"targetType\":\"ORDER_ITEM\",\"targetFilterType\":\"category\",\"targetCategory\":\"Food\",\"amount\":50}";
        JsonNode node = objectMapper.readTree(json);
        Action result = builder.buildAction(actionFactory, node);
        assertNotNull(result);
        assertInstanceOf(FixedDiscountAction.class, result);
    }

    @Test
    void buildActionForOrderItemByType() throws Exception {
        String json = "{\"type\":\"fixedDiscount\",\"targetType\":\"ORDER_ITEM\",\"targetFilterType\":\"type\",\"targetProductType\":\"Principal\",\"amount\":50}";
        JsonNode node = objectMapper.readTree(json);
        Action result = builder.buildAction(actionFactory, node);
        assertNotNull(result);
        assertInstanceOf(FixedDiscountAction.class, result);
    }

    @Test
    void buildActionForOrderItemWithDefaultFilterType() throws Exception {
        String json = "{\"type\":\"fixedDiscount\",\"targetType\":\"ORDER_ITEM\",\"targetItemId\":1,\"amount\":50}";
        JsonNode node = objectMapper.readTree(json);
        Action result = builder.buildAction(actionFactory, node);
        assertNotNull(result);
        assertInstanceOf(FixedDiscountAction.class, result);
    }

    @Test
    void buildActionWithDifferentAmounts() throws Exception {
        double[] amounts = { 10, 50, 100, 200, 500 };
        for (double amount : amounts) {
            String json = String.format("{\"type\":\"fixedDiscount\",\"targetType\":\"ORDER\",\"amount\":%.0f}",
                    amount);
            JsonNode node = objectMapper.readTree(json);
            Action result = builder.buildAction(actionFactory, node);
            assertNotNull(result, "Failed for amount: " + amount);
        }
    }
}
