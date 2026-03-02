package ar.uba.fi.ingsoft1.product_example.promotions.interpreter.expression;

import ar.uba.fi.ingsoft1.product_example.items.products.Product;
import ar.uba.fi.ingsoft1.product_example.order.Order;
import ar.uba.fi.ingsoft1.product_example.order.OrderItem;
import ar.uba.fi.ingsoft1.product_example.order.OrderStatus;
import ar.uba.fi.ingsoft1.product_example.order.PaymentMethod;
import ar.uba.fi.ingsoft1.product_example.promotions.interpreter.PromotionContext;
import ar.uba.fi.ingsoft1.product_example.user.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class TotalAmountExpressionTest {

    private Order order;
    private PromotionContext context;
    private Product product;

    @BeforeEach
    void setUp() {
        User user = new User();
        user.setId(1L);

        order = new Order();
        order.setId(1L);
        order.setUser(user);
        order.setStatus(OrderStatus.PENDING);
        order.setPaymentMethod(PaymentMethod.CASH);
        order.setItems(new ArrayList<>());

        product = new Product(1L, "Test Product", "Test", 1000f,
                List.of("Food"), "Principal", 30, null, "");

        context = new PromotionContext(order);
    }

    @ParameterizedTest(name = "{4} - operator: {0}, quantity: {1}, threshold: {2}, expected: {3}")
    @CsvSource({
            // operator, quantity, threshold, expectedResult, description
            ">, 2, 1000, true, GreaterThan_ReturnsTrue",
            ">, 1, 2000, false, GreaterThan_ReturnsFalse",
            ">=, 2, 2000, true, GreaterThanOrEqual_ReturnsTrue",
            ">=, 1, 2000, false, GreaterThanOrEqual_ReturnsFalse",
            "<, 1, 2000, true, LessThan_ReturnsTrue",
            "<, 3, 2000, false, LessThan_ReturnsFalse",
            "<=, 2, 2000, true, LessThanOrEqual_ReturnsTrue",
            "<=, 3, 2000, false, LessThanOrEqual_ReturnsFalse",
            "==, 2, 2000, true, Equal_ReturnsTrue",
            "==, 1, 2000, false, Equal_ReturnsFalse",
            "!=, 1, 2000, true, NotEqual_ReturnsTrue",
            "!=, 2, 2000, false, NotEqual_ReturnsFalse"
    })
    void interpretWithOperators(String operator, int quantity, float threshold, boolean expectedResult,
            String description) {
        OrderItem item = new OrderItem(order, product, quantity);
        order.getItems().add(item);
        order.calculateTotal();

        TotalAmountExpression expression = new TotalAmountExpression(operator, threshold);
        assertEquals(expectedResult, expression.interpret(context),
                String.format("Failed for operator '%s' with quantity %d and threshold %.0f", operator, quantity,
                        threshold));
    }

    @Test
    void interpretWithUnknownOperatorThrowsException() {
        TotalAmountExpression expression = new TotalAmountExpression("??", 1000);
        assertThrows(IllegalArgumentException.class, () -> expression.interpret(context));
    }

    @Test
    void interpretWithDiscounts() {
        OrderItem item = new OrderItem(order, product, 2);
        item.setDiscount(500f);
        item.calculateSubtotal();
        order.getItems().add(item);
        order.calculateTotal();

        TotalAmountExpression expression = new TotalAmountExpression(">=", 1000);
        assertTrue(expression.interpret(context));
    }
}
