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

class OrExpressionTest {

    private Order order;
    private PromotionContext context;
    private Product product1;
    private Product product2;

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

        product1 = new Product(1L, "Pizza", "Test", 1000f,
                List.of("Food"), "Principal", 30, null, "");
        product2 = new Product(2L, "Hamburguesa", "Test", 800f,
                List.of("Food"), "Principal", 20, null, "");

        context = new PromotionContext(order);
    }

    @ParameterizedTest(name = "Left product {0}, Right product {1}, Expected: {2}")
    @CsvSource({
            "1, 999, true", // left true
            "999, 2, true", // right true
            "1, 2, true" // both true
    })
    void interpretReturnsTrueWhenAnyOperandIsTrue(long leftProductId, long rightProductId, boolean expected) {
        if (leftProductId == 1) {
            order.getItems().add(new OrderItem(order, product1, 1));
        }
        if (rightProductId == 2) {
            order.getItems().add(new OrderItem(order, product2, 1));
        }
        order.calculateTotal();

        Expression left = new ProductInCartExpression(leftProductId);
        Expression right = new ProductInCartExpression(rightProductId);
        OrExpression expression = new OrExpression(left, right);

        assertEquals(expected, expression.interpret(context));
    }

    @Test
    void interpretReturnsFalseWhenBothAreFalse() {
        Expression left = new ProductInCartExpression(999L);
        Expression right = new ProductInCartExpression(888L);
        OrExpression expression = new OrExpression(left, right);

        assertFalse(expression.interpret(context));
    }

    @Test
    void interpretWithNestedOrExpressions() {
        OrderItem item = new OrderItem(order, product1, 1);
        order.getItems().add(item);
        order.calculateTotal();

        Expression innerLeft = new ProductInCartExpression(1L);
        Expression innerRight = new ProductInCartExpression(2L);
        Expression innerOr = new OrExpression(innerLeft, innerRight);

        Expression outerRight = new ProductNameExpression("Pizza");
        OrExpression outerOr = new OrExpression(innerOr, outerRight);

        assertTrue(outerOr.interpret(context));
    }

    @Test
    void interpretWithNestedOrExpressionsReturnsFalse() {
        Expression innerLeft = new ProductInCartExpression(999L);
        Expression innerRight = new ProductInCartExpression(888L);
        Expression innerOr = new OrExpression(innerLeft, innerRight);

        Expression outerRight = new ProductNameExpression("NonExistent");
        OrExpression outerOr = new OrExpression(innerOr, outerRight);

        assertFalse(outerOr.interpret(context));
    }
}
