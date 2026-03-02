package ar.uba.fi.ingsoft1.product_example.promotions.interpreter.builders;

import ar.uba.fi.ingsoft1.product_example.promotions.interpreter.ActionFactory;
import ar.uba.fi.ingsoft1.product_example.promotions.interpreter.action.Action;
import ar.uba.fi.ingsoft1.product_example.promotions.interpreter.action.FreeProductAction;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class BuilderFreeProductActionTest {

    @Autowired
    private ActionFactory actionFactory;

    private BuilderFreeProductAction builder;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        builder = new BuilderFreeProductAction();
        objectMapper = new ObjectMapper();
    }

    @Test
    void buildActionWithProductIdAndQuantity() throws Exception {

        String json = "{\"type\":\"freeProduct\",\"targetType\":\"ORDER\",\"productId\":1,\"quantity\":2}";
        JsonNode node = objectMapper.readTree(json);
        Action result = builder.buildAction(actionFactory, node);
        assertNotNull(result);
        assertInstanceOf(FreeProductAction.class, result);
    }

    @Test
    void buildActionWithDefaultQuantity() throws Exception {

        String json = "{\"type\":\"freeProduct\",\"targetType\":\"ORDER\",\"productId\":1}";
        JsonNode node = objectMapper.readTree(json);
        Action result = builder.buildAction(actionFactory, node);
        assertNotNull(result);
        assertInstanceOf(FreeProductAction.class, result);
    }

    @ParameterizedTest(name = "ProductId: {0}, Quantity: {1}")
    @CsvSource({
            "1, 1",
            "1, 5",
            "1, 10",
            "50, 1",
            "50, 5",
            "50, 10",
            "100, 1",
            "100, 5",
            "100, 10"
    })
    void buildActionWithMultipleCombinations(long productId, int quantity) throws Exception {
        String json = String.format(
                "{\"type\":\"freeProduct\",\"targetType\":\"ORDER\",\"productId\":%d,\"quantity\":%d}",
                productId, quantity);
        JsonNode node = objectMapper.readTree(json);
        Action result = builder.buildAction(actionFactory, node);
        assertNotNull(result);
        assertInstanceOf(FreeProductAction.class, result);
    }

    @Test
    void buildActionWithDifferentQuantities() throws Exception {
        int[] quantities = { 1, 2, 3, 5, 10, 20, 50 };
        for (int quantity : quantities) {
            String json = String.format(
                    "{\"type\":\"freeProduct\",\"targetType\":\"ORDER\",\"productId\":1,\"quantity\":%d}", quantity);
            JsonNode node = objectMapper.readTree(json);
            Action result = builder.buildAction(actionFactory, node);
            assertNotNull(result, "Failed for quantity: " + quantity);
            assertInstanceOf(FreeProductAction.class, result);
        }
    }
}
