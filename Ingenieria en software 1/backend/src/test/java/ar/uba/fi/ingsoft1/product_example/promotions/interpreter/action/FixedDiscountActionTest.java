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

class FixedDiscountActionTest {

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

        FixedDiscountAction action = new FixedDiscountAction("ORDER", null, null, null, 100);
        action.apply(context, order);

        assertEquals(100f, item.getDiscount(), 0.01f);
        assertTrue(item.getPromotionApplied().contains("Descuento fijo"));
        assertEquals(900f, item.getSubtotal(), 0.01f);
    }

    @Test
    void applyToOrderWithMultipleItems() {
        OrderItem item1 = new OrderItem(order, product1, 1);
        OrderItem item2 = new OrderItem(order, product2, 1);
        order.getItems().add(item1);
        order.getItems().add(item2);
        order.calculateTotal();

        FixedDiscountAction action = new FixedDiscountAction("ORDER", null, null, null, 200);
        action.apply(context, order);

        Float discount1 = item1.getDiscount();
        Float discount2 = item2.getDiscount();
        float totalDiscount = (discount1 != null ? discount1 : 0f) + (discount2 != null ? discount2 : 0f);
        assertEquals(200f, totalDiscount, 0.01f);
        assertEquals(200f, discount1, 0.01f);
        assertTrue(discount2 == null || discount2 == 0f);
    }

    @Test
    void applyToOrderDistributesSequentially() {
        OrderItem item1 = new OrderItem(order, product1, 1);
        OrderItem item2 = new OrderItem(order, product2, 1);
        order.getItems().add(item1);
        order.getItems().add(item2);
        order.calculateTotal();

        FixedDiscountAction action = new FixedDiscountAction("ORDER", null, null, null, 500);
        action.apply(context, order);

        assertEquals(500f, item1.getDiscount(), 0.01f);
        Float discount2 = item2.getDiscount();
        assertTrue(discount2 == null || discount2 == 0f);
        float totalDiscount = item1.getDiscount() + (discount2 != null ? discount2 : 0f);
        assertEquals(500f, totalDiscount, 0.01f);
    }

    @Test
    void applyToOrderDistributesAcrossMultipleItems() {
        Product product3 = new Product(3L, "Empanada", "Test", 500f,
                List.of("Food"), "Principal", 15, null, "");
        OrderItem item1 = new OrderItem(order, product1, 1);
        OrderItem item2 = new OrderItem(order, product2, 1);
        OrderItem item3 = new OrderItem(order, product3, 1);
        order.getItems().add(item1);
        order.getItems().add(item2);
        order.getItems().add(item3);
        order.calculateTotal();

        FixedDiscountAction action = new FixedDiscountAction("ORDER", null, null, null, 1500);
        action.apply(context, order);

        assertEquals(1000f, item1.getDiscount(), 0.01f);
        assertEquals(500f, item2.getDiscount(), 0.01f);
        Float discount3 = item3.getDiscount();
        assertTrue(discount3 == null || discount3 == 0f);
        float totalDiscount = item1.getDiscount() + item2.getDiscount() + (discount3 != null ? discount3 : 0f);
        assertEquals(1500f, totalDiscount, 0.01f);
    }

    @Test
    void applyToOrderWithExistingDiscountsDistributesCorrectly() {
        OrderItem item1 = new OrderItem(order, product1, 1);
        item1.setDiscount(200f);
        item1.calculateSubtotal();
        OrderItem item2 = new OrderItem(order, product2, 1);
        order.getItems().add(item1);
        order.getItems().add(item2);
        order.calculateTotal();

        FixedDiscountAction action = new FixedDiscountAction("ORDER", null, null, null, 500);
        action.apply(context, order);

        double item1Remaining = 1000 - 200;
        double expectedDiscount1 = Math.min(item1Remaining, 500);
        double expectedDiscount2 = 500 - expectedDiscount1;

        assertEquals(200f + expectedDiscount1, item1.getDiscount(), 0.01f);
        Float discount2 = item2.getDiscount();
        if (expectedDiscount2 > 0) {
            assertNotNull(discount2);
            assertEquals(expectedDiscount2, discount2, 0.01f);
        } else {
            assertTrue(discount2 == null || discount2 == 0f);
        }
        float totalNewDiscount = item1.getDiscount() + (discount2 != null ? discount2 : 0f) - 200f;
        assertEquals(500f, totalNewDiscount, 0.01f);
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

        FixedDiscountAction action = new FixedDiscountAction("ORDER", null, null, null, 200);
        action.apply(context, order);

        Float discount1 = item1.getDiscount();
        Float discount3 = item3.getDiscount();
        float totalDiscount = (discount1 != null ? discount1 : 0f) + (discount3 != null ? discount3 : 0f);
        assertEquals(200f, totalDiscount, 0.01f);
        assertEquals(fullyDiscounted.getItemPrice() * fullyDiscounted.getQuantity(),
                fullyDiscounted.getDiscount(), 0.01f);
    }

    @Test
    void applyToOrderWithEmptyItems() {
        FixedDiscountAction action = new FixedDiscountAction("ORDER", null, null, null, 100);
        action.apply(context, order);

        assertTrue(order.getItems().isEmpty());
    }

    @Test
    void applyToOrderWithZeroAmountKeepsDiscountsNull() {
        OrderItem item1 = new OrderItem(order, product1, 1);
        OrderItem item2 = new OrderItem(order, product2, 1);
        order.getItems().add(item1);
        order.getItems().add(item2);
        order.calculateTotal();

        FixedDiscountAction action = new FixedDiscountAction("ORDER", null, null, null, 0);
        action.apply(context, order);

        assertNull(item1.getDiscount());
        assertNull(item2.getDiscount());
    }

    @Test
    void applyToOrderWithDiscountGreaterThanTotal() {
        OrderItem item = new OrderItem(order, product1, 1);
        order.getItems().add(item);
        order.calculateTotal();

        FixedDiscountAction action = new FixedDiscountAction("ORDER", null, null, null, 2000);
        action.apply(context, order);

        assertEquals(1000f, item.getDiscount(), 0.01f);
        assertEquals(0f, item.getSubtotal(), 0.01f);
    }

    @Test
    void applyToOrderItemByProductId() {
        OrderItem item1 = new OrderItem(order, product1, 1);
        OrderItem item2 = new OrderItem(order, product2, 1);
        order.getItems().add(item1);
        order.getItems().add(item2);
        order.calculateTotal();

        FixedDiscountAction action = new FixedDiscountAction("ORDER_ITEM", 1L, null, null, 100);
        action.apply(context, order);

        assertEquals(100f, item1.getDiscount(), 0.01f);
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

        FixedDiscountAction action = new FixedDiscountAction("ORDER_ITEM", null, "Food", null, 50);
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

        FixedDiscountAction action = new FixedDiscountAction("ORDER_ITEM", null, null, "Principal", 50);
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

        FixedDiscountAction action = new FixedDiscountAction("ORDER_ITEM", 1L, null, null, 100);
        action.apply(context, order);

        assertEquals(150f, item.getDiscount(), 0.01f);
    }

    @Test
    void applyToOrderItemWithZeroAmountDoesNothing() {
        OrderItem item = new OrderItem(order, product1, 1);
        order.getItems().add(item);
        order.calculateTotal();

        FixedDiscountAction action = new FixedDiscountAction("ORDER_ITEM", 1L, null, null, 0);
        action.apply(context, order);

        assertNull(item.getDiscount());
    }
}
