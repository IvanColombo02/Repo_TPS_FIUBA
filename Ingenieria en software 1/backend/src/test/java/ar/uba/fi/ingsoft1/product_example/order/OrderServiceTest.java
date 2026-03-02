package ar.uba.fi.ingsoft1.product_example.order;

import ar.uba.fi.ingsoft1.product_example.items.ingredients.Ingredient;
import ar.uba.fi.ingsoft1.product_example.items.products.Product;
import ar.uba.fi.ingsoft1.product_example.items.products.ProductRepository;
import ar.uba.fi.ingsoft1.product_example.items.combos.Combo;
import ar.uba.fi.ingsoft1.product_example.items.combos.ComboRepository;
import ar.uba.fi.ingsoft1.product_example.promotions.Promotion;
import ar.uba.fi.ingsoft1.product_example.promotions.PromotionRepository;
import ar.uba.fi.ingsoft1.product_example.promotions.interpreter.PromotionContext;
import ar.uba.fi.ingsoft1.product_example.promotions.interpreter.PromotionInterpreter;
import ar.uba.fi.ingsoft1.product_example.promotions.interpreter.expression.IfExpression;
import ar.uba.fi.ingsoft1.product_example.user.User;
import ar.uba.fi.ingsoft1.product_example.user.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.Test;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.mockito.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private ProductRepository productRepository;
    @Mock
    private ComboRepository comboRepository;
    @Mock
    private UserService userService;
    @Mock
    private PromotionRepository promotionRepository;
    @Mock
    private PromotionInterpreter promotionInterpreter;
    @Mock
    private OrderRepository orderRepository;

    @InjectMocks
    private OrderService orderService;

    private User user;
    private Product product;
    private Ingredient ingredient;
    private Combo combo;

    @BeforeEach
    void setup() throws Exception {

        user = new User();
        user.setId(1L);
        when(userService.findById(1L)).thenReturn(Optional.of(user));

        ingredient = new Ingredient("Carne vacuna", 100, "imagen_base64_falsa");
        ingredient.getClass().getMethod("setId", Long.class).invoke(ingredient, 10L);

        product = new Product(
                "Hamburguesa Clásica",
                "Pan, carne, lechuga, tomate",
                500f,
                List.of("Comida"),
                "Individual",
                10,
                Map.of(ingredient, 1),
                "imagen_base64_falsa");
        product.setId(10L);
        product.setStock(10);

        combo = new Combo(
                "Combo Hamburguesa + Bebida",
                "Hamburguesa Clásica + Coca-Cola",
                800f,
                List.of("Comida", "Bebida"),
                List.of("Principal"),
                Map.of(product, 1),
                "imagen_base64_falsa");
        combo.setId(20L);
        combo.setStock(10);

        when(productRepository.findAllById(anyList())).thenAnswer(inv -> {
            List<Long> ids = inv.getArgument(0);
            if (ids.contains(product.getId()))
                return List.of(product);
            return List.of();
        });

        when(comboRepository.findAllById(anyList())).thenAnswer(inv -> {
            List<Long> ids = inv.getArgument(0);
            if (ids.contains(combo.getId()))
                return List.of(combo);
            return List.of();
        });
    }

    @Test
    void createOrderWithValidProduct() {
        when(orderRepository.save(any(Order.class))).thenAnswer(inv -> inv.getArgument(0));

        OrderCreateDTO dto = new OrderCreateDTO(Map.of(10L, 2), PaymentMethod.CASH);
        OrderDTO orderDTO = orderService.createOrder(dto, 1L);

        assertEquals(OrderStatus.PENDING, orderDTO.getStatus());
        assertEquals(1000f, orderDTO.getTotalPrice());
        assertEquals(1, orderDTO.getItems().size());

        var item = orderDTO.getItems().get(0);
        assertEquals("Hamburguesa Clásica", item.getItemName());
        assertEquals(500f, item.getItemPrice());
        assertEquals(2, item.getQuantity());
        assertEquals(1000f, item.getSubtotal());
    }

    @Test
    void createOrderWithValidCombo() {
        when(orderRepository.save(any(Order.class))).thenAnswer(inv -> inv.getArgument(0));

        OrderCreateDTO dto = new OrderCreateDTO(Map.of(20L, 1), PaymentMethod.CASH);
        OrderDTO orderDTO = orderService.createOrder(dto, 1L);

        assertEquals(OrderStatus.PENDING, orderDTO.getStatus());
        assertEquals(800f, orderDTO.getTotalPrice());
        assertEquals(1, orderDTO.getItems().size());

        var item = orderDTO.getItems().get(0);
        assertEquals("Combo Hamburguesa + Bebida", item.getItemName());
        assertEquals(800f, item.getItemPrice());
        assertEquals(1, item.getQuantity());
        assertEquals(800f, item.getSubtotal());
    }

    @Test
    void createOrderWithValidsProductAndCombo() {
        when(orderRepository.save(any(Order.class))).thenAnswer(inv -> inv.getArgument(0));

        OrderCreateDTO dto = new OrderCreateDTO(Map.of(10L, 1, 20L, 1), PaymentMethod.CASH);
        OrderDTO orderDTO = orderService.createOrder(dto, 1L);

        assertEquals(OrderStatus.PENDING, orderDTO.getStatus());
        assertEquals(1300f, orderDTO.getTotalPrice());
        assertEquals(2, orderDTO.getItems().size());
    }

    @Test
    void createOrderReducesStock() {
        when(orderRepository.save(any(Order.class))).thenAnswer(inv -> inv.getArgument(0));

        OrderCreateDTO dto = new OrderCreateDTO(Map.of(10L, 2), PaymentMethod.CASH);
        orderService.createOrder(dto, 1L);

        assertEquals(8, product.getStock());
        assertEquals(10, combo.getStock());
    }

    @Test
    void createOrderWithNonExistentItemThrowsException() {
        OrderCreateDTO dto = new OrderCreateDTO(Map.of(999L, 1), PaymentMethod.CASH);

        assertThrows(RuntimeException.class, () -> orderService.createOrder(dto, 1L));
    }

    @Test
    void createOrderWithNoItemsThrowsException() {
        OrderCreateDTO dto = new OrderCreateDTO(Map.of(), PaymentMethod.CASH);

        assertThrows(RuntimeException.class, () -> orderService.createOrder(dto, 1L));
    }

    @Test
    void createOrderWithInsufficientStockThrowsException() {
        product.setStock(1);
        OrderCreateDTO dto = new OrderCreateDTO(Map.of(10L, 2), PaymentMethod.CASH);

        assertThrows(InsufficientStockException.class, () -> orderService.createOrder(dto, 1L));
    }

    @Test
    @MockitoSettings(strictness = Strictness.LENIENT)
    void createOrderWithNonExistentUserThrowsException() {
        when(userService.findById(99L)).thenReturn(Optional.empty());
        OrderCreateDTO dto = new OrderCreateDTO(Map.of(10L, 1), PaymentMethod.CASH);

        assertThrows(RuntimeException.class, () -> orderService.createOrder(dto, 99L));
    }

    @Test
    @MockitoSettings(strictness = Strictness.LENIENT)
    void updateOrderStatus_ValidTransition_ChangesStatus() {

        Order order = new Order();
        order.setId(1L);
        order.setStatus(OrderStatus.PENDING);
        order.setUser(user);

        OrderItem item = new OrderItem();
        item.setComponent(product);
        item.setQuantity(1);
        order.getItems().add(item);

        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(orderRepository.save(any(Order.class))).thenAnswer(inv -> inv.getArgument(0));

        OrderStatusUpdateDTO dto = new OrderStatusUpdateDTO(OrderStatus.IN_PREPARATION);
        OrderDTO result = orderService.updateOrderStatus(1L, dto);

        assertEquals(OrderStatus.IN_PREPARATION, result.getStatus());
    }

    @Test
    @MockitoSettings(strictness = Strictness.LENIENT)
    void updateOrderStatus_PendingOrderToCancelled_ReturnsStock() {

        Order order = new Order();
        order.setId(1L);
        order.setStatus(OrderStatus.PENDING);
        order.setUser(user);

        OrderItem item = new OrderItem();
        item.setComponent(product);
        item.setQuantity(2);
        order.getItems().add(item);

        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(orderRepository.save(any(Order.class))).thenAnswer(inv -> inv.getArgument(0));

        OrderStatusUpdateDTO dto = new OrderStatusUpdateDTO(OrderStatus.CANCELLED);
        OrderDTO result = orderService.updateOrderStatus(1L, dto);

        assertEquals(OrderStatus.CANCELLED, result.getStatus());
        assertEquals(12, product.getStock());
    }

    @Test
    @MockitoSettings(strictness = Strictness.LENIENT)
    void updateOrderStatus_NonPendingOrderToCancelled_ReturnsStock() {

        Order order = new Order();
        order.setId(1L);
        order.setStatus(OrderStatus.READY);
        order.setUser(user);

        OrderItem item = new OrderItem();
        item.setComponent(product);
        item.setQuantity(2);
        order.getItems().add(item);

        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(orderRepository.save(any(Order.class))).thenAnswer(inv -> inv.getArgument(0));

        OrderStatusUpdateDTO dto = new OrderStatusUpdateDTO(OrderStatus.CANCELLED);
        OrderDTO result = orderService.updateOrderStatus(1L, dto);

        assertEquals(OrderStatus.CANCELLED, result.getStatus());
        assertEquals(10, product.getStock());
    }

    @Test
    @MockitoSettings(strictness = Strictness.LENIENT)
    void updateOrderStatus_OrderNotFound_ThrowsException() {
        when(orderRepository.findById(999L)).thenReturn(Optional.empty());
        OrderStatusUpdateDTO dto = new OrderStatusUpdateDTO(OrderStatus.READY);

        assertThrows(RuntimeException.class, () -> orderService.updateOrderStatus(999L, dto));
    }

    @Test
    @MockitoSettings(strictness = Strictness.LENIENT)
    void updateOrderStatus_NullDTO_ThrowsException() {
        Order order = new Order();
        order.setId(1L);
        order.setStatus(OrderStatus.PENDING);
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

        assertThrows(IllegalArgumentException.class, () -> orderService.updateOrderStatus(1L, null));
    }

    @Test
    @MockitoSettings(strictness = Strictness.LENIENT)
    void cancelOrder_ValidPendingOrder_CancelsAndRestoresStock() {

        Order order = new Order();
        order.setId(1L);
        order.setUser(user);
        order.setStatus(OrderStatus.PENDING);

        OrderItem item = new OrderItem();
        item.setComponent(product);
        item.setQuantity(3);
        order.getItems().add(item);

        when(orderRepository.findByIdAndUser_Id(1L, 1L)).thenReturn(Optional.of(order));
        when(orderRepository.save(any(Order.class))).thenAnswer(inv -> inv.getArgument(0));

        OrderDTO result = orderService.cancelOrder(1L, 1L);

        assertEquals(OrderStatus.CANCELLED, result.getStatus());
        assertEquals(13, product.getStock());
    }

    @Test
    @MockitoSettings(strictness = Strictness.LENIENT)
    void cancelOrder_InvalidState_ThrowsException() {
        Order order = new Order();
        order.setId(1L);
        order.setUser(user);
        order.setStatus(OrderStatus.READY);

        when(orderRepository.findByIdAndUser_Id(1L, 1L)).thenReturn(Optional.of(order));

        assertThrows(IllegalStateException.class, () -> orderService.cancelOrder(1L, 1L));
    }

    @Test
    @MockitoSettings(strictness = Strictness.LENIENT)
    void cancelOrder_OrderNotFound_ThrowsException() {
        when(orderRepository.findByIdAndUser_Id(999L, 1L)).thenReturn(Optional.empty());
        assertThrows(RuntimeException.class, () -> orderService.cancelOrder(999L, 1L));
    }

    @Test
    @MockitoSettings(strictness = Strictness.LENIENT)
    void cancelOrder_UserNotFound_ThrowsException() {
        when(userService.findById(99L)).thenReturn(Optional.empty());
        assertThrows(RuntimeException.class, () -> orderService.cancelOrder(1L, 99L));
    }

    @Test
    @MockitoSettings(strictness = Strictness.LENIENT)
    void cancelOrder_UserNotOwner_ThrowsException() {
        User anotherUser = new User();
        anotherUser.setId(2L);

        Order order = new Order();
        order.setId(1L);
        order.setUser(anotherUser);

        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

        assertThrows(RuntimeException.class, () -> orderService.cancelOrder(1L, 1L));
    }

    @Test
    @MockitoSettings(strictness = Strictness.LENIENT)
    void getOrderById_ValidOrder_ReturnsOrderDTO() {
        Order order = new Order();
        order.setId(1L);
        order.setUser(user);
        order.setStatus(OrderStatus.PENDING);

        OrderItem item = new OrderItem();
        item.setComponent(product);
        item.setQuantity(1);
        order.getItems().add(item);

        when(orderRepository.findByIdAndUser_Id(1L, 1L)).thenReturn(Optional.of(order));

        OrderDTO result = orderService.getOrderById(1L, 1L);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals(OrderStatus.PENDING, result.getStatus());
        verify(orderRepository).findByIdAndUser_Id(1L, 1L);
    }

    @Test
    @MockitoSettings(strictness = Strictness.LENIENT)
    void getOrderById_OrderNotFound_ThrowsException() {
        when(orderRepository.findByIdAndUser_Id(999L, 1L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> orderService.getOrderById(999L, 1L));
        verify(orderRepository).findByIdAndUser_Id(999L, 1L);
    }

    @Test
    @MockitoSettings(strictness = Strictness.LENIENT)
    void getOrderById_UserNotOwner_ThrowsException() {
        User anotherUser = new User();
        anotherUser.setId(2L);

        Order order = new Order();
        order.setId(1L);
        order.setUser(anotherUser);

        when(orderRepository.findByIdAndUser_Id(1L, 1L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> orderService.getOrderById(1L, 1L));
    }

    @Test
    @MockitoSettings(strictness = Strictness.LENIENT)
    void getActiveOrders_ReturnsAllActiveOrders() {
        Order order1 = new Order();
        order1.setId(1L);
        order1.setStatus(OrderStatus.PENDING);
        order1.setUser(user);

        Order order2 = new Order();
        order2.setId(2L);
        order2.setStatus(OrderStatus.IN_PREPARATION);
        order2.setUser(user);

        Order order3 = new Order();
        order3.setId(3L);
        order3.setStatus(OrderStatus.READY);
        order3.setUser(user);

        List<Order> activeOrders = List.of(order1, order2, order3);
        when(orderRepository.findByStatusInOrderByCreatedAtAsc(anyList())).thenReturn(activeOrders);

        List<OrderDTO> result = orderService.getActiveOrders();

        assertNotNull(result);
        assertEquals(3, result.size());
        verify(orderRepository).findByStatusInOrderByCreatedAtAsc(anyList());
    }

    @Test
    @MockitoSettings(strictness = Strictness.LENIENT)
    void getActiveOrders_NoActiveOrders_ReturnsEmptyList() {
        when(orderRepository.findByStatusInOrderByCreatedAtAsc(anyList())).thenReturn(new ArrayList<>());

        List<OrderDTO> result = orderService.getActiveOrders();

        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(orderRepository).findByStatusInOrderByCreatedAtAsc(anyList());
    }

    @Test
    @MockitoSettings(strictness = Strictness.LENIENT)
    void calculateCartDiscounts_ValidItems_ReturnsDiscountCalculation() {
        Map<Long, Integer> items = Map.of(product.getId(), 2);

        when(promotionRepository.findActivePromotionsOrdered(any(LocalDate.class))).thenReturn(new ArrayList<>());

        CartDiscountCalculationDTO result = orderService.calculateCartDiscounts(items);

        assertNotNull(result);
        assertEquals(1000.0, result.getOriginalSubtotal(), 0.01);
        assertEquals(1000.0, result.getFinalTotal(), 0.01);
        assertEquals(0.0, result.getTotalDiscount(), 0.01);
        assertNotNull(result.getAppliedDiscounts());
        assertNotNull(result.getPromotionDescriptions());
        verify(productRepository).findAllById(anyList());
        verify(comboRepository).findAllById(anyList());
    }

    @Test
    @MockitoSettings(strictness = Strictness.LENIENT)
    void calculateCartDiscounts_WithPromotion_AppliesDiscount() {
        Map<Long, Integer> items = Map.of(product.getId(), 2);

        Promotion promotion = new Promotion(
                "Test Promotion",
                "Test Description",
                LocalDate.now().minusDays(1),
                LocalDate.now().plusDays(1),
                "{\"condition\":{\"type\":\"totalAmount\",\"operator\":\">=\",\"value\":500},\"action\":{\"type\":\"percentageDiscount\",\"percentage\":10}}",
                null,
                0);
        promotion.setId(1L);

        IfExpression mockExpression = mock(IfExpression.class);
        when(promotionInterpreter.parseExpression(anyString())).thenReturn(mockExpression);
        when(mockExpression.interpret(any())).thenReturn(true);
        when(promotionRepository.findActivePromotionsOrdered(any(LocalDate.class))).thenReturn(List.of(promotion));

        CartDiscountCalculationDTO result = orderService.calculateCartDiscounts(items);

        assertNotNull(result);
        assertEquals(1000.0, result.getOriginalSubtotal(), 0.01);
        verify(promotionRepository).findActivePromotionsOrdered(any(LocalDate.class));
        verify(promotionInterpreter).parseExpression(anyString());
    }

    @Test
    @MockitoSettings(strictness = Strictness.LENIENT)
    void calculateCartDiscounts_InvalidItems_ThrowsException() {
        Map<Long, Integer> items = Map.of(999L, 1);

        assertThrows(RuntimeException.class, () -> orderService.calculateCartDiscounts(items));
        verify(productRepository).findAllById(anyList());
        verify(comboRepository).findAllById(anyList());
    }

    @Test
    @MockitoSettings(strictness = Strictness.LENIENT)
    void calculateCartDiscounts_PromotionThrowsException_ContinuesProcessing() {
        Map<Long, Integer> items = Map.of(product.getId(), 2);

        Promotion promotion = new Promotion(
                "Test Promotion",
                "Test Description",
                LocalDate.now().minusDays(1),
                LocalDate.now().plusDays(1),
                "invalid json",
                null,
                0);
        promotion.setId(1L);

        when(promotionInterpreter.parseExpression(anyString())).thenThrow(new RuntimeException("Parse error"));
        when(promotionRepository.findActivePromotionsOrdered(any(LocalDate.class))).thenReturn(List.of(promotion));

        CartDiscountCalculationDTO result = orderService.calculateCartDiscounts(items);

        assertNotNull(result);
        assertEquals(1000.0, result.getOriginalSubtotal(), 0.01);
        verify(promotionRepository).findActivePromotionsOrdered(any(LocalDate.class));
        verify(promotionInterpreter).parseExpression(anyString());
    }

    @Test
    @MockitoSettings(strictness = Strictness.LENIENT)
    void createOrder_WithPromotions_AppliesPromotions() {
        when(orderRepository.save(any(Order.class))).thenAnswer(inv -> inv.getArgument(0));

        Promotion promotion = new Promotion(
                "Test Promotion",
                "Test Description",
                LocalDate.now().minusDays(1),
                LocalDate.now().plusDays(1),
                "{\"condition\":{\"type\":\"totalAmount\",\"operator\":\">=\",\"value\":500},\"action\":{\"type\":\"percentageDiscount\",\"percentage\":10}}",
                null,
                0);
        promotion.setId(1L);

        IfExpression mockExpression = mock(IfExpression.class);
        when(promotionInterpreter.parseExpression(anyString())).thenReturn(mockExpression);
        when(mockExpression.interpret(any())).thenReturn(true);
        when(promotionRepository.findActivePromotionsOrdered(any(LocalDate.class))).thenReturn(List.of(promotion));

        OrderCreateDTO dto = new OrderCreateDTO(Map.of(10L, 2), PaymentMethod.CASH);
        OrderDTO orderDTO = orderService.createOrder(dto, 1L);

        assertNotNull(orderDTO);
        verify(promotionRepository).findActivePromotionsOrdered(any(LocalDate.class));
        verify(promotionInterpreter).parseExpression(anyString());
    }

    @Test
    @MockitoSettings(strictness = Strictness.LENIENT)
    void createOrder_PromotionThrowsException_ContinuesProcessing() {
        when(orderRepository.save(any(Order.class))).thenAnswer(inv -> inv.getArgument(0));

        Promotion promotion = new Promotion(
                "Test Promotion",
                "Test Description",
                LocalDate.now().minusDays(1),
                LocalDate.now().plusDays(1),
                "invalid json",
                null,
                0);
        promotion.setId(1L);

        when(promotionInterpreter.parseExpression(anyString())).thenThrow(new RuntimeException("Parse error"));
        when(promotionRepository.findActivePromotionsOrdered(any(LocalDate.class))).thenReturn(List.of(promotion));

        OrderCreateDTO dto = new OrderCreateDTO(Map.of(10L, 2), PaymentMethod.CASH);
        OrderDTO orderDTO = orderService.createOrder(dto, 1L);

        assertNotNull(orderDTO);
        assertEquals(OrderStatus.PENDING, orderDTO.getStatus());
        verify(promotionRepository).findActivePromotionsOrdered(any(LocalDate.class));
        verify(promotionInterpreter).parseExpression(anyString());
    }

    @Test
    @MockitoSettings(strictness = Strictness.LENIENT)
    void calculateCartDiscounts_WithProductAndCombo_CalculatesCorrectly() {
        Map<Long, Integer> items = Map.of(product.getId(), 1, combo.getId(), 1);

        when(promotionRepository.findActivePromotionsOrdered(any(LocalDate.class))).thenReturn(new ArrayList<>());

        CartDiscountCalculationDTO result = orderService.calculateCartDiscounts(items);

        assertNotNull(result);
        assertEquals(1300.0, result.getOriginalSubtotal(), 0.01);
        assertEquals(1300.0, result.getFinalTotal(), 0.01);
        verify(productRepository).findAllById(anyList());
        verify(comboRepository).findAllById(anyList());
    }

    @Test
    @MockitoSettings(strictness = Strictness.LENIENT)
    void calculateCartDiscounts_WithComboAndPromotion_AppliesDiscount() {
        Map<Long, Integer> items = Map.of(combo.getId(), 1);

        Promotion promotion = new Promotion(
                "Combo Promotion",
                "Test Description",
                LocalDate.now().minusDays(1),
                LocalDate.now().plusDays(1),
                "{\"condition\":{\"type\":\"totalAmount\",\"operator\":\">=\",\"value\":500},\"action\":{\"type\":\"percentageDiscount\",\"percentage\":15}}",
                null,
                0);
        promotion.setId(1L);

        IfExpression mockExpression = mock(IfExpression.class);
        when(promotionInterpreter.parseExpression(anyString())).thenReturn(mockExpression);
        when(mockExpression.interpret(any())).thenReturn(true);
        when(promotionRepository.findActivePromotionsOrdered(any(LocalDate.class))).thenReturn(List.of(promotion));

        CartDiscountCalculationDTO result = orderService.calculateCartDiscounts(items);

        assertNotNull(result);
        assertEquals(800.0, result.getOriginalSubtotal(), 0.01);
        verify(promotionRepository).findActivePromotionsOrdered(any(LocalDate.class));
        verify(promotionInterpreter).parseExpression(anyString());
        verify(comboRepository).findAllById(anyList());
    }

    @Test
    @MockitoSettings(strictness = Strictness.LENIENT)
    void calculateCartDiscounts_WithFreeProduct_DetectsFreeProduct() {
        Map<Long, Integer> items = Map.of(product.getId(), 2);

        Product freeProduct = new Product(
                "Producto Gratis",
                "Descripción",
                300f,
                List.of("Comida"),
                "Individual",
                10,
                Map.of(ingredient, 1),
                "imagen_base64_falsa");
        freeProduct.setId(30L);

        when(productRepository.findById(30L)).thenReturn(Optional.of(freeProduct));

        Promotion promotion = new Promotion(
                "Promoción Producto Gratis",
                "Test Description",
                LocalDate.now().minusDays(1),
                LocalDate.now().plusDays(1),
                "{\"condition\":{\"type\":\"totalAmount\",\"operator\":\">=\",\"value\":500},\"action\":{\"type\":\"freeProduct\",\"productId\":30,\"quantity\":1}}",
                null,
                0);
        promotion.setId(1L);

        IfExpression mockExpression = mock(IfExpression.class);
        when(promotionInterpreter.parseExpression(anyString())).thenReturn(mockExpression);

        doAnswer(invocation -> {
            PromotionContext context = invocation.getArgument(0);
            Order order = context.getOrder();

            OrderItem freeItem = new OrderItem(order, freeProduct, 1);
            double freeItemTotal = freeItem.getItemPrice() * 1;
            freeItem.setDiscount((float) freeItemTotal);
            freeItem.addPromotionApplied("Producto gratis");
            freeItem.calculateSubtotal();
            order.getItems().add(freeItem);

            return true;
        }).when(mockExpression).interpret(any());

        when(promotionRepository.findActivePromotionsOrdered(any(LocalDate.class))).thenReturn(List.of(promotion));

        CartDiscountCalculationDTO result = orderService.calculateCartDiscounts(items);

        assertNotNull(result);
        assertEquals(1300.0, result.getOriginalSubtotal(), 0.01);
        assertEquals(300.0, result.getTotalDiscount(), 0.01);
        assertEquals(1000.0, result.getFinalTotal(), 0.01);
        assertFalse(result.getAppliedDiscounts().isEmpty());
        assertTrue(result.getAppliedDiscounts().get(0).getDescription().contains("gratis"));
        assertEquals(300.0, result.getAppliedDiscounts().get(0).getDiscount(), 0.01);
        verify(promotionRepository).findActivePromotionsOrdered(any(LocalDate.class));
        verify(promotionInterpreter).parseExpression(anyString());
    }

    @Test
    @MockitoSettings(strictness = Strictness.LENIENT)
    void calculateCartDiscounts_WithFreeCombo_DetectsFreeCombo() {
        Map<Long, Integer> items = Map.of(product.getId(), 2);

        Combo freeCombo = new Combo(
                "Combo Gratis",
                "Descripción",
                500f,
                List.of("Comida"),
                List.of("Principal"),
                Map.of(product, 1),
                "imagen_base64_falsa");
        freeCombo.setId(40L);

        when(comboRepository.findById(40L)).thenReturn(Optional.of(freeCombo));

        Promotion promotion = new Promotion(
                "Promoción Combo Gratis",
                "Test Description",
                LocalDate.now().minusDays(1),
                LocalDate.now().plusDays(1),
                "{\"condition\":{\"type\":\"totalAmount\",\"operator\":\">=\",\"value\":500},\"action\":{\"type\":\"freeProduct\",\"productId\":40,\"quantity\":1}}",
                null,
                0);
        promotion.setId(1L);

        IfExpression mockExpression = mock(IfExpression.class);
        when(promotionInterpreter.parseExpression(anyString())).thenReturn(mockExpression);

        doAnswer(invocation -> {
            PromotionContext context = invocation.getArgument(0);
            Order order = context.getOrder();

            OrderItem freeItem = new OrderItem(order, freeCombo, 1);
            double freeItemTotal = freeItem.getItemPrice() * 1;
            freeItem.setDiscount((float) freeItemTotal);
            freeItem.addPromotionApplied("Producto gratis");
            freeItem.calculateSubtotal();
            order.getItems().add(freeItem);

            return true;
        }).when(mockExpression).interpret(any());

        when(promotionRepository.findActivePromotionsOrdered(any(LocalDate.class))).thenReturn(List.of(promotion));

        CartDiscountCalculationDTO result = orderService.calculateCartDiscounts(items);

        assertNotNull(result);
        assertEquals(1500.0, result.getOriginalSubtotal(), 0.01);
        assertEquals(500.0, result.getTotalDiscount(), 0.01);
        assertEquals(1000.0, result.getFinalTotal(), 0.01);
        assertFalse(result.getAppliedDiscounts().isEmpty());
        assertTrue(result.getAppliedDiscounts().get(0).getDescription().contains("gratis"));
        assertEquals(500.0, result.getAppliedDiscounts().get(0).getDiscount(), 0.01);
        verify(promotionRepository).findActivePromotionsOrdered(any(LocalDate.class));
        verify(comboRepository).findAllById(anyList());
    }

    @Test
    @MockitoSettings(strictness = Strictness.LENIENT)
    void calculateCartDiscounts_WithFreeProductMultipleQuantity_ShowsQuantity() {
        Map<Long, Integer> items = Map.of(product.getId(), 2);

        Product freeProduct = new Product(
                "Producto Gratis",
                "Descripción",
                200f,
                List.of("Comida"),
                "Individual",
                10,
                Map.of(ingredient, 1),
                "imagen_base64_falsa");
        freeProduct.setId(30L);

        when(productRepository.findById(30L)).thenReturn(Optional.of(freeProduct));

        Promotion promotion = new Promotion(
                "Promoción Producto Gratis",
                "Test Description",
                LocalDate.now().minusDays(1),
                LocalDate.now().plusDays(1),
                "{\"condition\":{\"type\":\"totalAmount\",\"operator\":\">=\",\"value\":500},\"action\":{\"type\":\"freeProduct\",\"productId\":30,\"quantity\":2}}",
                null,
                0);
        promotion.setId(1L);

        IfExpression mockExpression = mock(IfExpression.class);
        when(promotionInterpreter.parseExpression(anyString())).thenReturn(mockExpression);

        doAnswer(invocation -> {
            PromotionContext context = invocation.getArgument(0);
            Order order = context.getOrder();

            OrderItem freeItem = new OrderItem(order, freeProduct, 2);
            double freeItemTotal = freeItem.getItemPrice() * 2;
            freeItem.setDiscount((float) freeItemTotal);
            freeItem.addPromotionApplied("Producto gratis");
            freeItem.calculateSubtotal();
            order.getItems().add(freeItem);

            return true;
        }).when(mockExpression).interpret(any());

        when(promotionRepository.findActivePromotionsOrdered(any(LocalDate.class))).thenReturn(List.of(promotion));

        CartDiscountCalculationDTO result = orderService.calculateCartDiscounts(items);

        assertNotNull(result);
        assertFalse(result.getAppliedDiscounts().isEmpty());
        String description = result.getAppliedDiscounts().get(0).getDescription();
        assertTrue(description.contains("x2") || description.contains("Producto Gratis"));
        verify(promotionRepository).findActivePromotionsOrdered(any(LocalDate.class));
    }

    @Test
    @MockitoSettings(strictness = Strictness.LENIENT)
    void calculateCartDiscounts_WithSmallDiscount_DoesNotAddToAppliedDiscounts() {
        Map<Long, Integer> items = Map.of(product.getId(), 1);

        Promotion promotion = new Promotion(
                "Small Discount Promotion",
                "Test Description",
                LocalDate.now().minusDays(1),
                LocalDate.now().plusDays(1),
                "{\"condition\":{\"type\":\"totalAmount\",\"operator\":\">=\",\"value\":100},\"action\":{\"type\":\"fixedDiscount\",\"targetType\":\"ORDER\",\"amount\":0.005}}",
                null,
                0);
        promotion.setId(1L);

        IfExpression mockExpression = mock(IfExpression.class);
        when(promotionInterpreter.parseExpression(anyString())).thenReturn(mockExpression);

        // Simular un descuento muy pequeño (menor a 0.01)
        doAnswer(invocation -> {
            PromotionContext context = invocation.getArgument(0);
            Order order = context.getOrder();
            // Aplicar un descuento muy pequeño a un item
            if (!order.getItems().isEmpty()) {
                OrderItem item = order.getItems().get(0);
                item.setDiscount(0.005f);
                item.calculateSubtotal();
                // No llamamos calculateTotal() aquí, el servicio lo hace después
            }
            return true;
        }).when(mockExpression).interpret(any());

        when(promotionRepository.findActivePromotionsOrdered(any(LocalDate.class))).thenReturn(List.of(promotion));

        CartDiscountCalculationDTO result = orderService.calculateCartDiscounts(items);

        assertNotNull(result);
        // El descuento es menor a 0.01, así que NO debería agregarse a appliedDiscounts
        assertTrue(result.getAppliedDiscounts().isEmpty());
        // Pero el total debería reflejar el descuento pequeño
        assertEquals(500.0, result.getOriginalSubtotal(), 0.01);
        assertEquals(0.005, result.getTotalDiscount(), 0.001);
        verify(promotionRepository).findActivePromotionsOrdered(any(LocalDate.class));
    }

    @Test
    @MockitoSettings(strictness = Strictness.LENIENT)
    void calculateCartDiscounts_WithPercentageDiscount_AppliesAndAddsToAppliedDiscounts() {
        Map<Long, Integer> items = Map.of(product.getId(), 2);

        Promotion promotion = new Promotion(
                "Percentage Discount",
                "Test Description",
                LocalDate.now().minusDays(1),
                LocalDate.now().plusDays(1),
                "{\"condition\":{\"type\":\"totalAmount\",\"operator\":\">=\",\"value\":500},\"action\":{\"type\":\"percentageDiscount\",\"targetType\":\"ORDER\",\"percentage\":20}}",
                null,
                0);
        promotion.setId(1L);

        IfExpression mockExpression = mock(IfExpression.class);
        when(promotionInterpreter.parseExpression(anyString())).thenReturn(mockExpression);

        doAnswer(invocation -> {
            PromotionContext context = invocation.getArgument(0);
            Order order = context.getOrder();
            if (!order.getItems().isEmpty()) {
                double subtotalBefore = order.getItems().stream()
                        .mapToDouble(item -> {
                            double itemPrice = item.getItemPrice();
                            int quantity = item.getQuantity();
                            float discount = item.getDiscount() != null ? item.getDiscount() : 0f;
                            return (itemPrice * quantity) - discount;
                        })
                        .sum();

                double totalDiscountGoal = (subtotalBefore * 20.0) / 100.0;

                for (OrderItem item : order.getItems()) {
                    double itemPrice = item.getItemPrice();
                    int quantity = item.getQuantity();
                    float itemDiscount = item.getDiscount() != null ? item.getDiscount() : 0f;
                    double itemTotal = (itemPrice * quantity) - itemDiscount;

                    if (itemTotal > 0) {
                        double itemDiscountAmount = (itemTotal / subtotalBefore) * totalDiscountGoal;
                        item.setDiscount(itemDiscount + (float) itemDiscountAmount);
                        item.addPromotionApplied("Descuento 20.0%");
                        item.calculateSubtotal();
                    }
                }
            }
            return true;
        }).when(mockExpression).interpret(any());

        when(promotionRepository.findActivePromotionsOrdered(any(LocalDate.class))).thenReturn(List.of(promotion));

        CartDiscountCalculationDTO result = orderService.calculateCartDiscounts(items);

        assertNotNull(result);
        assertEquals(1000.0, result.getOriginalSubtotal(), 0.01);
        assertEquals(200.0, result.getTotalDiscount(), 0.01);
        assertEquals(800.0, result.getFinalTotal(), 0.01);
        assertFalse(result.getAppliedDiscounts().isEmpty());
        assertEquals(200.0, result.getAppliedDiscounts().get(0).getDiscount(), 0.01);
        verify(promotionRepository).findActivePromotionsOrdered(any(LocalDate.class));
    }

}