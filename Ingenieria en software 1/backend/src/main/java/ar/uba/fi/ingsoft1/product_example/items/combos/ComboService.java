package ar.uba.fi.ingsoft1.product_example.items.combos;

import ar.uba.fi.ingsoft1.product_example.items.ComponentSearchDTO;
import ar.uba.fi.ingsoft1.product_example.items.ComponentSpecification;
import ar.uba.fi.ingsoft1.product_example.items.products.ProductRepository;
import ar.uba.fi.ingsoft1.product_example.order.OrderRepository;
import ar.uba.fi.ingsoft1.product_example.order.OrderStatus;
import ar.uba.fi.ingsoft1.product_example.promotions.PromotionRepository;

import java.time.LocalDate;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
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
public class ComboService {
    private final ComboRepository comboRepository;
    private final ProductRepository productRepository;
    private final EntityManager entityManager;
    private final OrderRepository orderRepository;
    private final PromotionRepository promotionRepository;

    public Page<ComboSimpleDTO> getCombos(ComponentSearchDTO filter, Pageable pageable) {
        Specification<Combo> spec = ComponentSpecification.searchFilter(filter);
        return comboRepository.findAll(spec, pageable)
                .map(ComboSimpleDTO::new);
    }

    public Optional<ComboDTO> getComboById(long id) {
        return comboRepository.findById(id).map(ComboDTO::new);
    }

    public ComboDTO createCombo(ComboCreateDTO data) {
        Specification<Combo> spec = ComponentSpecification.searchByName(data.name());
        if (!comboRepository.findAll(spec).isEmpty()) return null;
        var combo = data.asCombo(productRepository::getReferenceById);
        return new ComboDTO(comboRepository.save(combo));
    }

    public Optional<ComboDTO> updateCombo(Long id, ComboUpdateDTO update) {
        return comboRepository.findById(id)
                .map(combo -> {
                    Combo updated = update.applyTo(combo, productRepository::getReferenceById);
                    // Force flush to persist delete operations before insert
                    entityManager.flush();
                    // Force stock recalculation to reflect current product stock values
                    updated.recalculateStock();
                    return updated;
                })
                .map(comboRepository::save)
                .map(ComboDTO::new);
    }

    private static final List<OrderStatus> ACTIVE_STATUSES = List.of(
            OrderStatus.PENDING, OrderStatus.IN_PREPARATION, OrderStatus.READY);
    private static final List<OrderStatus> FINALIZED_STATUSES = List.of(
            OrderStatus.CANCELLED, OrderStatus.DELIVERED);

    public boolean deleteComboById(Long id) {
        if (!comboRepository.existsById(id))
            return false;

        if (orderRepository.existsActiveOrdersContainingComponent(id, ACTIVE_STATUSES)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "No se puede eliminar el combo: está presente en al menos una orden activa.");
        }

        if (promotionRepository.existsActivePromotionsReferencingComponent(String.valueOf(id), LocalDate.now())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "No se puede eliminar el combo: está siendo usado en al menos una promoción activa.");
        }

        orderRepository.removeComponentFromFinalizedOrders(id, FINALIZED_STATUSES);
        comboRepository.deleteById(id);
        return true;
    }

    public Optional<ComboDTO> updateStock(Long id, ComboStockDTO update) {
        return comboRepository.findById(id)
                .flatMap(update::applyTo)
                .map(comboRepository::save)
                .map(ComboDTO::new);
    }
}
