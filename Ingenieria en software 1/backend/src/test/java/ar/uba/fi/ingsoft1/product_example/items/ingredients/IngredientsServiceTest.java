package ar.uba.fi.ingsoft1.product_example.items.ingredients;

import ar.uba.fi.ingsoft1.product_example.items.products.ProductCreateDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static ar.uba.fi.ingsoft1.product_example.items.ProductConstants.*;
import ar.uba.fi.ingsoft1.product_example.items.products.ProductRepository;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;
import java.util.Map;

import java.util.List;
import java.util.Map;
import java.util.Optional;

class IngredientsServiceTest {

    private IngredientRepository ingredientRepository;
    private IngredientService ingredientService;
    private ProductRepository productRepository;

    Ingredient ingredient;
    @BeforeEach
    void setup() {
        ingredientRepository = mock();
        productRepository = mock();
        ingredient = new Ingredient(ID, NAME, STOCK, IMAGE64);
        when(ingredientRepository.save(any(Ingredient.class))).thenAnswer(invocation -> {
            Ingredient arg = invocation.getArgument(0);
            arg.setId(ID); // Assign an ID to the saved ingredient
            return arg;
        });
        when(ingredientRepository.findById(ID)).thenReturn(Optional.of(ingredient));
        when(ingredientRepository.existsById(ID)).thenReturn(true);
        when(ingredientRepository.existsById(2L)).thenReturn(false);
        ingredientService = new IngredientService(ingredientRepository, productRepository);
    }

    @Test
    void getProductByIdReturnsDTO() {
        var result = ingredientService.getIngredientById(ID);
        assertEquals(new IngredientDTO(ingredient), result.get());
    }

    @Test
    void getProductByIdReturnsEmptyIfAbsent() {
        when(ingredientRepository.findById(1111L)).thenReturn(Optional.empty());
        var result = ingredientService.getIngredientById(1111L);
        assertTrue(result.isEmpty());
    }

    @Test
    void createWritesToDatabase() {
        // Tests that creating an ingredient writes to the database
        var newIngredient = new IngredientCreateDTO(NAME, STOCK, IMAGE64);
        ingredientService.createIngredient(newIngredient);
        verify(ingredientRepository).save(any(Ingredient.class));
    }

    @Test
    void deleteIngredientById() {
        // Tests that deleting an existing ingredient returns true
        boolean result = ingredientService.deleteIngredientById(ID);
        assertTrue(result);
        verify(ingredientRepository).deleteById(ID);
    }

    @Test
    void cantDeleteAbsentIngredient() {
        // Tests that deleting a non-existent ingredient returns false
        boolean result = ingredientService.deleteIngredientById(2L);
        assertFalse(result);
    }

    @Test
    void createIngredientWithDuplicateNameReturnsNull() {
        var ingredient  = new Ingredient(ID, NAME, STOCK, IMAGE64);
        when(ingredientRepository.findAll(any(Specification.class))).thenReturn(List.of(ingredient));
        var newIngredient = new IngredientCreateDTO(NAME, STOCK, IMAGE64);
        var response = ingredientService.createIngredient(newIngredient);
        assertNull(response);
    }
}