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

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ProductTypeExpressionTest {

    private Order order;
    private PromotionContext context;
    private Product product1;

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

        context = new PromotionContext(order);
    }

    @Test
    void interpretReturnsTrueWhenTypeMatches() {
        OrderItem item = new OrderItem(order, product1, 1);
        order.getItems().add(item);
        order.calculateTotal();

        ProductTypeExpression expression = new ProductTypeExpression("Principal");
        assertTrue(expression.interpret(context));
    }

    @Test
    void interpretReturnsFalseWhenTypeDoesNotMatch() {
        OrderItem item = new OrderItem(order, product1, 1);
        order.getItems().add(item);
        order.calculateTotal();

        ProductTypeExpression expression = new ProductTypeExpression("Bebida");
        assertFalse(expression.interpret(context));
    }

    @Test
    void interpretReturnsFalseWhenTypesIsNull() {
        Product productWithoutTypes = new Product(3L, "Test", "Test", 100f,
                List.of("Food"), null, 10, null, "");
        OrderItem item = new OrderItem(order, productWithoutTypes, 1);
        order.getItems().add(item);
        order.calculateTotal();

        ProductTypeExpression expression = new ProductTypeExpression("Principal");
        assertFalse(expression.interpret(context));
    }

    @Test
    void interpretReturnsFalseForEmptyCart() {
        ProductTypeExpression expression = new ProductTypeExpression("Principal");
        assertFalse(expression.interpret(context));
    }

    @Test
    void interpretIsCaseSensitive() {
        OrderItem item = new OrderItem(order, product1, 1);
        order.getItems().add(item);
        order.calculateTotal();

        ProductTypeExpression expression1 = new ProductTypeExpression("Principal");
        ProductTypeExpression expression2 = new ProductTypeExpression("principal");
        ProductTypeExpression expression3 = new ProductTypeExpression("PRINCIPAL");

        assertTrue(expression1.interpret(context));
        assertFalse(expression2.interpret(context));
        assertFalse(expression3.interpret(context));
    }
}
