package ar.uba.fi.ingsoft1.product_example.order;

import static ar.uba.fi.ingsoft1.product_example.order.OrderConstants.*;
import ar.uba.fi.ingsoft1.product_example.items.Component;
import ar.uba.fi.ingsoft1.product_example.items.combos.Combo;
import ar.uba.fi.ingsoft1.product_example.items.combos.ComboRepository;
import ar.uba.fi.ingsoft1.product_example.items.products.Product;
import ar.uba.fi.ingsoft1.product_example.items.products.ProductRepository;
import ar.uba.fi.ingsoft1.product_example.promotions.Promotion;
import ar.uba.fi.ingsoft1.product_example.promotions.PromotionRepository;
import ar.uba.fi.ingsoft1.product_example.promotions.interpreter.expression.IfExpression;
import ar.uba.fi.ingsoft1.product_example.promotions.interpreter.PromotionContext;
import ar.uba.fi.ingsoft1.product_example.promotions.interpreter.PromotionInterpreter;
import ar.uba.fi.ingsoft1.product_example.user.User;
import ar.uba.fi.ingsoft1.product_example.user.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Service layer for order management.
 * Handles business logic for creating, updating, and canceling orders.
 */
@Service
public class OrderService {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private ComboRepository comboRepository;

    @Autowired
    private UserService userService;

    @Autowired
    private PromotionRepository promotionRepository;

    @Autowired
    private PromotionInterpreter promotionInterpreter;

