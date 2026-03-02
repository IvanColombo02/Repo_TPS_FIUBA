package ar.uba.fi.ingsoft1.product_example.items.products;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ProductRepository extends JpaRepository<Product, Long>, JpaSpecificationExecutor<Product> {
    // Stringly-typed generated query, see
    // https://docs.spring.io/spring-data/jpa/reference/jpa/query-methods.html

    @Query("select count(p) from Product p join p.ingredients i where key(i).id = :ingredientId and size(p.ingredients) = 1")
    long countProductsWhereOnlyIngredientIs(@Param("ingredientId") Long ingredientId);
}
