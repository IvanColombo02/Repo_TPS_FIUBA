package ar.uba.fi.ingsoft1.product_example.promotions.interpreter.action;

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

class QuantityDiscountActionTest {

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
    void applyWithBuy3Pay2() {
        OrderItem item = new OrderItem(order, product, 3);
        order.getItems().add(item);
        order.calculateTotal();

        QuantityDiscountAction action = new QuantityDiscountAction(3, 2);
        action.apply(context, order);

        assertEquals(1000f, item.getDiscount(), 0.01f);
        assertTrue(item.getPromotionApplied().contains("Lleva 3 paga 2"));
        assertEquals(2000f, item.getSubtotal(), 0.01f);
    }

    @Test
    void applyWithBuy2Pay1() {
        OrderItem item = new OrderItem(order, product, 2);
        order.getItems().add(item);
        order.calculateTotal();

        QuantityDiscountAction action = new QuantityDiscountAction(2, 1);
        action.apply(context, order);

        assertEquals(1000f, item.getDiscount(), 0.01f);
        assertEquals(1000f, item.getSubtotal(), 0.01f);
    }

    @Test
    void applyWithMultipleSets() {
        OrderItem item = new OrderItem(order, product, 6);
        order.getItems().add(item);
        order.calculateTotal();

        QuantityDiscountAction action = new QuantityDiscountAction(3, 2);
        action.apply(context, order);

        assertEquals(2000f, item.getDiscount(), 0.01f);
        assertEquals(4000f, item.getSubtotal(), 0.01f);
    }

    @Test
    void applyWithQuantityBelowBuyQuantity() {
        OrderItem item = new OrderItem(order, product, 2);
        order.getItems().add(item);
        order.calculateTotal();

        QuantityDiscountAction action = new QuantityDiscountAction(3, 2);
        action.apply(context, order);

        Float discount = item.getDiscount();
        assertTrue(discount == null || discount == 0f);
        assertEquals(2000f, item.getSubtotal(), 0.01f);
    }

    @Test
    void applyWithExistingDiscount() {
        OrderItem item = new OrderItem(order, product, 3);
        item.setDiscount(100f);
        item.calculateSubtotal();
        order.getItems().add(item);
        order.calculateTotal();

        QuantityDiscountAction action = new QuantityDiscountAction(3, 2);
        action.apply(context, order);

        assertEquals(1100f, item.getDiscount(), 0.01f);
    }

    @Test
    void applyWithMultipleItems() {
        Product product2 = new Product(2L, "Hamburguesa", "Test", 800f,
                List.of("Food"), "Principal", 20, null, "");
        OrderItem item1 = new OrderItem(order, product, 3);
        OrderItem item2 = new OrderItem(order, product2, 3);
        order.getItems().add(item1);
        order.getItems().add(item2);
        order.calculateTotal();

        QuantityDiscountAction action = new QuantityDiscountAction(3, 2);
        action.apply(context, order);

        assertEquals(1000f, item1.getDiscount(), 0.01f);
        assertEquals(800f, item2.getDiscount(), 0.01f);
    }

    @Test
    void applyWithBuy5Pay4() {
        OrderItem item = new OrderItem(order, product, 5);
        order.getItems().add(item);
        order.calculateTotal();

        QuantityDiscountAction action = new QuantityDiscountAction(5, 4);
        action.apply(context, order);

        assertEquals(1000f, item.getDiscount(), 0.01f);
        assertEquals(4000f, item.getSubtotal(), 0.01f);
    }

}
