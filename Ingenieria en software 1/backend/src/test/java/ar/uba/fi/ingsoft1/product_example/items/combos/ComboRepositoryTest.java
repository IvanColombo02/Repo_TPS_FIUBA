package ar.uba.fi.ingsoft1.product_example.items.combos;

import ar.uba.fi.ingsoft1.product_example.items.ComponentSearchDTO;
import ar.uba.fi.ingsoft1.product_example.items.ComponentSpecification;
import ar.uba.fi.ingsoft1.product_example.items.ingredients.Ingredient;
import ar.uba.fi.ingsoft1.product_example.items.ingredients.IngredientRepository;
import ar.uba.fi.ingsoft1.product_example.items.products.Product;
import ar.uba.fi.ingsoft1.product_example.items.products.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.jpa.domain.Specification;
import static ar.uba.fi.ingsoft1.product_example.items.ProductConstants.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

@DataJpaTest
class ComboRepositoryTest {

    @Autowired
    private IngredientRepository ingredientRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private ComboRepository comboRepository;

    Ingredient ingredient1;
    Ingredient ingredient2;
    Product product1;
    Product product2;
    Combo combo1;
    Combo combo2;
    Combo combo3;

    @BeforeEach
    void setUp() {
        ingredient1 = ingredientRepository.save(new Ingredient(INGREDIENT_NAME, STOCK, IMAGE64));
        ingredient2 = ingredientRepository.save(new Ingredient(INGREDIENT_NAME + "_2", STOCK + 1, IMAGE64));

        product1 = new Product(
                NAME,
                DESCRIPTION,
                PRICE,
                List.of(CATEGORY),
                TYPE,
                ESTIMATED_TIME,
                Map.of(ingredient1, 1),
                IMAGE64
        );
        product2 = new Product(
                NAME + "_2",
                DESCRIPTION + "_2",
                PRICE + 200,
                List.of(CATEGORY + "_2"),
                TYPE + "_2",
                ESTIMATED_TIME + 5,
                Map.of(ingredient2, 2),
                IMAGE64
        );

        productRepository.save(product1);
        productRepository.save(product2);
        combo1 = new Combo(
                COMBO_NAME,
                COMBO_DESCRIPTION,
                500,
                List.of(COMBO_CATEGORY),
                List.of(COMBO_TYPE),
                Map.of(product1, 2, product2, 1),
                COMBO_IMAGE64
        );

        combo2 = new Combo(
                "CAMBO",
                COMBO_DESCRIPTION + "_2",
                1200,
                List.of("Drink"),
                List.of("TPE"),
                Map.of(product2, 1),
                COMBO_IMAGE64
        );

        combo3 = new Combo(
                "CIMBO",
                COMBO_DESCRIPTION + "_3",
                300,
                List.of("Food"),
                List.of(COMBO_TYPE),
                Map.of(product1, 1),
                COMBO_IMAGE64
        );

        comboRepository.save(combo1);
        comboRepository.save(combo2);
        comboRepository.save(combo3);
    }


    @Test
    void addCombo(){
        combo1 = new Combo(
                COMBO_NAME,
                COMBO_DESCRIPTION,
                500,
                List.of(COMBO_CATEGORY),
                List.of(COMBO_TYPE),
                Map.of(product1, 2, product2, 1),
                COMBO_IMAGE64
        );
        assertEquals(combo1, comboRepository.save(combo1));
    }
    @Test
    void removeCombo(){
        combo1 = new Combo(
                COMBO_NAME,
                COMBO_DESCRIPTION,
                500,
                List.of(COMBO_CATEGORY),
                List.of(COMBO_TYPE),
                Map.of(product1, 2, product2, 1),
                COMBO_IMAGE64
        );
        comboRepository.save(combo1);
        comboRepository.deleteById(combo1.getId());
        assertFalse(comboRepository.findById(combo1.getId()).isPresent());
    }
    @Test
    void specSearchByName() {
        Specification<Combo> spec = ComponentSpecification.searchByName(COMBO_NAME);
        var result = comboRepository.findAll(spec);
        assertEquals(List.of(combo1), result);
    }

    @Test
    void specSearchByNameNotFound() {
        Specification<Combo> spec = ComponentSpecification.searchByName("null");
        var result = comboRepository.findAll(spec);
        assertEquals(List.of(), result);
    }

    @Test
    void specSearchByCategory() {
        Specification<Combo> spec = ComponentSpecification.searchByCategories(List.of(COMBO_CATEGORY));
        var result = comboRepository.findAll(spec);
        assertEquals(List.of(combo1), result);
    }

    @Test
    void specSearchByCategoryNotFound() {
        Specification<Combo> spec = ComponentSpecification.searchByCategories(List.of("null"));
        var result = comboRepository.findAll(spec);
        assertEquals(List.of(), result);
    }
    @Test
    void specSearchByType() {
        Specification<Combo> spec = ComponentSpecification.searchByTypes(List.of(COMBO_TYPE));
        var result = comboRepository.findAll(spec);
        assertEquals(List.of(combo1,combo3), result);
    }
    @Test
    void specSearchByTypeNotFound() {
        Specification<Combo> spec = ComponentSpecification.searchByTypes(List.of("null"));
        var result = comboRepository.findAll(spec);
        assertEquals(List.of(), result);
    }
    @Test
    void specSearchByPriceMin() {
        int priceMin = 1000;
        Specification<Combo> spec = ComponentSpecification.searchByPrice(priceMin, null);
        var result = comboRepository.findAll(spec);
        assertEquals(List.of(combo2), result);
    }

    @Test
    void specSearchByPriceMax() {
        int priceMax = 900;
        Specification<Combo> spec = ComponentSpecification.searchByPrice(null, priceMax);
        var result = comboRepository.findAll(spec);
        assertEquals(List.of(combo1, combo3), result);
    }

    @Test
    void specSearchByPriceMinMax() {
        int priceMin = 400;
        int priceMax = 1000;
        Specification<Combo> spec = ComponentSpecification.searchByPrice(priceMin, priceMax);
        var result = comboRepository.findAll(spec);
        assertEquals(List.of(combo1), result);
    }
    @Test
    void specInvalidReturnEmpty() {
        ComponentSearchDTO dto = new ComponentSearchDTO(
                Optional.empty(),
                Optional.empty(),
                Optional.empty(),
                Optional.empty(),
                Optional.empty(),
                Optional.of(List.of("Nada")),
                Optional.empty()
        );
        Specification<Combo> spec = ComponentSpecification.searchFilter(dto);
        var result = comboRepository.findAll(spec);
        assertEquals(List.of(), result);
    }
    @Test
    void specEmptyReturnAll() {
        ComponentSearchDTO dto = ComponentSearchDTO.empty();
        Specification<Combo> spec = ComponentSpecification.searchFilter(dto);
        var result = comboRepository.findAll(spec);
        assertEquals(List.of(combo1, combo2, combo3), result);
    }
}
