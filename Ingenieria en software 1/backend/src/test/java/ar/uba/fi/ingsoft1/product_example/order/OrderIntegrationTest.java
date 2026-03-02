package ar.uba.fi.ingsoft1.product_example.order;

import com.fasterxml.jackson.databind.ObjectMapper;
import ar.uba.fi.ingsoft1.product_example.user.User;
import ar.uba.fi.ingsoft1.product_example.user.UserRepository;
import ar.uba.fi.ingsoft1.product_example.user.PasswordResetTokenRepository;
import ar.uba.fi.ingsoft1.product_example.user.VerificationTokenRepository;
import ar.uba.fi.ingsoft1.product_example.user.refresh_token.RefreshTokenRepository;
import ar.uba.fi.ingsoft1.product_example.items.ingredients.Ingredient;
import ar.uba.fi.ingsoft1.product_example.items.ingredients.IngredientRepository;
import ar.uba.fi.ingsoft1.product_example.items.products.Product;
import ar.uba.fi.ingsoft1.product_example.items.products.ProductRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;
import org.junit.jupiter.api.AfterEach;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.transaction.PlatformTransactionManager;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@SpringBootTest
@AutoConfigureMockMvc
class UserOrdersTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private IngredientRepository ingredientRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private PlatformTransactionManager transactionManager;

    @Autowired
    private PasswordResetTokenRepository passwordResetTokenRepository;

    @Autowired
    private VerificationTokenRepository verificationTokenRepository;

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    private User testUser;
    private User testUser2;
    private User testEmployee;
    private Long productId;
    private Long product2Id;

    @BeforeEach
    void setup() {

        TransactionTemplate transactionTemplate = new TransactionTemplate(transactionManager);
        transactionTemplate.executeWithoutResult(status -> {
            createTestData();
        });
    }

    private void createTestData() {
        String uniqueId = UUID.randomUUID().toString().substring(0, 8);

        testUser = new User(
                "Test",
                "User",
                28,
                "Masculino",
                "Calle Test 123",
                null,
                "usuario_test_" + uniqueId,
                passwordEncoder.encode("password"),
                "ROLE_USER");
        testUser.setEmail("testuserorder_" + uniqueId + "@fi.uba.ar");
        testUser.setEmailVerified(true);
        testUser = userRepository.save(testUser);

        testUser2 = new User(
                "Test2",
                "User2",
                28,
                "Masculino2",
                "Calle Test 1232",
                null,
                "usuario_test2_" + uniqueId,
                passwordEncoder.encode("password2"),
                "ROLE_USER");
        testUser2.setEmail("testuser2_" + uniqueId + "@fi.uba.ar");
        testUser2.setEmailVerified(true);
        testUser2 = userRepository.save(testUser2);

        testEmployee = new User(
                "Test",
                "Employee",
                28,
                "Masculino",
                "Calle Test 123",
                null,
                "empleado_test_" + uniqueId,
                passwordEncoder.encode("passworddos"),
                "ROLE_EMPLOYEE");
        testEmployee.setEmail("testemployee_" + uniqueId + "@fi.uba.ar");
        testEmployee.setEmailVerified(true);
        testEmployee = userRepository.save(testEmployee);

     
        Ingredient ingredient = new Ingredient("Carne", 10, "img");
        ingredient = ingredientRepository.save(ingredient);

        Product product = new Product(
                "Hamburguesa Clásica",
                "Pan, carne, lechuga",
                500f,
                List.of("Comida"),
                "Individual",
                10,
                Map.of(ingredient, 1),
                "imagen_base64");
        product = productRepository.save(product);
        productId = product.getId();

        Ingredient ingredient2 = new Ingredient("Lechuga", 10, "img_fake");
        ingredient2 = ingredientRepository.save(ingredient2);

        Product product2 = new Product(
                "Ensalada",
                "Descripcion normalita",
                500f,
                List.of("Verdura"),
                "Saludable",
                10,
                Map.of(ingredient2, 1),
                "imagen_base64");
        product2 = productRepository.save(product2);
        product2Id = product2.getId();

    }

    @AfterEach
    @Transactional
    void cleanup() {
        orderRepository.deleteAll();

  
        passwordResetTokenRepository.deleteAll();
        verificationTokenRepository.deleteAll();
        refreshTokenRepository.deleteAll();

        productRepository.deleteAllInBatch();
        ingredientRepository.deleteAllInBatch();
        userRepository.deleteAll();
    }

    @Test
    void userCanCreateOrderAndViewItsStatus() throws Exception {
        mockMvc.perform(post("/orders")
                .with(authentication(
                        new UsernamePasswordAuthenticationToken(testUser, null, testUser.getAuthorities())))
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                            {
                            "items": { "%d": 2 },
                            "paymentMethod": "CASH"
                            }
                        """.formatted(productId)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.userId").value(testUser.getId()))
                .andExpect(jsonPath("$.paymentMethod").value("CASH"))
                .andExpect(jsonPath("$.items[0].componentId").value(productId))
                .andExpect(jsonPath("$.status").value("PENDING"));

        mockMvc.perform(get("/orders")
                .with(authentication(
                        new UsernamePasswordAuthenticationToken(testUser, null, testUser.getAuthorities())))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].status").value("PENDING"))
                .andExpect(jsonPath("$[0].paymentMethod").value("CASH"))
                .andExpect(jsonPath("$[0].items[0].componentId").value(productId))
                .andExpect(jsonPath("$[0].userId").value(testUser.getId()));
    }

    @Test
    void createOrderReducesStock() throws Exception {

        int initialStock = productRepository.findById(productId).orElseThrow().getStock();

        mockMvc.perform(post("/orders")
                .with(authentication(
                        new UsernamePasswordAuthenticationToken(testUser, null, testUser.getAuthorities())))
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                            {
                            "items": { "%d": 2 },
                            "paymentMethod": "CASH"
                            }
                        """.formatted(productId)))
                .andExpect(status().isCreated());

        Product productAfterCreateOrder = productRepository.findById(productId).orElseThrow();
        assertEquals(initialStock - 2, productAfterCreateOrder.getStock(),
                "El stock debería reducirse tras crear la orden");
    }

    @Test
    void userSeesNoOrdersWhenHasNone() throws Exception {
        mockMvc.perform(get("/orders")
                .with(authentication(
                        new UsernamePasswordAuthenticationToken(testUser, null, testUser.getAuthorities())))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void createOrderFailsWhenProductHasNoStock() throws Exception {
        mockMvc.perform(post("/orders")
                .with(authentication(
                        new UsernamePasswordAuthenticationToken(testUser, null, testUser.getAuthorities())))
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                            {
                            "items": { "%d": 1000 },
                            "paymentMethod": "CASH"
                            }
                        """.formatted(productId)))
                .andExpect(status().isBadRequest());

        mockMvc.perform(get("/orders")
                .with(authentication(
                        new UsernamePasswordAuthenticationToken(testUser, null, testUser.getAuthorities())))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));

    }

    @Test
    void createOrderFailsWhenProductDoesNotExist() throws Exception {
        mockMvc.perform(post("/orders")
                .with(authentication(
                        new UsernamePasswordAuthenticationToken(testUser, null, testUser.getAuthorities())))
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                            {
                            "items": { "%d": 2 },
                            "paymentMethod": "CASH"
                            }
                        """.formatted(productId + 1000))) // this id cannot be in the bdd
                .andExpect(status().isBadRequest());

        mockMvc.perform(get("/orders")
                .with(authentication(
                        new UsernamePasswordAuthenticationToken(testUser, null, testUser.getAuthorities())))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));

    }

    @Test
    void userCannotInteractWithOrdersIfUserDoesNotExist() throws Exception {

        mockMvc.perform(post("/orders")
                .with(authentication(new UsernamePasswordAuthenticationToken("NonExistentUser", null,
                        List.of(new SimpleGrantedAuthority("ROLE_USER")))))
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                            {
                            "items": { "%d": 2 },
                            "paymentMethod": "CASH"
                            }
                        """.formatted(productId)))
                .andExpect(status().isBadRequest());

        mockMvc.perform(get("/orders")
                .with(authentication(new UsernamePasswordAuthenticationToken("NonExistentUser", null,
                        List.of(new SimpleGrantedAuthority("ROLE_USER")))))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    void userCanCancellOrder() throws Exception {
        MvcResult result = mockMvc.perform(post("/orders")
                .with(authentication(
                        new UsernamePasswordAuthenticationToken(testUser, null, testUser.getAuthorities())))
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                            {
                            "items": { "%d": 2 },
                            "paymentMethod": "CASH"
                            }
                        """.formatted(productId)))
                .andExpect(status().isCreated())
                .andReturn();

        OrderDTO createdOrder = objectMapper.readValue(result.getResponse().getContentAsString(), OrderDTO.class);
        Long orderId = createdOrder.getId();

        mockMvc.perform(delete("/orders/" + orderId)
                .with(authentication(
                        new UsernamePasswordAuthenticationToken(testUser, null, testUser.getAuthorities()))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(orderId))
                .andExpect(jsonPath("$.status").value("CANCELLED"));

        mockMvc.perform(get("/orders")
                .with(authentication(
                        new UsernamePasswordAuthenticationToken(testUser, null, testUser.getAuthorities())))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(orderId))
                .andExpect(jsonPath("$[0].status").value("CANCELLED"));
    }

    @Test
    void stockIsRestoredAfterOrderIsCancelled() throws Exception {

        int initialStock = productRepository.findById(productId).orElseThrow().getStock();

        MvcResult result = mockMvc.perform(post("/orders")
                .with(authentication(
                        new UsernamePasswordAuthenticationToken(testUser, null, testUser.getAuthorities())))
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                            {
                            "items": { "%d": 7 },
                            "paymentMethod": "CASH"
                            }
                        """.formatted(productId)))
                .andExpect(status().isCreated())
                .andReturn();

        OrderDTO createdOrder = objectMapper.readValue(result.getResponse().getContentAsString(), OrderDTO.class);
        Long orderId = createdOrder.getId();

        Product productAfterCreateOrder = productRepository.findById(productId).orElseThrow();
        assertEquals(initialStock - 7, productAfterCreateOrder.getStock(),
                "El stock debería reducirse tras crear la orden");

        mockMvc.perform(post("/orders")
                .with(authentication(
                        new UsernamePasswordAuthenticationToken(testUser, null, testUser.getAuthorities())))
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                            {
                            "items": { "%d": 7 },
                            "paymentMethod": "CASH"
                            }
                        """.formatted(productId)))
                .andExpect(status().isBadRequest());

        mockMvc.perform(delete("/orders/" + orderId)
                .with(authentication(
                        new UsernamePasswordAuthenticationToken(testUser, null, testUser.getAuthorities()))))
                .andExpect(status().isOk());

        Product productAfterCancel = productRepository.findById(productId).orElseThrow();
        assertEquals(initialStock, productAfterCancel.getStock(),
                "El stock debería volver a su valor original tras cancelar la orden");

        mockMvc.perform(post("/orders")
                .with(authentication(
                        new UsernamePasswordAuthenticationToken(testUser, null, testUser.getAuthorities())))
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                            {
                            "items": { "%d": 7 },
                            "paymentMethod": "CASH"
                            }
                        """.formatted(productId)))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/orders")
                .with(authentication(
                        new UsernamePasswordAuthenticationToken(testUser, null, testUser.getAuthorities())))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].status").value("PENDING"))
                .andExpect(jsonPath("$[1].status").value("CANCELLED"));
    }

    @Test
    void userCanSeeAllHisOrders() throws Exception {
        mockMvc.perform(post("/orders")
                .with(authentication(
                        new UsernamePasswordAuthenticationToken(testUser, null, testUser.getAuthorities())))
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                            {
                            "items": { "%d": 2 },
                            "paymentMethod": "CASH"
                            }
                        """.formatted(productId)))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/orders")
                .with(authentication(
                        new UsernamePasswordAuthenticationToken(testUser, null, testUser.getAuthorities())))
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                            {
                            "items": { "%d": 1 },
                            "paymentMethod": "CASH"
                            }
                        """.formatted(product2Id)))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/orders")
                .with(authentication(
                        new UsernamePasswordAuthenticationToken(testUser, null, testUser.getAuthorities())))
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                            {
                            "items": { "%d": 1 },
                            "paymentMethod": "CASH"
                            }
                        """.formatted(productId)))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/orders")
                .with(authentication(
                        new UsernamePasswordAuthenticationToken(testUser, null, testUser.getAuthorities())))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(3))
                .andExpect(jsonPath("$[*].items[0].componentId",
                        containsInAnyOrder(productId.intValue(), product2Id.intValue(), productId.intValue())));

    }

    @Test
    void userCanCreateOrderAndEmployeeCanViewItsStatus() throws Exception {
        mockMvc.perform(post("/orders")
                .with(authentication(
                        new UsernamePasswordAuthenticationToken(testUser, null, testUser.getAuthorities())))
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                            {
                            "items": { "%d": 2 },
                            "paymentMethod": "CASH"
                            }
                        """.formatted(productId)))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/orders/status/PENDING")
                .with(authentication(
                        new UsernamePasswordAuthenticationToken(testEmployee, null, testEmployee.getAuthorities())))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    void employeeSeesOrdersFromAllUsers() throws Exception {
        mockMvc.perform(post("/orders")
                .with(authentication(
                        new UsernamePasswordAuthenticationToken(testUser, null, testUser.getAuthorities())))
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                            {
                            "items": { "%d": 2 },
                            "paymentMethod": "CASH"
                            }
                        """.formatted(productId)))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/orders")
                .with(authentication(
                        new UsernamePasswordAuthenticationToken(testUser2, null, testUser2.getAuthorities())))
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                            {
                            "items": { "%d": 2 },
                            "paymentMethod": "CASH"
                            }
                        """.formatted(productId)))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/orders/status/PENDING")
                .with(authentication(
                        new UsernamePasswordAuthenticationToken(testEmployee, null, testEmployee.getAuthorities())))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    void userCannotSeeOtherUsersOrders() throws Exception {
        mockMvc.perform(post("/orders")
                .with(authentication(
                        new UsernamePasswordAuthenticationToken(testUser, null, testUser.getAuthorities())))
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                            {
                            "items": { "%d": 2 },
                            "paymentMethod": "CASH"
                            }
                        """.formatted(productId)))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/orders")
                .with(authentication(
                        new UsernamePasswordAuthenticationToken(testUser2, null, testUser2.getAuthorities())))
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                            {
                            "items": { "%d": 2 },
                            "paymentMethod": "CASH"
                            }
                        """.formatted(productId)))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/orders")
                .with(authentication(
                        new UsernamePasswordAuthenticationToken(testUser, null, testUser.getAuthorities())))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    void employeeSeesNoOrdersWhenNoneExist() throws Exception {
        mockMvc.perform(get("/orders/status/PENDING")
                .with(authentication(
                        new UsernamePasswordAuthenticationToken(testEmployee, null, testEmployee.getAuthorities())))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void employeeCanModifyOrderStatus() throws Exception {
        MvcResult result = mockMvc.perform(post("/orders")
                .with(authentication(
                        new UsernamePasswordAuthenticationToken(testUser, null, testUser.getAuthorities())))
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                            {
                            "items": { "%d": 2 },
                            "paymentMethod": "CASH"
                            }
                        """.formatted(productId)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("PENDING"))
                .andReturn();

        OrderDTO createdOrder = objectMapper.readValue(result.getResponse().getContentAsString(), OrderDTO.class);
        Long orderId = createdOrder.getId();

        mockMvc.perform(patch("/orders/" + orderId + "/status")
                .with(authentication(
                        new UsernamePasswordAuthenticationToken(testEmployee, null, testEmployee.getAuthorities())))
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                            { "status": "IN_PREPARATION" }
                        """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(orderId))
                .andExpect(jsonPath("$.status").value("IN_PREPARATION"));

        mockMvc.perform(get("/orders/status/PENDING")
                .with(authentication(
                        new UsernamePasswordAuthenticationToken(testEmployee, null, testEmployee.getAuthorities())))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));

        mockMvc.perform(get("/orders/status/IN_PREPARATION")
                .with(authentication(
                        new UsernamePasswordAuthenticationToken(testEmployee, null, testEmployee.getAuthorities())))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    void employeeCanCancellPendingOrderAndStockIsRestored() throws Exception {

        int initialStock = productRepository.findById(productId).orElseThrow().getStock();

        MvcResult result = mockMvc.perform(post("/orders")
                .with(authentication(
                        new UsernamePasswordAuthenticationToken(testUser, null, testUser.getAuthorities())))
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                            {
                            "items": { "%d": 2 },
                            "paymentMethod": "CASH"
                            }
                        """.formatted(productId)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("PENDING"))
                .andReturn();

        OrderDTO createdOrder = objectMapper.readValue(result.getResponse().getContentAsString(), OrderDTO.class);
        Long orderId = createdOrder.getId();

        Product productAfterCreateOrder = productRepository.findById(productId).orElseThrow();
        assertEquals(initialStock - 2, productAfterCreateOrder.getStock(),
                "El stock debería reducirse tras crear la orden");

        mockMvc.perform(patch("/orders/" + orderId + "/status")
                .with(authentication(
                        new UsernamePasswordAuthenticationToken(testEmployee, null, testEmployee.getAuthorities())))
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                            { "status": "CANCELLED" }
                        """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(orderId))
                .andExpect(jsonPath("$.status").value("CANCELLED"));

        Product productAfterCancellOrder = productRepository.findById(productId).orElseThrow();
        assertEquals(initialStock, productAfterCancellOrder.getStock(),
                "El stock debería reponerse al cancelar la orden");

        mockMvc.perform(get("/orders/status/PENDING")
                .with(authentication(
                        new UsernamePasswordAuthenticationToken(testEmployee, null, testEmployee.getAuthorities())))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));

        mockMvc.perform(get("/orders/status/CANCELLED")
                .with(authentication(
                        new UsernamePasswordAuthenticationToken(testEmployee, null, testEmployee.getAuthorities())))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    void employeeCanCancelNonPendingOrderAndStockIsNotRestored() throws Exception {

        int initialStock = productRepository.findById(productId).orElseThrow().getStock();

        MvcResult result = mockMvc.perform(post("/orders")
                .with(authentication(
                        new UsernamePasswordAuthenticationToken(testUser, null, testUser.getAuthorities())))
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                            {
                            "items": { "%d": 2 },
                            "paymentMethod": "CASH"
                            }
                        """.formatted(productId)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("PENDING"))
                .andReturn();

        OrderDTO createdOrder = objectMapper.readValue(result.getResponse().getContentAsString(), OrderDTO.class);
        Long orderId = createdOrder.getId();

        Product productAfterCreateOrder = productRepository.findById(productId).orElseThrow();
        assertEquals(initialStock - 2, productAfterCreateOrder.getStock(),
                "El stock debería reducirse tras crear la orden");

        mockMvc.perform(patch("/orders/" + orderId + "/status")
                .with(authentication(
                        new UsernamePasswordAuthenticationToken(testEmployee, null, testEmployee.getAuthorities())))
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                            { "status": "IN_PREPARATION" }
                        """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(orderId))
                .andExpect(jsonPath("$.status").value("IN_PREPARATION"));

        mockMvc.perform(patch("/orders/" + orderId + "/status")
                .with(authentication(
                        new UsernamePasswordAuthenticationToken(testEmployee, null, testEmployee.getAuthorities())))
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                            { "status": "CANCELLED" }
                        """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(orderId))
                .andExpect(jsonPath("$.status").value("CANCELLED"));

        Product productAfterCancellOrder = productRepository.findById(productId).orElseThrow();
        assertEquals(initialStock - 2, productAfterCancellOrder.getStock(),
                "El stock no debería reponerse al cancelar la orden");

        mockMvc.perform(get("/orders/status/PENDING")
                .with(authentication(
                        new UsernamePasswordAuthenticationToken(testEmployee, null, testEmployee.getAuthorities())))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));

        mockMvc.perform(get("/orders/status/IN_PREPARATION")
                .with(authentication(
                        new UsernamePasswordAuthenticationToken(testEmployee, null, testEmployee.getAuthorities())))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));

        mockMvc.perform(get("/orders/status/CANCELLED")
                .with(authentication(
                        new UsernamePasswordAuthenticationToken(testEmployee, null, testEmployee.getAuthorities())))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    void employeeCanCancelCancelledOrderAndStockIsNotRestored() throws Exception {

        int initialStock = productRepository.findById(productId).orElseThrow().getStock();

        MvcResult result = mockMvc.perform(post("/orders")
                .with(authentication(
                        new UsernamePasswordAuthenticationToken(testUser, null, testUser.getAuthorities())))
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                            {
                            "items": { "%d": 2 },
                            "paymentMethod": "CASH"
                            }
                        """.formatted(productId)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("PENDING"))
                .andReturn();

        OrderDTO createdOrder = objectMapper.readValue(result.getResponse().getContentAsString(), OrderDTO.class);
        Long orderId = createdOrder.getId();

        Product productAfterCreateOrder = productRepository.findById(productId).orElseThrow();
        assertEquals(initialStock - 2, productAfterCreateOrder.getStock(),
                "El stock debería reducirse tras crear la orden");

        mockMvc.perform(patch("/orders/" + orderId + "/status")
                .with(authentication(
                        new UsernamePasswordAuthenticationToken(testEmployee, null, testEmployee.getAuthorities())))
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                            { "status": "CANCELLED" }
                        """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(orderId))
                .andExpect(jsonPath("$.status").value("CANCELLED"));

        Product productAfterCancellOrder = productRepository.findById(productId).orElseThrow();
        assertEquals(initialStock, productAfterCancellOrder.getStock(),
                "El stock debería reponerse al cancelar la orden");

        mockMvc.perform(patch("/orders/" + orderId + "/status")
                .with(authentication(
                        new UsernamePasswordAuthenticationToken(testEmployee, null, testEmployee.getAuthorities())))
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                            { "status": "CANCELLED" }
                        """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(orderId))
                .andExpect(jsonPath("$.status").value("CANCELLED"));

        Product productAfterDoubleCancellOrder = productRepository.findById(productId).orElseThrow();
        assertEquals(initialStock, productAfterDoubleCancellOrder.getStock(),
                "El stock no debería reponerse al cancelar la orden dos veces");

        mockMvc.perform(get("/orders/status/PENDING")
                .with(authentication(
                        new UsernamePasswordAuthenticationToken(testEmployee, null, testEmployee.getAuthorities())))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));

        mockMvc.perform(get("/orders/status/CANCELLED")
                .with(authentication(
                        new UsernamePasswordAuthenticationToken(testEmployee, null, testEmployee.getAuthorities())))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    void employeeCannotUpdateNonExistentOrder() throws Exception {
        Long nonExistentOrderId = 9999L;

        mockMvc.perform(patch("/orders/" + nonExistentOrderId + "/status")
                .with(authentication(
                        new UsernamePasswordAuthenticationToken(testEmployee, null, testEmployee.getAuthorities())))
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        { "status": "CANCELLED" }
                        """))
                .andExpect(status().isBadRequest())
                .andExpect(result -> assertTrue(result.getResolvedException() instanceof RuntimeException))
                .andExpect(result -> assertEquals("Order not found with id: " + nonExistentOrderId,
                        result.getResolvedException().getMessage()));
    }

    @Test
    void employeeCanUpdateOrderAndUserSeesStatusChange() throws Exception {
        MvcResult result = mockMvc.perform(post("/orders")
                .with(authentication(
                        new UsernamePasswordAuthenticationToken(testUser, null, testUser.getAuthorities())))
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                            {
                            "items": { "%d": 2 },
                            "paymentMethod": "CASH"
                            }
                        """.formatted(productId)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("PENDING"))
                .andReturn();

        OrderDTO createdOrder = objectMapper.readValue(result.getResponse().getContentAsString(), OrderDTO.class);
        Long orderId = createdOrder.getId();

        mockMvc.perform(patch("/orders/" + orderId + "/status")
                .with(authentication(
                        new UsernamePasswordAuthenticationToken(testEmployee, null, testEmployee.getAuthorities())))
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                            { "status": "IN_PREPARATION" }
                        """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(orderId))
                .andExpect(jsonPath("$.status").value("IN_PREPARATION"));

        mockMvc.perform(get("/orders")
                .with(authentication(
                        new UsernamePasswordAuthenticationToken(testUser, null, testUser.getAuthorities())))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].status").value("IN_PREPARATION"));
    }
}