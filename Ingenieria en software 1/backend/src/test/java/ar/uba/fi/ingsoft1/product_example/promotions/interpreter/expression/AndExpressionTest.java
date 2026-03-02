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

class AndExpressionTest {

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

        product = new Product(1L, "Pizza", "Test", 1000f,
                List.of("Food"), "Principal", 30, null, "");

        context = new PromotionContext(order);
    }

    @Test
    void interpretReturnsTrueWhenBothExpressionsAreTrue() {
        OrderItem item = new OrderItem(order, product, 2);
        order.getItems().add(item);
        order.calculateTotal();

        Expression left = new TotalAmountExpression(">=", 1000);
        Expression right = new ProductInCartExpression(1L);
        AndExpression expression = new AndExpression(left, right);

        assertTrue(expression.interpret(context));
    }

    @ParameterizedTest(name = "Quantity: {0}, Threshold: {1}, ProductId: {2}, Expected: {3}")
    @CsvSource({
        "1, 2000, 1, false",   // left false (amount < threshold)
        "2, 1000, 999, false",  // right false (product not in cart)
        "1, 2000, 999, false"   // both false
    })
    void interpretReturnsFalseWhenAnyOperandIsFalse(int quantity, double threshold, long productId, boolean expected) {
        OrderItem item = new OrderItem(order, product, quantity);
        order.getItems().add(item);
        order.calculateTotal();

        Expression left = new TotalAmountExpression(">=", threshold);
        Expression right = new ProductInCartExpression(productId);
        AndExpression expression = new AndExpression(left, right);

        assertEquals(expected, expression.interpret(context));
    }

    @Test
    void interpretWithNestedAndExpressions() {
        OrderItem item = new OrderItem(order, product, 2);
        order.getItems().add(item);
        order.calculateTotal();

        Expression innerLeft = new TotalAmountExpression(">=", 1000);
        Expression innerRight = new ProductInCartExpression(1L);
        Expression innerAnd = new AndExpression(innerLeft, innerRight);

        Expression outerRight = new ProductNameExpression("Pizza");
        AndExpression outerAnd = new AndExpression(innerAnd, outerRight);

        assertTrue(outerAnd.interpret(context));
    }

    @Test
    void interpretWithNestedAndExpressionsReturnsFalse() {
        OrderItem item = new OrderItem(order, product, 1);
        order.getItems().add(item);
        order.calculateTotal();

        Expression innerLeft = new TotalAmountExpression(">=", 2000);
        Expression innerRight = new ProductInCartExpression(1L);
        Expression innerAnd = new AndExpression(innerLeft, innerRight);

        Expression outerRight = new ProductNameExpression("Pizza");
        AndExpression outerAnd = new AndExpression(innerAnd, outerRight);

        assertFalse(outerAnd.interpret(context));
    }
}
