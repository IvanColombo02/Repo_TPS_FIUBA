package ar.uba.fi.ingsoft1.product_example.promotions.interpreter;

import ar.uba.fi.ingsoft1.product_example.items.products.Product;
import ar.uba.fi.ingsoft1.product_example.order.Order;
import ar.uba.fi.ingsoft1.product_example.order.OrderItem;
import ar.uba.fi.ingsoft1.product_example.order.OrderStatus;
import ar.uba.fi.ingsoft1.product_example.order.PaymentMethod;
import ar.uba.fi.ingsoft1.product_example.user.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class PromotionContextTest {

    private Order order;
    private Product product1;
    private Product product2;
    private Product product3;
    private OrderItem item1;
    private OrderItem item2;
    private OrderItem item3;
    private PromotionContext context;

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

        product1 = new Product(1L, "Pizza", "Delicious pizza", 1000f,
                List.of("Food"), "Principal", 30, null, "");
        product2 = new Product(2L, "Hamburguesa", "Tasty burger", 800f,
                List.of("Food"), "Principal", 20, null, "");
        product3 = new Product(3L, "Coca Cola", "Refreshing drink", 200f,
                List.of("Drink"), "Bebida", 5, null, "");

        item1 = new OrderItem(order, product1, 2);
        item2 = new OrderItem(order, product2, 1);
        item3 = new OrderItem(order, product3, 3);

        order.getItems().add(item1);
        order.getItems().add(item2);
        order.getItems().add(item3);

        context = new PromotionContext(order);
    }

    @Test
    void constructorWithOrderSetsCurrentDate() {
        PromotionContext newContext = new PromotionContext(order);
        assertNotNull(newContext.getCurrentDate());
        assertEquals(order, newContext.getOrder());
    }

    @Test
    void getTotalAmountReturnsSumOfSubtotals() {
        double total = context.getTotalAmount();
        double expected = item1.getSubtotal() + item2.getSubtotal() + item3.getSubtotal();
        assertEquals(expected, total, 0.01);
    }

    @Test
    void getTotalAmountReturnsZeroForEmptyOrder() {
        Order emptyOrder = new Order();
        emptyOrder.setItems(new ArrayList<>());
        PromotionContext emptyContext = new PromotionContext(emptyOrder);
        assertEquals(0.0, emptyContext.getTotalAmount(), 0.01);
    }

    @Test
    void getItemsReturnsAllOrderItems() {
        List<OrderItem> items = context.getItems();
        assertEquals(3, items.size());
        assertTrue(items.contains(item1));
        assertTrue(items.contains(item2));
        assertTrue(items.contains(item3));
    }

    @Test
    void hasItemReturnsTrueWhenProductExists() {
        assertTrue(context.hasItem(1L));
        assertTrue(context.hasItem(2L));
        assertTrue(context.hasItem(3L));
    }

    @Test
    void hasItemReturnsFalseWhenProductDoesNotExist() {
        assertFalse(context.hasItem(999L));
        assertFalse(context.hasItem(0L));
    }

    @Test
    void hasItemByNameReturnsTrueWhenProductNameExists() {
        assertTrue(context.hasItemByName("Pizza"));
        assertTrue(context.hasItemByName("Hamburguesa"));
        assertTrue(context.hasItemByName("Coca Cola"));
    }

    @Test
    void hasItemByNameIsCaseInsensitive() {
        assertTrue(context.hasItemByName("pizza"));
        assertTrue(context.hasItemByName("PIZZA"));
        assertTrue(context.hasItemByName("PiZzA"));
    }

    @Test
    void hasItemByNameReturnsFalseWhenProductNameDoesNotExist() {
        assertFalse(context.hasItemByName("Non-existent Product"));
        assertFalse(context.hasItemByName(""));
    }

    @Test
    void getItemQuantityReturnsCorrectQuantityForSingleItem() {
        assertEquals(2, context.getItemQuantity(1L));
        assertEquals(1, context.getItemQuantity(2L));
        assertEquals(3, context.getItemQuantity(3L));
    }

    @Test
    void getItemQuantityReturnsSumForMultipleItemsWithSameProduct() {
        OrderItem duplicateItem = new OrderItem(order, product1, 1);
        order.getItems().add(duplicateItem);
        PromotionContext newContext = new PromotionContext(order);
        assertEquals(3, newContext.getItemQuantity(1L));
    }

    @Test
    void getItemQuantityReturnsZeroWhenProductDoesNotExist() {
        assertEquals(0, context.getItemQuantity(999L));
    }

    @Test
    void getItemsByCategoryReturnsItemsWithMatchingCategory() {
        List<OrderItem> foodItems = context.getItemsByCategory("Food");
        assertEquals(2, foodItems.size());
        assertTrue(foodItems.contains(item1));
        assertTrue(foodItems.contains(item2));
        assertFalse(foodItems.contains(item3));
    }

    @Test
    void getItemsByCategoryReturnsItemsWithMatchingCategoryCaseSensitive() {
        List<OrderItem> drinkItems = context.getItemsByCategory("Drink");
        assertEquals(1, drinkItems.size());
        assertTrue(drinkItems.contains(item3));
    }

    @Test
    void getItemsByCategoryReturnsEmptyListWhenNoMatch() {
        List<OrderItem> items = context.getItemsByCategory("Dessert");
        assertTrue(items.isEmpty());
    }

    @Test
    void getItemsByCategoryReturnsEmptyListWhenCategoriesIsNull() {
        Product productWithoutCategories = new Product(4L, "Test", "Test", 100f,
                null, "Principal", 10, null, "");
        OrderItem itemWithoutCategories = new OrderItem(order, productWithoutCategories, 1);
        order.getItems().add(itemWithoutCategories);
        PromotionContext newContext = new PromotionContext(order);

        List<OrderItem> items = newContext.getItemsByCategory("Food");
        assertEquals(2, items.size());
    }

    @Test
    void getItemsByTypeReturnsItemsWithMatchingType() {
        List<OrderItem> principalItems = context.getItemsByType("Principal");
        assertEquals(2, principalItems.size());
        assertTrue(principalItems.contains(item1));
        assertTrue(principalItems.contains(item2));
        assertFalse(principalItems.contains(item3));
    }

    @Test
    void getItemsByTypeReturnsItemsWithMatchingTypeCaseSensitive() {
        List<OrderItem> bebidaItems = context.getItemsByType("Bebida");
        assertEquals(1, bebidaItems.size());
        assertTrue(bebidaItems.contains(item3));
    }

    @Test
    void getItemsByTypeReturnsEmptyListWhenNoMatch() {
        List<OrderItem> items = context.getItemsByType("Dessert");
        assertTrue(items.isEmpty());
    }

    @Test
    void getItemsByTypeReturnsEmptyListWhenTypesIsNull() {
        Product productWithoutTypes = new Product(4L, "Test", "Test", 100f,
                List.of("Food"), null, 10, null, "");
        OrderItem itemWithoutTypes = new OrderItem(order, productWithoutTypes, 1);
        order.getItems().add(itemWithoutTypes);
        PromotionContext newContext = new PromotionContext(order);

        List<OrderItem> items = newContext.getItemsByType("Principal");
        assertEquals(2, items.size());
    }

    @Test
    void getItemsByProductIdReturnsItemsWithMatchingProductId() {
        List<OrderItem> items = context.getItemsByProductId(1L);
        assertEquals(1, items.size());
        assertTrue(items.contains(item1));
    }

    @Test
    void getItemsByProductIdReturnsMultipleItemsWithSameProductId() {
        OrderItem duplicateItem = new OrderItem(order, product1, 1);
        order.getItems().add(duplicateItem);
        PromotionContext newContext = new PromotionContext(order);

        List<OrderItem> items = newContext.getItemsByProductId(1L);
        assertEquals(2, items.size());
        assertTrue(items.contains(item1));
        assertTrue(items.contains(duplicateItem));
    }

    @Test
    void getItemsByProductIdReturnsEmptyListWhenProductDoesNotExist() {
        List<OrderItem> items = context.getItemsByProductId(999L);
        assertTrue(items.isEmpty());
    }

    @Test
    void getTotalAmountWithDiscounts() {
        item1.setDiscount(100f);
        item1.calculateSubtotal();
        double total = context.getTotalAmount();
        double expected = item1.getSubtotal() + item2.getSubtotal() + item3.getSubtotal();
        assertEquals(expected, total, 0.01);
    }

    @Test
    void getItemsReturnsEmptyListForEmptyOrder() {
        Order emptyOrder = new Order();
        emptyOrder.setItems(new ArrayList<>());
        PromotionContext emptyContext = new PromotionContext(emptyOrder);
        assertTrue(emptyContext.getItems().isEmpty());
    }
}
