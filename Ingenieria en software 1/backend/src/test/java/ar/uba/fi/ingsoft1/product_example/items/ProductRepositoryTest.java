package ar.uba.fi.ingsoft1.product_example.items;

import ar.uba.fi.ingsoft1.product_example.items.combos.Combo;
import ar.uba.fi.ingsoft1.product_example.items.ingredients.Ingredient;
import ar.uba.fi.ingsoft1.product_example.items.ingredients.IngredientRepository;
import ar.uba.fi.ingsoft1.product_example.items.products.Product;
import ar.uba.fi.ingsoft1.product_example.items.products.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static ar.uba.fi.ingsoft1.product_example.items.ProductConstants.*;
import static ar.uba.fi.ingsoft1.product_example.items.ProductConstants.COMBO_IMAGE64;
import static ar.uba.fi.ingsoft1.product_example.items.ProductConstants.COMBO_TYPE;
import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class ProductRepositoryTest {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private IngredientRepository ingredientRepository;

    Ingredient ingredient1;
    Ingredient ingredient2;
    Product product1;
    Product product2;
    Product product3;

    @BeforeEach
    void setUp() {
        ingredient1 = ingredientRepository.save(new Ingredient("Ingredient Name1", 10, "Img1"));
        ingredient2 = ingredientRepository.save(new Ingredient("Ingredient Name2", 5, "Img2"));
        product1 = new Product(NAME, DESCRIPTION, 200, List.of("None 1"), TYPE,
                30, Map.of(ingredient1, 1), "Img3");
        product2 = new Product("Product 2", DESCRIPTION, 600, List.of("None 2"), "Type 2",
                30, Map.of(ingredient2, 1), "Img4");
        product3 = new Product("Product 3", DESCRIPTION, 1200, List.of("None 3"), TYPE,
                30, Map.of(ingredient2, 1, ingredient1, 1), "Img5");
        productRepository.save(product1);
        productRepository.save(product2);
        productRepository.save(product3);
    }

    @Test
    void addProduct(){
        product1 = new Product("Product 1", "Description 1", 200, List.of("None 1"), "Type 1",
                30, Map.of(ingredient1, 1), "Img3");
        assertEquals(product1, productRepository.save(product1));
    }
    @Test
    void removeProduct(){
        product1 = new Product("Product 1", "Description 1", 200, List.of("None 1"), "Type 1",
                30, Map.of(ingredient1, 1), "Img3");
        productRepository.save(product1);
        productRepository.deleteById(product1.getId());
        assertFalse(productRepository.findById(product1.getId()).isPresent());
    }
    @Test
    void specSearchByName() {
        Specification<Product> spec = ComponentSpecification.searchByName("t 2");
        var result = productRepository.findAll(spec);
        assertEquals(List.of(product2), result);
    }

    @Test
    void specSearchByNameNotFound() {
        Specification<Product> spec = ComponentSpecification.searchByName("jaja");
        var result = productRepository.findAll(spec);
        assertEquals(List.of(), result);
    }

    @Test
    void specSearchByCategory() {
        Specification<Product> spec = ComponentSpecification.searchByCategories(List.of("None 1"));
        var result = productRepository.findAll(spec);
        assertEquals(List.of(product1), result);
    }

    @Test
    void specSearchByCategoryNotFound() {
        Specification<Product> spec = ComponentSpecification.searchByCategories(List.of("jaja"));
        var result = productRepository.findAll(spec);
        assertEquals(List.of(), result);
    }
    @Test
    void specSearchByType() {
        Specification<Product> spec = ComponentSpecification.searchByTypes(List.of(TYPE));
        var result = productRepository.findAll(spec);
        assertEquals(List.of(product1,product3), result);
    }
    @Test
    void specSearchByTypeNotFound() {
        Specification<Product> spec = ComponentSpecification.searchByTypes(List.of("null"));
        var result = productRepository.findAll(spec);
        assertEquals(List.of(), result);
    }

    @Test
    void specSearchByPriceMin() {
        int priceMin = 500;
        Specification<Product> spec = ComponentSpecification.searchByPrice(priceMin, null);
        var result = productRepository.findAll(spec);
        assertEquals(List.of(product2, product3), result);
    }

    @Test
    void specSearchByPriceMax() {
        int priceMax = 1000;
        Specification<Product> spec = ComponentSpecification.searchByPrice(null, priceMax);
        var result = productRepository.findAll(spec);
        assertEquals(List.of(product1, product2), result);
    }

    @Test
    void specSearchByPriceMinMax() {
        int priceMin = 500;
        int priceMax = 1000;
        Specification<Product> spec = ComponentSpecification.searchByPrice(priceMin, priceMax);
        var result = productRepository.findAll(spec);
        assertEquals(List.of(product2), result);
    }

    @Test
    void specInvalidReturnEmpty() {
        ComponentSearchDTO dto = new ComponentSearchDTO(
                Optional.empty(), Optional.empty(),
                Optional.empty(),
                Optional.empty(),
                Optional.empty(),
                Optional.of(List.of("Nada")),
                Optional.empty()
        );
        Specification<Product> spec = ComponentSpecification.searchFilter(dto);
        var result = productRepository.findAll(spec);
        assertEquals(List.of(), result);
    }

    @Test
    void specEmptyReturnAll() {
        ComponentSearchDTO dto = ComponentSearchDTO.empty();
        Specification<Product> spec = ComponentSpecification.searchFilter(dto);
        var result = productRepository.findAll(spec);
        assertEquals(List.of(product1, product2, product3), result);
    }
}
