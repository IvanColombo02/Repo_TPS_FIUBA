package ar.uba.fi.ingsoft1.product_example.items;
import ar.uba.fi.ingsoft1.product_example.items.combos.Combo;
import ar.uba.fi.ingsoft1.product_example.items.ingredients.Ingredient;
import ar.uba.fi.ingsoft1.product_example.items.products.Product;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static ar.uba.fi.ingsoft1.product_example.items.ProductConstants.*;
import static ar.uba.fi.ingsoft1.product_example.items.ProductConstants.IMAGE64;
import static org.junit.jupiter.api.Assertions.*;

public class ComponentSimpleTest {
    Ingredient ingredient = new Ingredient(INGREDIENT_ID, INGREDIENT_NAME, STOCK, IMAGE64);
    Product product = new Product(ID, NAME, DESCRIPTION, PRICE, List.of(CATEGORY), TYPE,
            ESTIMATED_TIME, Map.of(ingredient,1), IMAGE64 );
    Combo combo = new Combo(ID, NAME, DESCRIPTION, PRICE, List.of(CATEGORY), List.of(TYPE),
            Map.of(product, 1), IMAGE64);;
    @Test
    void updateComboStockWithNegativeStock() {
        assertFalse(combo.addStock(-1));
        assertFalse(combo.reduceStock(-1));
    }
    @Test
    void updateComboStockWithPositiveStock() {
        int stock = combo.getStock();
        combo.addStock(1);
        assertEquals(combo.getStock(), stock + 1);
    }
    @Test
    void updateProductStockWithNegativeStock() {
        assertFalse(product.addStock(-1));
        assertFalse(product.reduceStock(-1));
    }
    @Test
    void updateProductStockWithPositiveStock() {
        int stock = product.getStock();
        product.addStock(1);
        assertEquals(product.getStock(), stock + 1);
    }

    @Test
    void comboAddAddsNewComponent() {
        Product newProduct = new Product(ID +1 , NAME + "_2", DESCRIPTION, PRICE, List.of(CATEGORY), TYPE,
                ESTIMATED_TIME, Map.of(ingredient,1), IMAGE64);
        combo.add(newProduct, 2);
        assertTrue(combo.getProducts().containsKey(newProduct));
        assertEquals(2, combo.getProducts().get(newProduct));
    }

    @Test
    void comboAddUpdatesExistingComponentQuantity() {
        combo.add(product, 5);
        assertEquals(5, combo.getProducts().get(product));
    }

    @Test
    void comboRemoveRemovesExistingComponent() {
        combo.remove(product);
        assertFalse(combo.getProducts().containsKey(product));
    }

    @Test
    void comboRemoveDoesNothingForNonExistentComponent() {
        Product nonExistentProduct = new Product(ID+3, NAME+ "_3", DESCRIPTION, PRICE, List.of(CATEGORY), TYPE,
                ESTIMATED_TIME, Map.of(ingredient,1), IMAGE64 );
        combo.remove(nonExistentProduct);
        assertTrue(combo.getProducts().containsKey(product));
    }

    @Test
    void productAddsNewIngredient() {
        var newIngredient = new Ingredient(INGREDIENT_ID + 1, INGREDIENT_NAME + "_2", STOCK, IMAGE64);
        product.add(newIngredient, 2);
        assertTrue(product.getIngredients().containsKey(newIngredient));
        assertEquals(2, product.getIngredients().get(newIngredient));
    }

    @Test
    void productAddUpdatesExistingIngredientQuantity() {
        product.add(ingredient, 5);
        assertEquals(5, product.getIngredients().get(ingredient));
    }

    @Test
    void productRemoveRemovesExistingIngredient() {
        product.remove(ingredient);
        assertFalse(product.getIngredients().containsKey(ingredient));
    }

    @Test
    void productRemoveDoesNothingForNonExistentIngredient() {
        var nonExistentIngredient = new Ingredient(INGREDIENT_ID + 3, INGREDIENT_NAME + "_3", STOCK, IMAGE64);
        product.remove(nonExistentIngredient);
        assertTrue(product.getIngredients().containsKey(ingredient));
    }

    @Test
    void productGetCategories() {
        Product product = new Product();
        product.setCategories(List.of("Category1", "Category2"));
        List<String> categories = product.getCategories();
        assertEquals(List.of("Category1", "Category2"), categories);
    }

    @Test
    void productGetTypes() {
        Product product = new Product();
        product.setType("Type1");
        List<String> types = product.getTypes();
        assertEquals(List.of("Type1"), types);
    }

    @Test
    void comboGetCategories() {
        Combo combo = new Combo();
        combo.setCategories(List.of("Category1", "Category2"));
        List<String> categories = combo.getCategories();
        assertEquals(List.of("Category1", "Category2"), categories);
    }
    @Test
    void comboGetTypes() {
        Combo combo = new Combo();
        combo.setTypes(List.of("Type1", "Type2"));
        List<String> types = combo.getTypes();
        assertEquals(List.of("Type1", "Type2"), types);
    }
}