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
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class PercentageDiscountActionTest {

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

    @Test
    void applyToOrderWithSingleItem() {
        OrderItem item = new OrderItem(order, product1, 1);
        order.getItems().add(item);
        order.calculateTotal();

        PercentageDiscountAction action = new PercentageDiscountAction("ORDER", null, null, null, 10);
        action.apply(context, order);

        Float discount = item.getDiscount();
        assertNotNull(discount);
        assertEquals(100f, discount, 0.01f);
        String promotionApplied = item.getPromotionApplied();
        assertNotNull(promotionApplied);
        assertTrue(promotionApplied.contains("Descuento") || promotionApplied.contains("10"));
        assertEquals(900f, item.getSubtotal(), 0.01f);
    }

    @Test
    void applyToOrderWithMultipleItems() {
        OrderItem item1 = new OrderItem(order, product1, 1);
        OrderItem item2 = new OrderItem(order, product2, 1);
        order.getItems().add(item1);
        order.getItems().add(item2);
        order.calculateTotal();

        PercentageDiscountAction action = new PercentageDiscountAction("ORDER", null, null, null, 10);
        action.apply(context, order);

        Float discount1 = item1.getDiscount();
        Float discount2 = item2.getDiscount();
        assertNotNull(discount1);
        assertNotNull(discount2);
        assertTrue(discount1 > 0);
        assertTrue(discount2 > 0);
        double totalDiscount = discount1 + discount2;
        assertEquals(180f, totalDiscount, 0.01f);
    }

    @Test
    void applyToOrderDistributesProportionally() {
        OrderItem item1 = new OrderItem(order, product1, 1);
        OrderItem item2 = new OrderItem(order, product2, 1);
        order.getItems().add(item1);
        order.getItems().add(item2);
        order.calculateTotal();

        double subtotalBefore = 1000 + 800;
        double totalDiscountGoal = subtotalBefore * 0.10;

        PercentageDiscountAction action = new PercentageDiscountAction("ORDER", null, null, null, 10);
        action.apply(context, order);

        double item1Total = 1000.0;
        double expectedDiscount1 = (item1Total / subtotalBefore) * totalDiscountGoal;
        double expectedDiscount2 = totalDiscountGoal - expectedDiscount1;

        assertEquals(expectedDiscount1, item1.getDiscount(), 0.01f);
        assertEquals(expectedDiscount2, item2.getDiscount(), 0.01f);
        assertEquals(totalDiscountGoal, item1.getDiscount() + item2.getDiscount(), 0.01f);
    }

    @Test
    void applyToOrderLastItemReceivesRemainder() {
        Product product3 = new Product(3L, "Empanada", "Test", 500f,
                List.of("Food"), "Principal", 15, null, "");
        OrderItem item1 = new OrderItem(order, product1, 1);
        OrderItem item2 = new OrderItem(order, product2, 1);
        OrderItem item3 = new OrderItem(order, product3, 1);
        order.getItems().add(item1);
        order.getItems().add(item2);
        order.getItems().add(item3);
        order.calculateTotal();

        double subtotalBefore = 1000 + 800 + 500;
        double totalDiscountGoal = subtotalBefore * 0.10;

        PercentageDiscountAction action = new PercentageDiscountAction("ORDER", null, null, null, 10);
        action.apply(context, order);

        double totalDiscount = item1.getDiscount() + item2.getDiscount() + item3.getDiscount();
        assertEquals(totalDiscountGoal, totalDiscount, 0.01f);
    }

    @Test
    void applyToOrderWithExistingDiscountsDistributesCorrectly() {
        OrderItem item1 = new OrderItem(order, product1, 1);
        item1.setDiscount(100f);
        item1.calculateSubtotal();
        OrderItem item2 = new OrderItem(order, product2, 1);
        order.getItems().add(item1);
        order.getItems().add(item2);
        order.calculateTotal();

        double subtotalBefore = (1000 - 100) + 800;
        double totalDiscountGoal = subtotalBefore * 0.10;
        double existingDiscount = 100f;

        PercentageDiscountAction action = new PercentageDiscountAction("ORDER", null, null, null, 10);
        action.apply(context, order);

        double newDiscount1 = item1.getDiscount() - existingDiscount;
        double newDiscount2 = item2.getDiscount() != null ? item2.getDiscount() : 0f;
        double totalNewDiscount = newDiscount1 + newDiscount2;

        assertEquals(totalDiscountGoal, totalNewDiscount, 0.01f);
        assertEquals(100f + (900.0 / subtotalBefore) * totalDiscountGoal, item1.getDiscount(), 0.01f);
    }

    @Test
    void applyToOrderWithEmptyItems() {
        PercentageDiscountAction action = new PercentageDiscountAction("ORDER", null, null, null, 10);
        action.apply(context, order);

        assertTrue(order.getItems().isEmpty());
    }

    @Test
    void applyToOrderWithZeroPercentageKeepsDiscountsUnchanged() {
        OrderItem item1 = new OrderItem(order, product1, 1);
        OrderItem item2 = new OrderItem(order, product2, 1);
        order.getItems().add(item1);
        order.getItems().add(item2);
        order.calculateTotal();

        PercentageDiscountAction action = new PercentageDiscountAction("ORDER", null, null, null, 0);
        action.apply(context, order);

        assertNull(item1.getDiscount());
        assertNull(item2.getDiscount());
    }

    @Test
    void applyToOrderSkipsItemsWithZeroSubtotal() {
        OrderItem item1 = new OrderItem(order, product1, 1);
        OrderItem fullyDiscounted = new OrderItem(order, product2, 1);
        fullyDiscounted.setDiscount(fullyDiscounted.getItemPrice() * fullyDiscounted.getQuantity());
        fullyDiscounted.calculateSubtotal();
        Product product3 = new Product(3L, "Empanada", "Test", 500f,
                List.of("Food"), "Principal", 15, null, "");
        OrderItem item3 = new OrderItem(order, product3, 1);
        order.getItems().add(item1);
        order.getItems().add(fullyDiscounted);
        order.getItems().add(item3);
        order.calculateTotal();

        PercentageDiscountAction action = new PercentageDiscountAction("ORDER", null, null, null, 10);
        action.apply(context, order);

        assertNotNull(item1.getDiscount());
        assertNotNull(item3.getDiscount());
        assertEquals(fullyDiscounted.getItemPrice() * fullyDiscounted.getQuantity(),
                fullyDiscounted.getDiscount(), 0.01f);
    }

    @Test
    void applyToOrderItemByProductId() {
        OrderItem item1 = new OrderItem(order, product1, 1);
        OrderItem item2 = new OrderItem(order, product2, 1);
        order.getItems().add(item1);
        order.getItems().add(item2);
        order.calculateTotal();

        PercentageDiscountAction action = new PercentageDiscountAction("ORDER_ITEM", 1L, null, null, 15);
        action.apply(context, order);

        assertEquals(150f, item1.getDiscount(), 0.01f);
        Float discount2 = item2.getDiscount();
        assertTrue(discount2 == null || discount2 == 0f);
    }

    @Test
    void applyToOrderItemByCategory() {
        Product product3 = new Product(3L, "Coca Cola", "Test", 200f,
                List.of("Drink"), "Bebida", 5, null, "");
        OrderItem item1 = new OrderItem(order, product1, 1);
        OrderItem item2 = new OrderItem(order, product2, 1);
        OrderItem item3 = new OrderItem(order, product3, 1);
        order.getItems().add(item1);
        order.getItems().add(item2);
        order.getItems().add(item3);
        order.calculateTotal();

        PercentageDiscountAction action = new PercentageDiscountAction("ORDER_ITEM", null, "Food", null, 15);
        action.apply(context, order);

        Float discount1 = item1.getDiscount();
        Float discount2 = item2.getDiscount();
        Float discount3 = item3.getDiscount();
        assertNotNull(discount1);
        assertNotNull(discount2);
        assertTrue(discount1 > 0);
        assertTrue(discount2 > 0);
        assertTrue(discount3 == null || discount3 == 0f);
    }

    @Test
    void applyToOrderItemByType() {
        Product product3 = new Product(3L, "Coca Cola", "Test", 200f,
                List.of("Drink"), "Bebida", 5, null, "");
        OrderItem item1 = new OrderItem(order, product1, 1);
        OrderItem item2 = new OrderItem(order, product2, 1);
        OrderItem item3 = new OrderItem(order, product3, 1);
        order.getItems().add(item1);
        order.getItems().add(item2);
        order.getItems().add(item3);
        order.calculateTotal();

        PercentageDiscountAction action = new PercentageDiscountAction("ORDER_ITEM", null, null, "Principal", 10);
        action.apply(context, order);

        Float discount1 = item1.getDiscount();
        Float discount2 = item2.getDiscount();
        Float discount3 = item3.getDiscount();
        assertNotNull(discount1);
        assertNotNull(discount2);
        assertTrue(discount1 > 0);
        assertTrue(discount2 > 0);
        assertTrue(discount3 == null || discount3 == 0f);
    }

    @Test
    void applyToOrderItemWithExistingDiscount() {
        OrderItem item = new OrderItem(order, product1, 1);
        item.setDiscount(50f);
        item.calculateSubtotal();
        order.getItems().add(item);
        order.calculateTotal();

        PercentageDiscountAction action = new PercentageDiscountAction("ORDER_ITEM", 1L, null, null, 10);
        action.apply(context, order);

        assertTrue(item.getDiscount() > 50f);
    }

    @Test
    void applyToOrderItemSkipsWhenItemAlreadyHasFullDiscount() {
        OrderItem item = new OrderItem(order, product1, 1);
        float fullDiscount = item.getItemPrice() * item.getQuantity();
        item.setDiscount(fullDiscount);
        item.calculateSubtotal();
        order.getItems().add(item);
        order.calculateTotal();

        PercentageDiscountAction action = new PercentageDiscountAction("ORDER_ITEM", 1L, null, null, 10);
        action.apply(context, order);

        assertEquals(fullDiscount, item.getDiscount(), 0.01f);
    }

    @Test
    void applyToOrderItemWithMultipleQuantity() {
        OrderItem item = new OrderItem(order, product1, 3);
        order.getItems().add(item);
        order.calculateTotal();

        PercentageDiscountAction action = new PercentageDiscountAction("ORDER_ITEM", 1L, null, null, 10);
        action.apply(context, order);

        assertEquals(300f, item.getDiscount(), 0.01f);
        assertEquals(2700f, item.getSubtotal(), 0.01f);
    }

    @ParameterizedTest
    @ValueSource(doubles = { 5, 10, 15, 20, 25, 50 })
    void applyToOrderWithDifferentPercentages(double percentage) {
        OrderItem testItem = new OrderItem(order, product1, 1);
        Order testOrder = new Order();
        testOrder.setItems(new ArrayList<>());
        testOrder.getItems().add(testItem);
        testOrder.calculateTotal();
        PromotionContext testContext = new PromotionContext(testOrder);

        PercentageDiscountAction action = new PercentageDiscountAction("ORDER", null, null, null, percentage);
        action.apply(testContext, testOrder);

        double expectedDiscount = 1000 * (percentage / 100.0);
        assertEquals(expectedDiscount, testItem.getDiscount(), 0.01f);
    }

}
