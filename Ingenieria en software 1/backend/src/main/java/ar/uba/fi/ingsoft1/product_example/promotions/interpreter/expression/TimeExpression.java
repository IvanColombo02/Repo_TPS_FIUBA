package ar.uba.fi.ingsoft1.product_example.promotions.interpreter.expression;

import ar.uba.fi.ingsoft1.product_example.promotions.interpreter.PromotionContext;

import java.time.LocalTime;

public class TimeExpression implements Expression{
    private final LocalTime hour;
    private final String operator;

    public TimeExpression(LocalTime hour, String operator){
        this.hour = hour;
        this.operator = operator;
    }

    @Override
    public boolean interpret(PromotionContext context) {
        LocalTime orderTime = context.getTime();
        return switch(operator){
            case ">" -> orderTime.isAfter(hour);  
            case "<" -> orderTime.isBefore(hour); 
            case "=" -> orderTime.equals(hour);
            default -> throw new IllegalArgumentException("Unknown operator: " + operator);
        };
    }
}
