package ar.uba.fi.ingsoft1.product_example.promotions;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface PromotionRepository extends JpaRepository<Promotion, Long> {
    Optional<Promotion> findById(Long id);

    Optional<Promotion> findByName(String name);

    List<Promotion> findAllByOrderByPriorityAsc();

    @Query("SELECT p FROM Promotion p WHERE p.fromDate <= :date AND p.toDate >= :date ORDER BY p.priority ASC, p.id ASC")
    List<Promotion> findActivePromotionsOrdered(@Param("date") LocalDate date);

    @Query("SELECT COALESCE(MAX(p.priority), 0) FROM Promotion p")
    Integer findMaxPriority();

    @Modifying
    @Query("UPDATE Promotion p SET p.priority = p.priority + 1 WHERE p.priority >= :priority")
    void shiftPrioritiesFrom(@Param("priority") int priority);


    @Query("SELECT COUNT(p) > 0 FROM Promotion p WHERE " +
            "(p.fromDate <= :date AND p.toDate >= :date) AND " +
            "(p.expression LIKE CONCAT('%\"productId\":', :componentIdStr, '%') " +
            "OR p.expression LIKE CONCAT('%\"targetItemId\":', :componentIdStr, '%'))")
    boolean existsActivePromotionsReferencingComponent(@Param("componentIdStr") String componentIdStr,
            @Param("date") LocalDate date);
}
