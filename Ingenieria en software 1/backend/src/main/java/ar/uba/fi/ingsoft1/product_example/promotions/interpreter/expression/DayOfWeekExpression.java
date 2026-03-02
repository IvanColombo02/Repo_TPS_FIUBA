package ar.uba.fi.ingsoft1.product_example.promotions.interpreter.expression;

import ar.uba.fi.ingsoft1.product_example.promotions.interpreter.PromotionContext;
import lombok.RequiredArgsConstructor;

import java.time.DayOfWeek;

@RequiredArgsConstructor
public class DayOfWeekExpression implements Expression {
    private final String day;

    @Override
    public boolean interpret(PromotionContext context) {
        DayOfWeek currentDay = context.getCurrentDate().getDayOfWeek();
        DayOfWeek targetDay = parseDayOfWeek(day);
        return currentDay == targetDay;
    }

    private DayOfWeek parseDayOfWeek(String day) {
        try {
            return DayOfWeek.valueOf(day.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid day of week: " + day);
        }
    }
}
