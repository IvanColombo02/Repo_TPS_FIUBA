package ar.uba.fi.ingsoft1.product_example.promotions.interpreter;

import ar.uba.fi.ingsoft1.product_example.items.combos.ComboRepository;
import ar.uba.fi.ingsoft1.product_example.items.products.ProductRepository;
import ar.uba.fi.ingsoft1.product_example.promotions.interpreter.action.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ActionFactory {
    private final ProductRepository productRepository;
    private final ComboRepository comboRepository;

    public Action createFixedDiscountAction(String targetType, Long targetItemId,
                                            String targetCategory, String targetProductType,
                                            double amount) {
        return new FixedDiscountAction(targetType, targetItemId, targetCategory, targetProductType, amount);
    }

    public Action createPercentageDiscountAction(String targetType, Long targetItemId,
            String targetCategory, String targetProductType,
            double percentage) {
        return new PercentageDiscountAction(targetType, targetItemId, targetCategory, targetProductType, percentage);
    }

    public Action createFreeProductAction(long productId, int quantity) {
        return new FreeProductAction(productId, quantity, productRepository, comboRepository);
    }

    public Action createQuantityDiscountAction(int buyQuantity, int payQuantity) {
        return new QuantityDiscountAction(buyQuantity, payQuantity);
    }
}
