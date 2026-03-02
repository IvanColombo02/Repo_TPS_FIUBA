package ar.uba.fi.ingsoft1.product_example.promotions.interpreter.action;

import ar.uba.fi.ingsoft1.product_example.items.combos.Combo;
import ar.uba.fi.ingsoft1.product_example.items.combos.ComboRepository;
import ar.uba.fi.ingsoft1.product_example.items.products.Product;
import ar.uba.fi.ingsoft1.product_example.items.products.ProductRepository;
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
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class FreeProductActionTest {

    private Order order;
    private PromotionContext context;
    private ProductRepository productRepository;
    private ComboRepository comboRepository;
    private Product freeProduct;

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

        freeProduct = new Product(1L, "Free Pizza", "Free product", 1000f,
                List.of("Food"), "Principal", 30, null, "");

        productRepository = mock(ProductRepository.class);
        comboRepository = mock(ComboRepository.class);

        context = new PromotionContext(order);
    }

    @Test
    void applyAddsFreeProductWhenProductExists() {
        when(productRepository.findById(1L)).thenReturn(Optional.of(freeProduct));
        when(comboRepository.findById(1L)).thenReturn(Optional.empty());

        FreeProductAction action = new FreeProductAction(1L, 1, productRepository, comboRepository);
        action.apply(context, order);

        assertEquals(1, order.getItems().size());
        OrderItem freeItem = order.getItems().get(0);
        assertEquals(1000f, freeItem.getDiscount(), 0.01f);
        assertEquals(0f, freeItem.getSubtotal(), 0.01f);
        assertTrue(freeItem.getPromotionApplied().contains("Producto gratis"));
    }

    @Test
    void applyAddsFreeProductWithMultipleQuantity() {
        when(productRepository.findById(1L)).thenReturn(Optional.of(freeProduct));
        when(comboRepository.findById(1L)).thenReturn(Optional.empty());

        FreeProductAction action = new FreeProductAction(1L, 2, productRepository, comboRepository);
        action.apply(context, order);

        assertEquals(1, order.getItems().size());
        OrderItem freeItem = order.getItems().get(0);
        assertEquals(2, freeItem.getQuantity());
        assertEquals(2000f, freeItem.getDiscount(), 0.01f);
        assertEquals(0f, freeItem.getSubtotal(), 0.01f);
    }

    @Test
    void applyAddsFreeComboWhenComboExists() {
        Combo freeCombo = new Combo(2L, "Free Combo", "Free combo", 1500f,
                List.of("Food"), List.of("Principal"), null, "");
        when(productRepository.findById(2L)).thenReturn(Optional.empty());
        when(comboRepository.findById(2L)).thenReturn(Optional.of(freeCombo));

        FreeProductAction action = new FreeProductAction(2L, 1, productRepository, comboRepository);
        action.apply(context, order);

        assertEquals(1, order.getItems().size());
        OrderItem freeItem = order.getItems().get(0);
        assertEquals(1500f, freeItem.getDiscount(), 0.01f);
        assertEquals(0f, freeItem.getSubtotal(), 0.01f);
    }

    @Test
    void applyDoesNothingWhenProductAndComboDoNotExist() {
        when(productRepository.findById(999L)).thenReturn(Optional.empty());
        when(comboRepository.findById(999L)).thenReturn(Optional.empty());

        FreeProductAction action = new FreeProductAction(999L, 1, productRepository, comboRepository);
        action.apply(context, order);

        assertTrue(order.getItems().isEmpty());
    }

    @Test
    void applyAddsFreeProductToExistingItems() {
        Product existingProduct = new Product(2L, "Existing", "Test", 500f,
                List.of("Food"), "Principal", 20, null, "");
        OrderItem existingItem = new OrderItem(order, existingProduct, 1);
        order.getItems().add(existingItem);
        order.calculateTotal();

        when(productRepository.findById(1L)).thenReturn(Optional.of(freeProduct));
        when(comboRepository.findById(1L)).thenReturn(Optional.empty());

        FreeProductAction action = new FreeProductAction(1L, 1, productRepository, comboRepository);
        action.apply(context, order);

        assertEquals(2, order.getItems().size());
        OrderItem freeItem = order.getItems().stream()
                .filter(item -> item.getItemName().equals("Free Pizza"))
                .findFirst()
                .orElse(null);
        assertNotNull(freeItem);
        assertEquals(1000f, freeItem.getDiscount(), 0.01f);
    }

}
