package ar.uba.fi.ingsoft1.product_example.promotions.interpreter.action;

import ar.uba.fi.ingsoft1.product_example.items.Component;
import ar.uba.fi.ingsoft1.product_example.items.combos.Combo;
import ar.uba.fi.ingsoft1.product_example.items.combos.ComboRepository;
import ar.uba.fi.ingsoft1.product_example.items.products.Product;
import ar.uba.fi.ingsoft1.product_example.items.products.ProductRepository;
import ar.uba.fi.ingsoft1.product_example.order.Order;
import ar.uba.fi.ingsoft1.product_example.order.OrderItem;
import ar.uba.fi.ingsoft1.product_example.promotions.interpreter.PromotionContext;
import lombok.RequiredArgsConstructor;

import java.util.Optional;

@RequiredArgsConstructor
public class FreeProductAction implements Action {
    private final long productId;
    private final int quantity;
    private final ProductRepository productRepository;
    private final ComboRepository comboRepository;

    @Override
    public void apply(PromotionContext context, Order order) {

        Optional<Product> product = productRepository.findById(productId);
        Optional<Combo> combo = comboRepository.findById(productId);

        Component freeComponent = null;
        if (product.isPresent()) {
            freeComponent = product.get();
        } else if (combo.isPresent()) {
            freeComponent = combo.get();
        }

        if (freeComponent != null) {
            OrderItem freeItem = new OrderItem(order, freeComponent, quantity);
            double freeItemTotal = freeItem.getItemPrice() * quantity;
            freeItem.setDiscount((float) freeItemTotal); // descuento 100%
            freeItem.addPromotionApplied("Producto gratis");
            freeItem.calculateSubtotal();
            order.getItems().add(freeItem);
        }
    }
}
