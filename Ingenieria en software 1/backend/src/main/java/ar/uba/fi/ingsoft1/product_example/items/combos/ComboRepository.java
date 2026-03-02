package ar.uba.fi.ingsoft1.product_example.items.combos;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ComboRepository extends JpaRepository<Combo, Long>, JpaSpecificationExecutor<Combo> {
    // Stringly-typed generated query, see
    // https://docs.spring.io/spring-data/jpa/reference/jpa/query-methods.html

    @Query("select count(c) from Combo c join c.products pr where key(pr).id = :productId and type(key(pr)) = ar.uba.fi.ingsoft1.product_example.items.products.Product and size(c.products) = 1")
    long countCombosWhereOnlyProductIs(@Param("productId") Long productId);
}