    /**
     * Creates a new order with stock validation.
     */
    @Transactional
    public OrderDTO createOrder(OrderCreateDTO dto, Long userId) {
        // 1. Validate user exists
        User user = userService.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));

        // 2. Fetch and validate all components exist (Products and Combos)
        Map<Long, Integer> itemsMap = dto.getItems();
        List<Long> requestedIds = new ArrayList<>(itemsMap.keySet());

        List<Product> products = productRepository.findAllById(requestedIds);
        List<Combo> combos = comboRepository.findAllById(requestedIds);

        List<Component> components = new ArrayList<>();
        components.addAll(products);
        components.addAll(combos);

        if (components.size() != itemsMap.size()) {
            List<Long> foundIds = components.stream()
                    .map(Component::getId)
                    .collect(Collectors.toList());
            List<Long> missingIds = new ArrayList<>(itemsMap.keySet());
            missingIds.removeAll(foundIds);
            throw new RuntimeException("Components not found with ids: " + missingIds);
        }

        Order order = new Order();
        order.setUser(user);
        order.setPaymentMethod(dto.getPaymentMethod());
        order.setStatus(OrderStatus.PENDING);

        for (Component component : components) {
            int quantity = itemsMap.get(component.getId());
            order.addItem(component, quantity);
        }

        List<String> insufficientStockErrors = new ArrayList<>();
        for (Component component : components) {
            int quantity = itemsMap.get(component.getId());

            if (!component.reduceStock(quantity)) {
                insufficientStockErrors.add(
                        String.format("%s (requested: %d, available: %d)",
                                component.getName(),
                                quantity,
                                component.getStock()));
            }
        }

        // If any item failed, throw exception (transaction will rollback)
        if (!insufficientStockErrors.isEmpty()) {
            throw new InsufficientStockException(
                    "Insufficient stock for items: " + String.join(", ", insufficientStockErrors));
        }

        // Save Products
        List<Product> productsToSave = components.stream()
                .filter(c -> c instanceof Product)
                .map(c -> (Product) c)
                .collect(Collectors.toList());
        productRepository.saveAll(productsToSave);

        // Save Combos
        List<Combo> combosToSave = components.stream()
                .filter(c -> c instanceof Combo)
                .map(c -> (Combo) c)
                .collect(Collectors.toList());
        comboRepository.saveAll(combosToSave);
        evaluateAndApplyPromotions(order);

        Order savedOrder = orderRepository.save(order);
        initializeOrderUser(savedOrder);

        return OrderDTO.fromEntity(savedOrder);
    }

    private void evaluateAndApplyPromotions(Order order) {
        PromotionContext context = new PromotionContext(order);
        LocalDate today = LocalDate.now();

        List<Promotion> activePromotions = promotionRepository.findActivePromotionsOrdered(today);
        for (Promotion promotion : activePromotions) {
            try {
                IfExpression expression = promotionInterpreter.parseExpression(promotion.getExpression());
                expression.interpret(context);
            } catch (Exception e) {
                System.err.println("Error evaluating promotion " + promotion.getId() + ": " + e.getMessage());
            }
        }

        order.calculateTotal();
    }

    /**
     * Cancels an order if in PENDING status.                       Used by users
     */
    @Transactional
    public OrderDTO cancelOrder(Long orderId, Long userId) {
        Order order = orderRepository.findByIdAndUser_Id(orderId, userId)
                .orElseThrow(() -> new RuntimeException(
                        "Order not found or you don't have permission to cancel it"));

        if (!order.canBeCancelled()) {
            throw new IllegalStateException(
                    "Cannot cancel order in status: " + order.getStatus());
        }

        for (OrderItem item : order.getItems()) {
            Component component = item.getComponent();
            if (component != null) {
                component.addStock(item.getQuantity());

                if (component instanceof Product) {
                    productRepository.save((Product) component);
                } else if (component instanceof Combo) {
                    comboRepository.save((Combo) component);
                }
            }
        }

        order.cancel();
        Order savedOrder = orderRepository.save(order);
        initializeOrderUser(savedOrder);

        return OrderDTO.fromEntity(savedOrder);
    }

    /**
     * Updates the status of an order.
     * If status changes to CANCELLED, returns stock to components.         Used by employees
     */
    @Transactional
    public OrderDTO updateOrderStatus(Long orderId, OrderStatusUpdateDTO dto) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found with id: " + orderId));

        if (dto == null || dto.getStatus() == null) {
            throw new IllegalArgumentException("Order status is required");
        }

        // If changing to CANCELLED, return stock
        if (dto.getStatus() == OrderStatus.CANCELLED && order.getStatus() != OrderStatus.CANCELLED && order.getStatus() == OrderStatus.PENDING) {
            for (OrderItem item : order.getItems()) {
                Component component = item.getComponent();
                component.addStock(item.getQuantity());

                // Save based on type
                if (component instanceof Product) {
                    productRepository.save((Product) component);
                } else if (component instanceof Combo) {
                    comboRepository.save((Combo) component);
                }
            }
        }

        order.setStatus(dto.getStatus());
        Order savedOrder = orderRepository.save(order);
        initializeOrderUser(savedOrder);

        return OrderDTO.fromEntity(savedOrder);
    }

    /**
     * Gets order details by ID.
     */
    @Transactional(readOnly = true)
    public OrderDTO getOrderById(Long orderId, Long userId) {
        Order order = orderRepository.findByIdAndUser_Id(orderId, userId)
                .orElseThrow(() -> new RuntimeException(
                        "Order not found or you don't have permission to view it"));

        initializeOrderUser(order);

        return OrderDTO.fromEntity(order);
    }

    /**
     * Gets all orders for a user.
     */
    @Transactional(readOnly = true)
    public List<OrderDTO> getMyOrders(Long userId) {
        List<Order> orders = orderRepository.findByUser_IdOrderByCreatedAtDesc(userId);

        orders.forEach(order -> order.getUserId());

        return orders.stream()
                .map(OrderDTO::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * Gets all orders with a specific status.
     */
    @Transactional(readOnly = true)
    public List<OrderDTO> getOrdersByStatus(OrderStatus status) {
        List<Order> orders = orderRepository.findByStatus(status);

        orders.forEach(order -> order.getUserId());

        return orders.stream()
                .map(OrderDTO::fromEntity)
                .collect(Collectors.toList());
    }

    private void initializeOrderUser(Order order) {
        if (order != null) {
            order.getUserId();
            order.getUserEmail();
        }
    }

    @Transactional(readOnly = true)
    public List<OrderDTO> getActiveOrders() {
        List<OrderStatus> activeStatuses = List.of(
                OrderStatus.PENDING,
                OrderStatus.IN_PREPARATION,
                OrderStatus.READY,
                OrderStatus.DELIVERED,
                OrderStatus.CANCELLED);
        List<Order> orders = orderRepository.findByStatusInOrderByCreatedAtAsc(activeStatuses);

        orders.forEach(order -> order.getUserId());

        return orders.stream()
                .map(OrderDTO::fromEntity)
                .collect(Collectors.toList());
    }

   
    @Transactional(readOnly = true)
    public CartDiscountCalculationDTO calculateCartDiscounts(Map<Long, Integer> items) {
        return calculateCartDiscounts(items, null);
    }

    @Transactional(readOnly = true)
    public CartDiscountCalculationDTO calculateCartDiscounts(Map<Long, Integer> items, Long userId) {
        List<Long> requestedIds = new ArrayList<>(items.keySet());
        List<Product> products = productRepository.findAllById(requestedIds);
        List<Combo> combos = comboRepository.findAllById(requestedIds);

        List<Component> components = new ArrayList<>();
        components.addAll(products);
        components.addAll(combos);

        if (components.size() != items.size()) {
            List<Long> foundIds = components.stream()
                    .map(Component::getId)
                    .collect(Collectors.toList());
            List<Long> missingIds = new ArrayList<>(items.keySet());
            missingIds.removeAll(foundIds);
            throw new RuntimeException("Components not found with ids: " + missingIds);
        }

        Order tempOrder = new Order();
        tempOrder.setStatus(OrderStatus.PENDING);
        tempOrder.setPaymentMethod(PaymentMethod.CASH);

        if (userId != null) {
            userService.findById(userId).ifPresent(tempOrder::setUser);
        }

        for (Component component : components) {
            int quantity = items.get(component.getId());
            tempOrder.addItem(component, quantity);
        }

        PromotionContext context = new PromotionContext(tempOrder);
        LocalDate today = LocalDate.now();
        List<Promotion> activePromotions = promotionRepository.findActivePromotionsOrdered(today);

        List<CartDiscountCalculationDTO.AppliedDiscountDTO> appliedDiscounts = new ArrayList<>();
        List<String> promotionDescriptions = new ArrayList<>();

        for (Promotion promotion : activePromotions) {
            try {
                int itemCountBefore = tempOrder.getItems().size();
                double subtotalBefore = tempOrder.getItems().stream()
                        .mapToDouble(item -> {
                            double itemTotal = item.getItemPrice() * item.getQuantity();
                            float itemDiscount = item.getDiscount() != null ? item.getDiscount() : DEFAULT_DISCOUNT;
                            return itemTotal - itemDiscount;
                        })
                        .sum();

                IfExpression expression = promotionInterpreter.parseExpression(promotion.getExpression());
                expression.interpret(context);

                tempOrder.getItems().forEach(OrderItem::calculateSubtotal);

                int itemCountAfter = tempOrder.getItems().size();
                double subtotalAfter = tempOrder.getItems().stream()
                        .mapToDouble(item -> {
                            double itemTotal = item.getItemPrice() * item.getQuantity();
                            float itemDiscount = item.getDiscount() != null ? item.getDiscount() : DEFAULT_DISCOUNT;
                            return itemTotal - itemDiscount;
                        })
                        .sum();

                double promotionDiscount = subtotalBefore - subtotalAfter;
                StringBuilder descriptionBuilder = new StringBuilder(promotion.getName());

                if (itemCountAfter > itemCountBefore) {
                    List<OrderItem> newItems = tempOrder.getItems().subList(itemCountBefore, itemCountAfter);
                    List<String> freeProducts = new ArrayList<>();
                    for (OrderItem newItem : newItems) {
                        double itemValue = newItem.getItemPrice() * newItem.getQuantity();
                        float itemDiscount = newItem.getDiscount() != null ? newItem.getDiscount() : 0f;
                        if (itemDiscount >= itemValue * FREE_PRODUCT_THRESHOLD) {
                            promotionDiscount += itemValue;
                            freeProducts.add(newItem.getItemName()
                                    + (newItem.getQuantity() > 1 ? " x" + newItem.getQuantity() : ""));
                        }
                    }
                    if (!freeProducts.isEmpty()) {
                        descriptionBuilder.append(": ").append(String.join(", ", freeProducts)).append(" gratis");
                    }
                }

                if (promotionDiscount > MIN_DISCOUNT_THRESHOLD) {
                    String description = descriptionBuilder.toString();
                    appliedDiscounts.add(new CartDiscountCalculationDTO.AppliedDiscountDTO(
                            promotion.getId(),
                            promotion.getName(),
                            promotionDiscount,
                            description));

                    promotionDescriptions.add(description);
                }
            } catch (Exception e) {
                System.err.println("Error evaluating promotion " + promotion.getId() + ": " + e.getMessage());
            }
        }

        tempOrder.calculateTotal();
        double finalTotal = tempOrder.getTotalPrice();

        double originalSubtotal = tempOrder.getItems().stream()
                .mapToDouble(item -> item.getItemPrice() * item.getQuantity())
                .sum();

        double totalDiscount = originalSubtotal - finalTotal;

        return new CartDiscountCalculationDTO(
                originalSubtotal,
                totalDiscount,
                finalTotal,
                appliedDiscounts,
                promotionDescriptions);
    }
}
