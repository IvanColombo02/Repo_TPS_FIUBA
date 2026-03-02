package ar.uba.fi.ingsoft1.product_example.order;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

    /**
     * Finds orders by user ID, ordered by creation date desc.
     */
    List<Order> findByUser_IdOrderByCreatedAtDesc(Long userId);

    /**
     * Finds orders by status.
     */
    List<Order> findByStatus(OrderStatus status);

    /**
     * Finds orders by multiple statuses, ordered by creation date asc.
     */
    List<Order> findByStatusInOrderByCreatedAtAsc(List<OrderStatus> statuses);

    /**
     * Finds order by ID and user ID.
     */
    Optional<Order> findByIdAndUser_Id(Long id, Long userId);

    /**
     * Finds orders by user ID and status.
     */
    List<Order> findByUser_IdAndStatus(Long userId, OrderStatus status);

    /**
     * Counts orders by status.
     */
    Long countByStatus(OrderStatus status);

    /**
     * Finds orders containing a specific component.
     */
    @Query("SELECT DISTINCT o FROM Order o JOIN o.items i WHERE i.component.id = :componentId")
    List<Order> findOrdersContainingComponent(@Param("componentId") Long componentId);

    @Query("SELECT COUNT(i) > 0 FROM OrderItem i WHERE i.component.id = :componentId " +
            "AND i.order.status IN :activeStatuses")
    boolean existsActiveOrdersContainingComponent(@Param("componentId") Long componentId,
            @Param("activeStatuses") List<OrderStatus> activeStatuses);


    @Modifying
    @Transactional
    @Query("UPDATE OrderItem i SET i.component = NULL WHERE i.component.id = :componentId " +
            "AND i.order.status IN :finalizedStatuses")
    int removeComponentFromFinalizedOrders(@Param("componentId") Long componentId,
            @Param("finalizedStatuses") List<OrderStatus> finalizedStatuses);
}
