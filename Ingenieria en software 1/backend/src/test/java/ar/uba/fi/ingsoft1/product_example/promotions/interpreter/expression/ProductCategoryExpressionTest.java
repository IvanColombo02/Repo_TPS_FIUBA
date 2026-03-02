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

class ProductCategoryExpressionTest {

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
    void interpretReturnsTrueWhenCategoryMatches() {
        OrderItem item = new OrderItem(order, product1, 1);
        order.getItems().add(item);
        order.calculateTotal();

        ProductCategoryExpression expression = new ProductCategoryExpression("Food");
        assertTrue(expression.interpret(context));
    }

    @Test
    void interpretReturnsFalseWhenCategoryDoesNotMatch() {
        OrderItem item = new OrderItem(order, product1, 1);
        order.getItems().add(item);
        order.calculateTotal();

        ProductCategoryExpression expression = new ProductCategoryExpression("Drink");
        assertFalse(expression.interpret(context));
    }

    @Test
    void interpretReturnsFalseWhenCategoriesIsNull() {
        Product productWithoutCategories = new Product(3L, "Test", "Test", 100f,
                null, "Principal", 10, null, "");
        OrderItem item = new OrderItem(order, productWithoutCategories, 1);
        order.getItems().add(item);
        order.calculateTotal();

        ProductCategoryExpression expression = new ProductCategoryExpression("Food");
        assertFalse(expression.interpret(context));
    }

    @Test
    void interpretReturnsFalseForEmptyCart() {
        ProductCategoryExpression expression = new ProductCategoryExpression("Food");
        assertFalse(expression.interpret(context));
    }

    @Test
    void interpretIsCaseSensitive() {
        OrderItem item = new OrderItem(order, product1, 1);
        order.getItems().add(item);
        order.calculateTotal();

        ProductCategoryExpression expression1 = new ProductCategoryExpression("Food");
        ProductCategoryExpression expression2 = new ProductCategoryExpression("food");
        ProductCategoryExpression expression3 = new ProductCategoryExpression("FOOD");

        assertTrue(expression1.interpret(context));
        assertFalse(expression2.interpret(context));
        assertFalse(expression3.interpret(context));
    }
}
