package ar.uba.fi.ingsoft1.product_example.promotions.interpreter;

import ar.uba.fi.ingsoft1.product_example.promotions.interpreter.builders.BuilderAction;
import ar.uba.fi.ingsoft1.product_example.promotions.interpreter.builders.BuilderExpression;
import ar.uba.fi.ingsoft1.product_example.promotions.interpreter.expression.IfExpression;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class PromotionInterpreterTest {

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ActionFactory actionFactory;

    private PromotionInterpreter interpreter;

    @BeforeEach
    void setUp() {
        Map<String, BuilderExpression> builderExpressions = new HashMap<>();
        Map<String, BuilderAction> builderActions = new HashMap<>();
        interpreter = new PromotionInterpreter(objectMapper, actionFactory, builderExpressions, builderActions);
    }

    @Test
    void parseExpressionWithTotalAmountCondition() {
        String json = "{\"condition\":{\"type\":\"totalAmount\",\"operator\":\">=\",\"value\":1000},\"action\":{\"type\":\"percentageDiscount\",\"targetType\":\"ORDER\",\"percentage\":10}}";
        IfExpression result = interpreter.parseExpression(json);
        assertNotNull(result);
    }

    @Test
    void parseExpressionWithDayOfWeekCondition() {
        String json = "{\"condition\":{\"type\":\"dayOfWeek\",\"day\":\"MONDAY\"},\"action\":{\"type\":\"fixedDiscount\",\"targetType\":\"ORDER\",\"amount\":100}}";
        IfExpression result = interpreter.parseExpression(json);
        assertNotNull(result);
    }

    @Test
    void parseExpressionWithProductInCartCondition() {
        String json = "{\"condition\":{\"type\":\"productInCart\",\"productId\":1},\"action\":{\"type\":\"fixedDiscount\",\"targetType\":\"ORDER\",\"amount\":50}}";
        IfExpression result = interpreter.parseExpression(json);
        assertNotNull(result);
    }

    @Test
    void parseExpressionWithQuantityCondition() {
        String json = "{\"condition\":{\"type\":\"quantity\",\"productId\":1,\"minQuantity\":3},\"action\":{\"type\":\"quantityDiscount\",\"buyQuantity\":3,\"payQuantity\":2}}";
        IfExpression result = interpreter.parseExpression(json);
        assertNotNull(result);
    }

    @Test
    void parseExpressionWithProductNameCondition() {
        String json = "{\"condition\":{\"type\":\"productName\",\"productName\":\"Pizza\"},\"action\":{\"type\":\"percentageDiscount\",\"targetType\":\"ORDER\",\"percentage\":15}}";
        IfExpression result = interpreter.parseExpression(json);
        assertNotNull(result);
    }

    @Test
    void parseExpressionWithProductTypeCategoryCondition() {
        String json = "{\"condition\":{\"type\":\"productType\",\"filterType\":\"category\",\"category\":\"Food\"},\"action\":{\"type\":\"fixedDiscount\",\"targetType\":\"ORDER\",\"amount\":100}}";
        IfExpression result = interpreter.parseExpression(json);
        assertNotNull(result);
    }

    @Test
    void parseExpressionWithProductTypeTypeCondition() {
        String json = "{\"condition\":{\"type\":\"productType\",\"filterType\":\"type\",\"productType\":\"Principal\"},\"action\":{\"type\":\"percentageDiscount\",\"targetType\":\"ORDER\",\"percentage\":10}}";
        IfExpression result = interpreter.parseExpression(json);
        assertNotNull(result);
    }

    @Test
    void parseExpressionWithAndCondition() {
        String json = "{\"condition\":{\"type\":\"and\",\"left\":{\"type\":\"totalAmount\",\"operator\":\">=\",\"value\":1000},\"right\":{\"type\":\"dayOfWeek\",\"day\":\"FRIDAY\"}},\"action\":{\"type\":\"percentageDiscount\",\"targetType\":\"ORDER\",\"percentage\":25}}";
        IfExpression result = interpreter.parseExpression(json);
        assertNotNull(result);
    }

    @Test
    void parseExpressionWithOrCondition() {
        String json = "{\"condition\":{\"type\":\"or\",\"left\":{\"type\":\"productInCart\",\"productId\":1},\"right\":{\"type\":\"productInCart\",\"productId\":2}},\"action\":{\"type\":\"fixedDiscount\",\"targetType\":\"ORDER\",\"amount\":200}}";
        IfExpression result = interpreter.parseExpression(json);
        assertNotNull(result);
    }

    @Test
    void parseExpressionWithNestedAndOrConditions() {
        String json = "{\"condition\":{\"type\":\"and\",\"left\":{\"type\":\"or\",\"left\":{\"type\":\"dayOfWeek\",\"day\":\"SATURDAY\"},\"right\":{\"type\":\"dayOfWeek\",\"day\":\"SUNDAY\"}},\"right\":{\"type\":\"totalAmount\",\"operator\":\">=\",\"value\":2000}},\"action\":{\"type\":\"freeProduct\",\"targetType\":\"ORDER\",\"productId\":1,\"quantity\":1}}";
        IfExpression result = interpreter.parseExpression(json);
        assertNotNull(result);
    }

    @Test
    void parseExpressionWithFixedDiscountAction() {
        String json = "{\"condition\":{\"type\":\"totalAmount\",\"operator\":\">=\",\"value\":1000},\"action\":{\"type\":\"fixedDiscount\",\"targetType\":\"ORDER\",\"amount\":100}}";
        IfExpression result = interpreter.parseExpression(json);
        assertNotNull(result);
    }

    @Test
    void parseExpressionWithPercentageDiscountAction() {
        String json = "{\"condition\":{\"type\":\"totalAmount\",\"operator\":\">=\",\"value\":1000},\"action\":{\"type\":\"percentageDiscount\",\"targetType\":\"ORDER\",\"percentage\":10}}";
        IfExpression result = interpreter.parseExpression(json);
        assertNotNull(result);
    }

    @Test
    void parseExpressionWithFreeProductAction() {
        String json = "{\"condition\":{\"type\":\"totalAmount\",\"operator\":\">=\",\"value\":2000},\"action\":{\"type\":\"freeProduct\",\"targetType\":\"ORDER\",\"productId\":1,\"quantity\":2}}";
        IfExpression result = interpreter.parseExpression(json);
        assertNotNull(result);
    }

    @Test
    void parseExpressionWithQuantityDiscountAction() {
        String json = "{\"condition\":{\"type\":\"quantity\",\"productId\":1,\"minQuantity\":3},\"action\":{\"type\":\"quantityDiscount\",\"buyQuantity\":3,\"payQuantity\":2}}";
        IfExpression result = interpreter.parseExpression(json);
        assertNotNull(result);
    }

    @Test
    void parseExpressionWithFixedDiscountOrderItemAction() {
        String json = "{\"condition\":{\"type\":\"totalAmount\",\"operator\":\">=\",\"value\":1000},\"action\":{\"type\":\"fixedDiscount\",\"targetType\":\"ORDER_ITEM\",\"targetFilterType\":\"product\",\"targetItemId\":1,\"amount\":50}}";
        IfExpression result = interpreter.parseExpression(json);
        assertNotNull(result);
    }

    @Test
    void parseExpressionWithPercentageDiscountOrderItemByCategory() {
        String json = "{\"condition\":{\"type\":\"totalAmount\",\"operator\":\">=\",\"value\":1000},\"action\":{\"type\":\"percentageDiscount\",\"targetType\":\"ORDER_ITEM\",\"targetFilterType\":\"category\",\"targetCategory\":\"Food\",\"percentage\":15}}";
        IfExpression result = interpreter.parseExpression(json);
        assertNotNull(result);
    }

    @Test
    void parseExpressionWithPercentageDiscountOrderItemByType() {
        String json = "{\"condition\":{\"type\":\"totalAmount\",\"operator\":\">=\",\"value\":1000},\"action\":{\"type\":\"percentageDiscount\",\"targetType\":\"ORDER_ITEM\",\"targetFilterType\":\"type\",\"targetProductType\":\"Principal\",\"percentage\":10}}";
        IfExpression result = interpreter.parseExpression(json);
        assertNotNull(result);
    }

    @Test
    void parseExpressionThrowsExceptionWhenMissingCondition() {
        String json = "{\"action\":{\"type\":\"percentageDiscount\",\"targetType\":\"ORDER\",\"percentage\":10}}";
        assertThrows(IllegalArgumentException.class, () -> interpreter.parseExpression(json));
    }

    @Test
    void parseExpressionThrowsExceptionWhenMissingAction() {
        String json = "{\"condition\":{\"type\":\"totalAmount\",\"operator\":\">=\",\"value\":1000}}";
        assertThrows(IllegalArgumentException.class, () -> interpreter.parseExpression(json));
    }

    @Test
    void parseExpressionThrowsExceptionWhenInvalidJson() {
        String json = "invalid json";
        assertThrows(RuntimeException.class, () -> interpreter.parseExpression(json));
    }

    @Test
    void parseExpressionWithAllTotalAmountOperators() {
        String[] operators = { ">", ">=", "<", "<=", "==" };
        for (String operator : operators) {
            String json = String.format(
                    "{\"condition\":{\"type\":\"totalAmount\",\"operator\":\"%s\",\"value\":1500},\"action\":{\"type\":\"percentageDiscount\",\"targetType\":\"ORDER\",\"percentage\":10}}",
                    operator);
            IfExpression result = interpreter.parseExpression(json);
            assertNotNull(result, "Failed for operator: " + operator);
        }
    }
}
