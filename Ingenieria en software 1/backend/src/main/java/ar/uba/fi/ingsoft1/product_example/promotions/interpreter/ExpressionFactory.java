package ar.uba.fi.ingsoft1.product_example.promotions.interpreter;

import ar.uba.fi.ingsoft1.product_example.promotions.interpreter.expression.*;
import lombok.Getter;

import java.time.LocalTime;

public class ExpressionFactory {
    @Getter
    private static final ExpressionFactory expressionFactory = new ExpressionFactory();

    private ExpressionFactory() {
    }

    public Expression createAndExpression(Expression left, Expression right) {
        return new AndExpression(left, right);
    }

    public Expression createOrExpression(Expression left, Expression right) {
        return new OrExpression(left, right);
    }
    public Expression createTotalAmountExpression(String operator, double value){
        return new TotalAmountExpression(operator, value);
    }
    public Expression createDayOfWeek(String day){
        return new DayOfWeekExpression(day);
    }
    public Expression createProductInCar(long day){
        return new ProductInCartExpression(day);
    }
    public Expression createProductName(String productName){
        return new ProductNameExpression(productName);
    }
    public Expression createQuantity(long quantityProductId, int minQuantity){
        return new QuantityExpression(quantityProductId, minQuantity);
    }
    public Expression createProductCategoryExpression(String category) {
        return new ProductCategoryExpression(category);
    }
    public Expression createProductTypeExpression(String type) {
        return new ProductTypeExpression(type);
    }
    public Expression createTimeExpression(LocalTime hour, String operator){
        return new TimeExpression(hour, operator);
    }
    public Expression createMailContainsExpression(String mail){
        return new MailContainsExpression(mail);
    }
}
