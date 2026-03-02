package ar.uba.fi.ingsoft1.product_example.items.products;

import ar.uba.fi.ingsoft1.product_example.items.combos.Combo;
import ar.uba.fi.ingsoft1.product_example.items.ingredients.IngredientRepository;
import ar.uba.fi.ingsoft1.product_example.items.ComponentSearchDTO;
import ar.uba.fi.ingsoft1.product_example.items.ComponentSpecification;
import ar.uba.fi.ingsoft1.product_example.items.combos.ComboRepository;
import ar.uba.fi.ingsoft1.product_example.order.OrderRepository;
import ar.uba.fi.ingsoft1.product_example.order.OrderStatus;
import ar.uba.fi.ingsoft1.product_example.promotions.PromotionRepository;

import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import org.antlr.v4.runtime.atn.SemanticContext;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;
    private final IngredientRepository ingredientRepository;
    private final ComboRepository comboRepository;
    private final OrderRepository orderRepository;
    private final PromotionRepository promotionRepository;

    @Transactional(readOnly = true)
    public Page<ProductSimpleDTO> getProducts(ComponentSearchDTO filter, Pageable pageable) {
        Specification<Product> spec = ComponentSpecification.searchFilter(filter);
        return productRepository.findAll(spec, pageable)
                .map(ProductSimpleDTO::new);
    }

    public Optional<ProductDTO> getProductById(long id) {
        return productRepository.findById(id).map(ProductDTO::new);
    }

    public ProductDTO createProduct(ProductCreateDTO data) {
        Specification<Product> spec = ComponentSpecification.searchByName(data.name());
        if (!productRepository.findAll(spec).isEmpty()) return null;
        var product = data.asProduct(ingredientRepository::getReferenceById);
        return new ProductDTO(productRepository.save(product));
    }

    public Optional<ProductDTO> updateProduct(Long id, ProductUpdateDTO update) {
        return productRepository.findById(id)
                .map(product -> update.applyTo(product, ingredientRepository::getReferenceById))
                .map(productRepository::save)
                .map(ProductDTO::new);
    }

    private static final List<OrderStatus> ACTIVE_STATUSES = List.of(
            OrderStatus.PENDING, OrderStatus.IN_PREPARATION, OrderStatus.READY);
    private static final List<OrderStatus> FINALIZED_STATUSES = List.of(
            OrderStatus.CANCELLED, OrderStatus.DELIVERED);

    public boolean deleteProductById(Long id) {
        if (!productRepository.existsById(id))
            return false;

        if (orderRepository.existsActiveOrdersContainingComponent(id, ACTIVE_STATUSES)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "No se puede eliminar el producto: está presente en al menos una orden activa.");
        }

        if (comboRepository.countCombosWhereOnlyProductIs(id) > 0) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "No se puede eliminar el producto: es el único componente de al menos un combo.");
        }

        if (promotionRepository.existsActivePromotionsReferencingComponent(String.valueOf(id), LocalDate.now())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "No se puede eliminar el producto: está siendo usado en al menos una promoción activa.");
        }

        orderRepository.removeComponentFromFinalizedOrders(id, FINALIZED_STATUSES);
        productRepository.deleteById(id);
        return true;
    }

    public Optional<ProductDTO> updateStock(Long id, ProductStockDTO update) {
        return productRepository.findById(id)
                .flatMap(update::applyTo)
                .map(productRepository::save)
                .map(ProductDTO::new);
    }
}
