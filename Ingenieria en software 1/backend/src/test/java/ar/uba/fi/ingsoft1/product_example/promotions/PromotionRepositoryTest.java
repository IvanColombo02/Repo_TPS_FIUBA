package ar.uba.fi.ingsoft1.product_example.promotions;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class PromotionRepositoryTest {

    @Autowired
    private PromotionRepository promotionRepository;

    private Promotion promotion1;
    private Promotion promotion2;
    private Promotion promotion3;

    @BeforeEach
    void setUp() {
        LocalDate today = LocalDate.now();
        LocalDate fromDate1 = today.minusDays(10);
        LocalDate toDate1 = today.plusDays(20);
        LocalDate fromDate2 = today.minusDays(5);
        LocalDate toDate2 = today.plusDays(15);
        LocalDate fromDate3 = today.plusDays(5);
        LocalDate toDate3 = today.plusDays(30);

        promotion1 = new Promotion(
                "Promotion 1",
                "First promotion for testing",
                fromDate1,
                toDate1,
                "{\"condition\":{\"type\":\"productInCart\",\"productId\":1},\"action\":{\"type\":\"fixedDiscount\",\"targetType\":\"ORDER\",\"amount\":100}}",
                "",
                1);

        promotion2 = new Promotion(
                "Promotion 2",
                "Second promotion for testing",
                fromDate2,
                toDate2,
                "{\"condition\":{\"type\":\"totalAmount\",\"operator\":\">=\",\"value\":2000},\"action\":{\"type\":\"percentageDiscount\",\"targetType\":\"ORDER\",\"percentage\":15}}",
                "",
                2);

        promotion3 = new Promotion(
                "Promotion 3",
                "Third promotion for testing",
                fromDate3,
                toDate3,
                "{\"condition\":{\"type\":\"totalAmount\",\"operator\":\">=\",\"value\":3000},\"action\":{\"type\":\"percentageDiscount\",\"targetType\":\"ORDER\",\"percentage\":20}}",
                "",
                3);

        promotionRepository.save(promotion1);
        promotionRepository.save(promotion2);
        promotionRepository.save(promotion3);
    }

    @Test
    void addPromotion() {
        Promotion newPromotion = new Promotion(
                "New Promotion",
                "A new promotion",
                LocalDate.now().minusDays(1),
                LocalDate.now().plusDays(30),
                "{\"condition\":{\"type\":\"totalAmount\",\"operator\":\">=\",\"value\":1000},\"action\":{\"type\":\"percentageDiscount\",\"targetType\":\"ORDER\",\"percentage\":10}}",
                "",
                4);
        Promotion saved = promotionRepository.save(newPromotion);
        assertEquals(newPromotion.getName(), saved.getName());
        assertNotNull(saved.getId());
    }

    @Test
    void removePromotion() {
        promotionRepository.deleteById(promotion1.getId());
        assertFalse(promotionRepository.findById(promotion1.getId()).isPresent());
    }

    @Test
    void findByIdReturnsPromotion() {
        Optional<Promotion> result = promotionRepository.findById(promotion1.getId());
        assertTrue(result.isPresent());
        assertEquals(promotion1.getName(), result.get().getName());
    }

    @Test
    void findByIdReturnsEmptyWhenNotFound() {
        Optional<Promotion> result = promotionRepository.findById(99999L);
        assertFalse(result.isPresent());
    }

    @Test
    void findByNameReturnsPromotion() {
        Optional<Promotion> result = promotionRepository.findByName("Promotion 1");
        assertTrue(result.isPresent());
        assertEquals(promotion1.getId(), result.get().getId());
    }

    @Test
    void findByNameReturnsEmptyWhenNotFound() {
        Optional<Promotion> result = promotionRepository.findByName("Non-existent Promotion");
        assertFalse(result.isPresent());
    }

    @Test
    void findAllByOrderByPriorityAscReturnsOrderedList() {
        List<Promotion> result = promotionRepository.findAllByOrderByPriorityAsc();
        assertEquals(3, result.size());
        assertEquals(1, result.get(0).getPriority());
        assertEquals(2, result.get(1).getPriority());
        assertEquals(3, result.get(2).getPriority());
    }

    @Test
    void findActivePromotionsOrderedReturnsOnlyActive() {
        LocalDate testDate = LocalDate.now();
        List<Promotion> result = promotionRepository.findActivePromotionsOrdered(testDate);
        assertTrue(result.size() >= 2);
        assertTrue(result.stream().anyMatch(p -> p.getName().equals("Promotion 1")));
        assertTrue(result.stream().anyMatch(p -> p.getName().equals("Promotion 2")));
        assertFalse(result.stream().anyMatch(p -> p.getName().equals("Promotion 3")));
    }

    @Test
    void findActivePromotionsOrderedReturnsEmptyForFutureDate() {
        LocalDate futureDate = LocalDate.now().plusDays(100);
        List<Promotion> result = promotionRepository.findActivePromotionsOrdered(futureDate);
        assertTrue(result.isEmpty() || result.stream().noneMatch(p -> p.getName().equals("Promotion 1")));
    }

    @Test
    void findActivePromotionsOrderedReturnsEmptyForPastDate() {
        LocalDate pastDate = LocalDate.now().minusDays(100);
        List<Promotion> result = promotionRepository.findActivePromotionsOrdered(pastDate);
        assertTrue(result.isEmpty());
    }

    @Test
    void findMaxPriorityReturnsHighestPriority() {
        Integer maxPriority = promotionRepository.findMaxPriority();
        assertNotNull(maxPriority);
        assertEquals(3, maxPriority);
    }

    @Test
    void findMaxPriorityReturnsZeroWhenNoPromotions() {
        promotionRepository.deleteAll();
        Integer maxPriority = promotionRepository.findMaxPriority();
        assertNotNull(maxPriority);
        assertEquals(0, maxPriority);
    }

    @Test
    void existsActivePromotionsReferencingComponentReturnsTrueWhenReferencesExist() {
        LocalDate testDate = LocalDate.now();
        String componentIdStr = "1";
        boolean result = promotionRepository.existsActivePromotionsReferencingComponent(componentIdStr, testDate);
        assertTrue(result);
    }

    @Test
    void existsActivePromotionsReferencingComponentReturnsFalseForNonExistent() {
        LocalDate testDate = LocalDate.now();
        String componentIdStr = "99999";
        boolean result = promotionRepository.existsActivePromotionsReferencingComponent(componentIdStr, testDate);
        assertFalse(result);
    }
}
