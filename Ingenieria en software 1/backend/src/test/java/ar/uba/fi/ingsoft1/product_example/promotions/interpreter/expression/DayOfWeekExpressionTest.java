package ar.uba.fi.ingsoft1.product_example.promotions.interpreter.expression;

import ar.uba.fi.ingsoft1.product_example.order.Order;
import ar.uba.fi.ingsoft1.product_example.order.OrderStatus;
import ar.uba.fi.ingsoft1.product_example.order.PaymentMethod;
import ar.uba.fi.ingsoft1.product_example.promotions.interpreter.PromotionContext;
import ar.uba.fi.ingsoft1.product_example.user.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

class DayOfWeekExpressionTest {

    private static final ZoneId ARGENTINA_ZONE = ZoneId.of("America/Argentina/Buenos_Aires");
    private Order order;
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

        context = new PromotionContext(order);
    }

    @Test
    void interpretReturnsTrueForCurrentDay() {
        DayOfWeek currentDay = LocalDate.now(ARGENTINA_ZONE).getDayOfWeek();
        String dayName = currentDay.name();
        DayOfWeekExpression expression = new DayOfWeekExpression(dayName);
        assertTrue(expression.interpret(context));
    }

    @Test
    void interpretReturnsFalseForDifferentDay() {
        DayOfWeek currentDay = LocalDate.now(ARGENTINA_ZONE).getDayOfWeek();
        DayOfWeek differentDay = currentDay.plus(1);
        String dayName = differentDay.name();
        DayOfWeekExpression expression = new DayOfWeekExpression(dayName);
        assertFalse(expression.interpret(context));
    }

    @Test
    void interpretWithLowerCaseDay() {
        DayOfWeek currentDay = LocalDate.now(ARGENTINA_ZONE).getDayOfWeek();
        String dayName = currentDay.name().toLowerCase();
        DayOfWeekExpression expression = new DayOfWeekExpression(dayName);
        assertTrue(expression.interpret(context));
    }

    @Test
    void interpretWithInvalidDayThrowsException() {
        DayOfWeekExpression expression = new DayOfWeekExpression("INVALID_DAY");
        assertThrows(IllegalArgumentException.class, () -> expression.interpret(context));
    }
}
