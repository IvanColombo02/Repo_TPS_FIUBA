package ar.uba.fi.ingsoft1.product_example.promotions;

import static ar.uba.fi.ingsoft1.product_example.promotions.PromotionConstants.*;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class PromotionService {

    private final PromotionRepository promotionRepository;

    public Optional<PromotionDTO> getPromotionById(long id) {
        return promotionRepository.findById(id).map(PromotionDTO::new);
    }

    public Optional<PromotionDTO> getPromotionByName(String name) {
        return promotionRepository.findByName(name).map(PromotionDTO::new);
    }

    public Page<PromotionDTO> getPromotions(Pageable pageable) {
        Pageable effectivePageable = pageable;
        if (pageable == null || pageable.isUnpaged()) {
            effectivePageable = PageRequest.of(DEFAULT_PAGE_NUMBER, UNLIMITED_PAGE_SIZE,
                    Sort.by(Sort.Direction.ASC, "priority").and(Sort.by("id")));
        } else if (pageable.getSort().isUnsorted()) {
            effectivePageable = PageRequest.of(
                    pageable.getPageNumber(),
                    pageable.getPageSize(),
                    Sort.by(Sort.Direction.ASC, "priority").and(Sort.by("id")));
        }
        return promotionRepository.findAll(effectivePageable).map(PromotionDTO::new);
    }

    public List<PromotionDTO> getActivePromotions(LocalDate date) {
        return promotionRepository.findActivePromotionsOrdered(date).stream()
                .map(PromotionDTO::new)
                .collect(Collectors.toList());
    }

    public PromotionDTO createPromotion(@NonNull PromotionCreateDTO data) {
        var promotion = data.asPromotion();
        int resolvedPriority = resolvePriorityForCreation(promotion.getPriority());
        promotion.setPriority(resolvedPriority);
        return new PromotionDTO(promotionRepository.save(promotion));
    }

    public boolean deletePromotionById(long id) {
        if (!promotionRepository.existsById(id))
            return false;
        promotionRepository.deleteById(id);
        return true;
    }

    public Optional<PromotionDTO> updatePromotion(Long id, PromotionUpdateDTO update) {
        return promotionRepository.findById(id)
                .map(update::applyTo)
                .map(promotionRepository::save)
                .map(PromotionDTO::new);
    }

    public List<PromotionDTO> updatePriorities(@NonNull PromotionPriorityUpdateDTO priorityUpdateDTO) {
        List<Long> orderedIds = priorityUpdateDTO.orderedPromotionIds();
        if (orderedIds == null || orderedIds.isEmpty()) {
            throw new IllegalArgumentException(
                    "Debe proporcionar al menos un ID de promoción para actualizar las prioridades");
        }

        Set<Long> uniqueIds = new LinkedHashSet<>(orderedIds);
        if (uniqueIds.size() != orderedIds.size()) {
            throw new IllegalArgumentException("Los IDs de promoción no pueden repetirse");
        }

        Map<Long, Promotion> promotionsById = promotionRepository.findAllByOrderByPriorityAsc()
                .stream()
                .collect(Collectors.toMap(Promotion::getId, Function.identity(), (a, b) -> a, LinkedHashMap::new));

        List<Long> missing = orderedIds.stream()
                .filter(id -> !promotionsById.containsKey(id))
                .collect(Collectors.toList());
        if (!missing.isEmpty()) {
            throw new IllegalArgumentException("Promociones inexistentes: " + missing);
        }

        List<Promotion> reordered = new ArrayList<>(promotionsById.values());
        AtomicInteger counter = new AtomicInteger(MIN_PRIORITY);

        List<Promotion> updated = new ArrayList<>();

        for (Long id : orderedIds) {
            Promotion promotion = promotionsById.get(id);
            promotion.setPriority(counter.getAndIncrement());
            updated.add(promotion);
        }

        for (Promotion promotion : reordered) {
            if (!uniqueIds.contains(promotion.getId())) {
                promotion.setPriority(counter.getAndIncrement());
                updated.add(promotion);
            }
        }

        promotionRepository.saveAll(updated);
        return promotionRepository.findAllByOrderByPriorityAsc().stream()
                .map(PromotionDTO::new)
                .collect(Collectors.toList());
    }

    private int resolvePriorityForCreation(Integer requestedPriority) {
        int maxPriority = Optional.ofNullable(promotionRepository.findMaxPriority()).orElse(DEFAULT_PRIORITY);
        int nextPriority = maxPriority + MIN_PRIORITY;
        if (requestedPriority == null) {
            return nextPriority;
        }

        int priority = Math.max(MIN_PRIORITY, Math.min(requestedPriority, nextPriority));
        promotionRepository.shiftPrioritiesFrom(priority);
        return priority;
    }
}
