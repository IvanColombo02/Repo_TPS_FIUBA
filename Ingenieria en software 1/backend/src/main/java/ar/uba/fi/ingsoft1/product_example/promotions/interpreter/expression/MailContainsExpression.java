package ar.uba.fi.ingsoft1.product_example.promotions.interpreter.expression;

import ar.uba.fi.ingsoft1.product_example.promotions.interpreter.PromotionContext;

import java.time.LocalDate;
import java.time.LocalTime;

public class MailContainsExpression implements Expression {
    private String mail;
    public MailContainsExpression(String mail) {
        this.mail = mail;
    }
    @Override
    public boolean interpret(PromotionContext context) {
        if (context.getOrder().getUser() == null) {
          
            return false;
        }
        String userMail = context.getOrder().getUser().getEmail();
        if (userMail == null) {
            return false;
        }
        return userMail.contains(mail);
    }
}
