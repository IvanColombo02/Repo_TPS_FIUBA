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

class ProductInCartExpressionTest {

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

    @Test
    void interpretReturnsTrueWhenProductInCart() {
        OrderItem item = new OrderItem(order, product, 1);
        order.getItems().add(item);
        order.calculateTotal();

        ProductInCartExpression expression = new ProductInCartExpression(1L);
        assertTrue(expression.interpret(context));
    }

    @Test
    void interpretReturnsFalseWhenProductNotInCart() {
        ProductInCartExpression expression = new ProductInCartExpression(999L);
        assertFalse(expression.interpret(context));
    }

    @Test
    void interpretReturnsFalseForEmptyCart() {
        ProductInCartExpression expression = new ProductInCartExpression(1L);
        assertFalse(expression.interpret(context));
    }
}
