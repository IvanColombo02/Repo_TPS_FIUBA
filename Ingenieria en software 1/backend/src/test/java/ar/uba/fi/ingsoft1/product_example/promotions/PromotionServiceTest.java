package ar.uba.fi.ingsoft1.product_example.promotions;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class PromotionServiceTest {
    private PromotionRepository promotionRepository;
    private PromotionService promotionService;
    private Promotion promotion1;
    private Promotion promotion2;

    @BeforeEach
    void setup() {
        promotionRepository = mock();
        promotionService = new PromotionService(promotionRepository);

        LocalDate today = LocalDate.now();
        promotion1 = new Promotion(
                1L,
                "Test Promotion 1",
                "Description 1",
                today.minusDays(1),
                today.plusDays(30),
                "{\"condition\":{\"type\":\"totalAmount\",\"operator\":\">=\",\"value\":1000},\"action\":{\"type\":\"percentageDiscount\",\"targetType\":\"ORDER\",\"percentage\":10}}",
                "",
                1);

        promotion2 = new Promotion(
                2L,
                "Test Promotion 2",
                "Description 2",
                today.minusDays(1),
                today.plusDays(30),
                "{\"condition\":{\"type\":\"totalAmount\",\"operator\":\">=\",\"value\":2000},\"action\":{\"type\":\"percentageDiscount\",\"targetType\":\"ORDER\",\"percentage\":15}}",
                "",
                2);

        when(promotionRepository.findById(1L)).thenReturn(Optional.of(promotion1));
        when(promotionRepository.findById(999L)).thenReturn(Optional.empty());
        when(promotionRepository.findByName("Test Promotion 1")).thenReturn(Optional.of(promotion1));
        when(promotionRepository.findByName("Non-existent")).thenReturn(Optional.empty());
        when(promotionRepository.existsById(1L)).thenReturn(true);
        when(promotionRepository.existsById(999L)).thenReturn(false);
    }

    @Test
    void getPromotionByIdReturnsDTO() {
        var result = promotionService.getPromotionById(1L);
        assertTrue(result.isPresent());
        assertEquals(new PromotionDTO(promotion1), result.get());
    }

    @Test
    void getPromotionByIdReturnsEmptyIfAbsent() {
        var result = promotionService.getPromotionById(999L);
        assertTrue(result.isEmpty());
    }

    @Test
    void getPromotionByNameReturnsDTO() {
        var result = promotionService.getPromotionByName("Test Promotion 1");
        assertTrue(result.isPresent());
        assertEquals(new PromotionDTO(promotion1), result.get());
    }

    @Test
    void getPromotionByNameReturnsEmptyIfAbsent() {
        var result = promotionService.getPromotionByName("Non-existent");
        assertTrue(result.isEmpty());
    }

    @Test
    void createWritesToDatabase() {
        when(promotionRepository.findMaxPriority()).thenReturn(0);
        when(promotionRepository.save(any(Promotion.class))).thenAnswer(inv -> {
            Promotion p = inv.getArgument(0);
            p.setId(1L);
            return p;
        });

        var dto = new PromotionCreateDTO(
                "New Promotion",
                "A new promotion description",
                LocalDate.now().minusDays(1).toString(),
                LocalDate.now().plusDays(30).toString(),
                "{\"condition\":{\"type\":\"totalAmount\",\"operator\":\">=\",\"value\":1000},\"action\":{\"type\":\"percentageDiscount\",\"targetType\":\"ORDER\",\"percentage\":10}}",
                "",
                null);
        promotionService.createPromotion(dto);
        verify(promotionRepository).save(any(Promotion.class));
    }

    @Test
    void createReturnsCreatedPromotion() {
        when(promotionRepository.findMaxPriority()).thenReturn(0);
        when(promotionRepository.save(any(Promotion.class))).thenAnswer(inv -> {
            Promotion p = inv.getArgument(0);
            p.setId(1L);
            return p;
        });

        var dto = new PromotionCreateDTO(
                "New Promotion",
                "A new promotion description",
                LocalDate.now().minusDays(1).toString(),
                LocalDate.now().plusDays(30).toString(),
                "{\"condition\":{\"type\":\"totalAmount\",\"operator\":\">=\",\"value\":1000},\"action\":{\"type\":\"percentageDiscount\",\"targetType\":\"ORDER\",\"percentage\":10}}",
                "",
                null);
        var result = promotionService.createPromotion(dto);
        assertEquals("New Promotion", result.name());
    }

    @Test
    void createWithPriorityResolvesPriority() {
        when(promotionRepository.findMaxPriority()).thenReturn(5);
        when(promotionRepository.save(any(Promotion.class))).thenAnswer(inv -> {
            Promotion p = inv.getArgument(0);
            p.setId(1L);
            return p;
        });

        var dto = new PromotionCreateDTO(
                "Priority Promotion",
                "A promotion with priority",
                LocalDate.now().minusDays(1).toString(),
                LocalDate.now().plusDays(30).toString(),
                "{\"condition\":{\"type\":\"totalAmount\",\"operator\":\">=\",\"value\":1000},\"action\":{\"type\":\"percentageDiscount\",\"targetType\":\"ORDER\",\"percentage\":10}}",
                "",
                3);
        var result = promotionService.createPromotion(dto);
        verify(promotionRepository).shiftPrioritiesFrom(3);
        assertEquals("Priority Promotion", result.name());
    }

    @Test
    void deleteExistingPromotionReturnsTrue() {
        promotionService.deletePromotionById(1L);
        verify(promotionRepository).deleteById(1L);
    }

    @Test
    void deleteNonexistentPromotionReturnsFalse() {
        boolean result = promotionService.deletePromotionById(999L);
        assertFalse(result);
        verify(promotionRepository, never()).deleteById(anyLong());
    }

    @Test
    void updatePromotionReturnsUpdatedDTO() {
        when(promotionRepository.save(any(Promotion.class))).thenAnswer(inv -> inv.getArgument(0));

        var update = new PromotionUpdateDTO(
                Optional.of("Updated Name"),
                Optional.empty(),
                Optional.empty(),
                Optional.empty(),
                Optional.empty(),
                Optional.empty());
        var result = promotionService.updatePromotion(1L, update);
        assertTrue(result.isPresent());
        assertEquals("Updated Name", result.get().name());
    }

    @Test
    void updatePromotionReturnsEmptyIfNotFound() {
        var update = new PromotionUpdateDTO(
                Optional.of("Updated Name"),
                Optional.empty(),
                Optional.empty(),
                Optional.empty(),
                Optional.empty(),
                Optional.empty());
        var result = promotionService.updatePromotion(999L, update);
        assertTrue(result.isEmpty());
    }

    @Test
    void getPromotionsWithNullPageableReturnsAll() {
        Pageable pageable = PageRequest.of(0, Integer.MAX_VALUE,
                Sort.by(Sort.Direction.ASC, "priority").and(Sort.by("id")));
        when(promotionRepository.findAll(any(Pageable.class))).thenReturn(
                org.springframework.data.domain.Page.empty(pageable));

        var result = promotionService.getPromotions(null);
        assertNotNull(result);
        verify(promotionRepository).findAll(any(Pageable.class));
    }

    @Test
    void getPromotionsWithUnpagedReturnsAll() {
        Pageable pageable = PageRequest.of(0, Integer.MAX_VALUE,
                Sort.by(Sort.Direction.ASC, "priority").and(Sort.by("id")));
        when(promotionRepository.findAll(any(Pageable.class))).thenReturn(
                org.springframework.data.domain.Page.empty(pageable));

        var result = promotionService.getPromotions(Pageable.unpaged());
        assertNotNull(result);
        verify(promotionRepository).findAll(any(Pageable.class));
    }

    @Test
    void getPromotionsWithUnsortedAddsSort() {
        Pageable unsortedPageable = PageRequest.of(0, 10);
        Pageable sortedPageable = PageRequest.of(0, 10, Sort.by(Sort.Direction.ASC, "priority").and(Sort.by("id")));
        when(promotionRepository.findAll(any(Pageable.class))).thenReturn(
                org.springframework.data.domain.Page.empty(sortedPageable));

        var result = promotionService.getPromotions(unsortedPageable);
        assertNotNull(result);
        verify(promotionRepository).findAll(any(Pageable.class));
    }

    @Test
    void getActivePromotionsReturnsOrderedList() {
        LocalDate testDate = LocalDate.now();
        List<Promotion> activePromotions = List.of(promotion1, promotion2);
        when(promotionRepository.findActivePromotionsOrdered(testDate)).thenReturn(activePromotions);

        var result = promotionService.getActivePromotions(testDate);
        assertEquals(2, result.size());
        assertEquals(new PromotionDTO(promotion1), result.get(0));
        assertEquals(new PromotionDTO(promotion2), result.get(1));
    }

    @Test
    void updatePrioritiesWithEmptyListThrowsException() {
        var dto = new PromotionPriorityUpdateDTO(new ArrayList<>());
        assertThrows(IllegalArgumentException.class, () -> {
            promotionService.updatePriorities(dto);
        });
    }

    @Test
    void updatePrioritiesWithDuplicateIdsThrowsException() {
        var dto = new PromotionPriorityUpdateDTO(List.of(1L, 1L));
        assertThrows(IllegalArgumentException.class, () -> {
            promotionService.updatePriorities(dto);
        });
    }

    @Test
    void updatePrioritiesWithNonExistentIdsThrowsException() {
        when(promotionRepository.findAllByOrderByPriorityAsc()).thenReturn(new ArrayList<>());
        var dto = new PromotionPriorityUpdateDTO(List.of(999L));
        assertThrows(IllegalArgumentException.class, () -> {
            promotionService.updatePriorities(dto);
        });
    }

    @Test
    void updatePrioritiesReordersCorrectly() {
        List<Promotion> allPromotions = List.of(promotion1, promotion2);
        when(promotionRepository.findAllByOrderByPriorityAsc())
                .thenReturn(allPromotions)
                .thenReturn(List.of(promotion2, promotion1));
        when(promotionRepository.saveAll(anyList())).thenAnswer(inv -> {
            List<Promotion> saved = inv.getArgument(0);
            return saved;
        });

        var dto = new PromotionPriorityUpdateDTO(List.of(2L, 1L));
        var result = promotionService.updatePriorities(dto);

        assertEquals(2, result.size());
        assertEquals(2L, result.get(0).id());
        assertEquals(1L, result.get(1).id());
        verify(promotionRepository).saveAll(anyList());
        verify(promotionRepository, times(2)).findAllByOrderByPriorityAsc();
    }

    @Test
    void updatePrioritiesIncludesPromotionsNotInList() {
        Promotion promotion3 = new Promotion(
                3L,
                "Test Promotion 3",
                "Description 3",
                LocalDate.now().minusDays(1),
                LocalDate.now().plusDays(30),
                "{\"condition\":{\"type\":\"totalAmount\",\"operator\":\">=\",\"value\":3000},\"action\":{\"type\":\"percentageDiscount\",\"targetType\":\"ORDER\",\"percentage\":20}}",
                "",
                3);

        List<Promotion> allPromotions = List.of(promotion1, promotion2, promotion3);
        when(promotionRepository.findAllByOrderByPriorityAsc())
                .thenReturn(allPromotions)
                .thenReturn(List.of(promotion2, promotion1, promotion3));
        when(promotionRepository.saveAll(anyList())).thenAnswer(inv -> {
            List<Promotion> saved = inv.getArgument(0);
            return saved;
        });

        var dto = new PromotionPriorityUpdateDTO(List.of(2L, 1L));
        var result = promotionService.updatePriorities(dto);

        assertEquals(3, result.size());
        assertEquals(2L, result.get(0).id());
        assertEquals(1L, result.get(1).id());
        assertEquals(3L, result.get(2).id());
        verify(promotionRepository).saveAll(anyList());
        verify(promotionRepository, times(2)).findAllByOrderByPriorityAsc();
    }
}
