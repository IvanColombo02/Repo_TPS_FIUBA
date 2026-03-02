package ar.uba.fi.ingsoft1.product_example.items.ingredients;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
public interface IngredientRepository extends JpaRepository<Ingredient, Long>, JpaSpecificationExecutor<Ingredient> {
    // Stringly-typed generated query, see
    // https://docs.spring.io/spring-data/jpa/reference/jpa/query-methods.html
}
