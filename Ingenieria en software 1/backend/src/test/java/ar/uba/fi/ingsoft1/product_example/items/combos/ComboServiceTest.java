package ar.uba.fi.ingsoft1.product_example.items.combos;

import ar.uba.fi.ingsoft1.product_example.items.ingredients.Ingredient;
import ar.uba.fi.ingsoft1.product_example.items.products.*;
import ar.uba.fi.ingsoft1.product_example.order.OrderRepository;
import ar.uba.fi.ingsoft1.product_example.promotions.PromotionRepository;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static ar.uba.fi.ingsoft1.product_example.items.ProductConstants.*;
import static ar.uba.fi.ingsoft1.product_example.items.ProductConstants.CATEGORY;
import static ar.uba.fi.ingsoft1.product_example.items.ProductConstants.DESCRIPTION;
import static ar.uba.fi.ingsoft1.product_example.items.ProductConstants.ESTIMATED_TIME;
import static ar.uba.fi.ingsoft1.product_example.items.ProductConstants.ID;
import static ar.uba.fi.ingsoft1.product_example.items.ProductConstants.IMAGE64;
import static ar.uba.fi.ingsoft1.product_example.items.ProductConstants.INGREDIENT_ID;
import static ar.uba.fi.ingsoft1.product_example.items.ProductConstants.NAME;
import static ar.uba.fi.ingsoft1.product_example.items.ProductConstants.PRICE;
import static ar.uba.fi.ingsoft1.product_example.items.ProductConstants.TYPE;
import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.verify;

public class ComboServiceTest {
        private ProductRepository productRepository;
        private ComboRepository comboRepository;
        private EntityManager entityManager;
        private OrderRepository orderRepository;
        private PromotionRepository promotionRepository;
        private ComboService comboService;
        private Combo combo1;
        private List<Ingredient> INGREDIENTS = new ArrayList<>();
        private List<Product> PRODUCTS = new ArrayList<>();
    @BeforeEach
    void setup() {
        productRepository = mock();
        comboRepository = mock();
        entityManager = mock();
        INGREDIENTS.clear();
        PRODUCTS.clear();
        INGREDIENTS.add(new Ingredient(INGREDIENT_ID, INGREDIENT_NAME, STOCK, IMAGE64));
        PRODUCTS.add(new Product(ID, NAME, DESCRIPTION, PRICE, List.of(CATEGORY), TYPE,
                ESTIMATED_TIME, Map.of(INGREDIENTS.getFirst(),1), IMAGE64 ));
        var product1 = PRODUCTS.getFirst();
        when(productRepository.getReferenceById(ID)).thenReturn(product1);
        when(productRepository.findById(ID)).thenReturn(Optional.of(product1));

        when(comboRepository.save(any(Combo.class))).thenAnswer(inv -> {
            Combo combo = inv.getArgument(0);
            combo.setId(ID);
            return combo;
        });
        combo1 = new Combo(ID, NAME, DESCRIPTION, PRICE, List.of(CATEGORY), List.of(TYPE),
                Map.of(PRODUCTS.getFirst(), 1), IMAGE64);
        when(comboRepository.findById(ID)).thenReturn(Optional.of(combo1));

        when(comboRepository.existsById(ID)).thenReturn(true);
        when(comboRepository.existsById(2L)).thenReturn(false);
        orderRepository = mock();
        when(orderRepository.existsActiveOrdersContainingComponent(anyLong(), any())).thenReturn(false);
        when(orderRepository.removeComponentFromFinalizedOrders(anyLong(), any())).thenReturn(0);
        promotionRepository = mock();
        when(promotionRepository.existsActivePromotionsReferencingComponent(any(), any())).thenReturn(false);
        comboService = new ComboService(comboRepository, productRepository, entityManager, orderRepository,
                promotionRepository);
    }

    @Test
    void getComboByIdReturnsDTO() {
        var result = comboService.getComboById(ID);
        assertEquals(new ComboDTO(combo1), result.get());
    }

    @Test
    void getComboByIdReturnsEmptyIfAbsent() {
        when(comboRepository.findById(ID)).thenReturn(Optional.empty());
        var result = comboService.getComboById(1111L);
        assertTrue(result.isEmpty());
    }

    @Test
    void createWritesToDatabase() {
        var dto = new ComboCreateDTO(NAME, DESCRIPTION, PRICE, List.of(CATEGORY), List.of(TYPE),
                Map.of(ID, 2), IMAGE64);
        comboService.createCombo(dto);
        verify(comboRepository).save(any(Combo.class));
    }

    @Test
    void createReturnsCreatedCombo() {
        var dto = new ComboCreateDTO(NAME, DESCRIPTION, PRICE, List.of(CATEGORY), List.of(TYPE),
                Map.of(ID, 2), IMAGE64);
        var result = comboService.createCombo(dto);
        assertEquals(NAME, result.name());
    }
    @Test
    void createHandlesNonExistsProducts(){
        var dto = new ComboCreateDTO(NAME, DESCRIPTION, PRICE, List.of(CATEGORY), List.of(TYPE),
                Map.of(111629786139129L,1), IMAGE64);
        assertThrows(NullPointerException.class, () -> {
            comboService.createCombo(dto);
        });
    }
    @Test
    void createComboWithDuplicateNameReturnsNull() {
        when(comboRepository.findAll(any(Specification.class))).thenReturn(List.of(combo1));
        var newCombo = new ComboCreateDTO(NAME, DESCRIPTION, PRICE, List.of(CATEGORY), List.of(TYPE),
                Map.of(ID, 1), IMAGE64);
        var response = comboService.createCombo(newCombo);
        assertNull(response);
    }
    @Test
    void updateStockUpdatesAddAndReturnsDTO() {
        ComboStockDTO dto = new ComboStockDTO(Optional.of(1));
        System.out.println(PRODUCTS.getFirst().getStock());
        Optional<ComboDTO> result = comboService.updateStock(ID, dto);
        Optional<ComboDTO> comboDTO = comboService.getComboById(ID);
        assertEquals(comboDTO.get().stock(), result.get().stock());
        PRODUCTS.getFirst().recalculateStock();
        assertEquals(101, PRODUCTS.getFirst().getStock());
        assertEquals(101, INGREDIENTS.getFirst().getStock());
    }
    @Test
    void updateStockUpdatesReduceAndReturnsDTO() {
        PRODUCTS.getFirst().recalculateStock();
        combo1.recalculateStock();
        ComboStockDTO dto = new ComboStockDTO(Optional.of(-1));
        System.out.println(PRODUCTS.getFirst().getStock());
        Optional<ComboDTO> result = comboService.updateStock(ID, dto);
        Optional<ComboDTO> comboDTO = comboService.getComboById(ID);
        assertEquals(comboDTO.get().stock(), result.get().stock());
        PRODUCTS.getFirst().recalculateStock();
        assertEquals(99, PRODUCTS.getFirst().getStock());
        assertEquals(99, INGREDIENTS.getFirst().getStock());
    }
    @Test
    void updateStockUpdatesNoneAndReturnsEmpty() {
        PRODUCTS.getFirst().recalculateStock();
        combo1.recalculateStock();
        ComboStockDTO dto = new ComboStockDTO(Optional.of(0));
        System.out.println(PRODUCTS.getFirst().getStock());
        Optional<ComboDTO> result = comboService.updateStock(ID, dto);
        assertTrue(result.isEmpty());
        PRODUCTS.getFirst().recalculateStock();
        assertEquals(100, PRODUCTS.getFirst().getStock());
        assertEquals(100, INGREDIENTS.getFirst().getStock());
    }
    @Test
    void updateStockFailsIfComboStockIsInsufficient() {
        combo1.recalculateStock();
        var initialStock = combo1.getStock();
        var dto = new ComboStockDTO(Optional.of(-10000));
        var result = comboService.updateStock(ID, dto);
        assertTrue(result.isEmpty());
        assertEquals(initialStock, combo1.getStock());
    }
    @Test
    void updateComboNameReturnsUpdatedCombo() {
        String newName = "New Name";
        ComboUpdateDTO update = new ComboUpdateDTO( Optional.of(newName),
                Optional.empty(),
                Optional.empty(),
                Optional.empty(),
                Optional.empty(),
                Optional.empty(),
                Optional.empty(),
                Optional.empty());
        Optional<ComboDTO> dto =  comboService.updateCombo(ID, update);
        assertEquals(newName, dto.get().name());
    }
    @Test
    void updateComboPriceReturnsUpdatedCombo() {
        float newPrice = 50;
        ComboUpdateDTO update = new ComboUpdateDTO( Optional.empty(),
                Optional.empty(),
                Optional.of(newPrice),
                Optional.empty(),
                Optional.empty(),
                Optional.empty(),
                Optional.empty(),
                Optional.empty());
        Optional<ComboDTO> dto =  comboService.updateCombo(ID, update);
        assertEquals(newPrice, dto.get().price());
    }

    @Test
    void updateComboDescriptionReturnsUpdatedCombo() {
        String newDescription = "New Description";
        ComboUpdateDTO update = new ComboUpdateDTO( Optional.empty(),
                Optional.of(newDescription),
                Optional.empty(),
                Optional.empty(),
                Optional.empty(),
                Optional.empty(),
                Optional.empty(),
                Optional.empty());
        Optional<ComboDTO> dto =  comboService.updateCombo(ID, update);
        assertEquals(newDescription, dto.get().description());
    }

    @Test
    void updateComboCategoriesReturnsUpdatedCombo() {
        List<String> newCategories = List.of("New Category");
        ComboUpdateDTO update = new ComboUpdateDTO( Optional.empty(),
                Optional.empty(),
                Optional.empty(),
                Optional.of(newCategories),
                Optional.empty(),
                Optional.empty(),
                Optional.empty(),
                Optional.empty());
        Optional<ComboDTO> dto =  comboService.updateCombo(ID, update);
        assertEquals(newCategories, dto.get().categories());
    }

    @Test
    void updateComboTypesReturnsUpdatedCombo() {
        List<String> newTypes = List.of("New Type");
        ComboUpdateDTO update = new ComboUpdateDTO( Optional.empty(),
                Optional.empty(),
                Optional.empty(),
                Optional.empty(),
                Optional.of(newTypes),
                Optional.empty(),
                Optional.empty(),
                Optional.empty());
        Optional<ComboDTO> dto =  comboService.updateCombo(ID, update);
        assertEquals(newTypes, dto.get().types());
    }

    @Test
    void updateComboAddProductsReturnsUpdatedCombo() {
        var combo = new Combo(ID, NAME, DESCRIPTION, PRICE, List.of(CATEGORY), List.of(TYPE),
                Map.of(), IMAGE64);
        when(comboRepository.findById(ID)).thenReturn(Optional.of(combo));
        Map<Long, Integer> newProducts = Map.of(ID, 1);
        ComboUpdateDTO update = new ComboUpdateDTO( Optional.empty(),
                Optional.empty(),
                Optional.empty(),
                Optional.empty(),
                Optional.empty(),
                Optional.of(newProducts),
                Optional.empty(),
                Optional.empty());
        Optional<ComboDTO> dto =  comboService.updateCombo(ID, update);
        assertEquals(1, dto.get().products().size());
    }

    @Test
    void updateComboDeleteProductsReturnsUpdatedCombo() {
        List<Long> deleteProducts = List.of(ID);
        ComboUpdateDTO update = new ComboUpdateDTO( Optional.empty(),
                Optional.empty(),
                Optional.empty(),
                Optional.empty(),
                Optional.empty(),
                Optional.empty(),
                Optional.of(deleteProducts),
                Optional.empty());
        Optional<ComboDTO> dto =  comboService.updateCombo(ID, update);
        assertTrue(dto.get().products().isEmpty());
    }

    @Test
    void updateComboImageReturnsUpdatedCombo() {
        String newImage = "newImage";
        ComboUpdateDTO update = new ComboUpdateDTO( Optional.empty(),
                Optional.empty(),
                Optional.empty(),
                Optional.empty(),
                Optional.empty(),
                Optional.empty(),
                Optional.empty(),
                Optional.of(newImage));
        Optional<ComboDTO> dto =  comboService.updateCombo(ID, update);
        assertEquals(newImage, dto.get().base64Image());
    }
    @Test
    void updateComboEmptyReturnsTheSame(){
        ComboUpdateDTO update = new ComboUpdateDTO( Optional.empty(),
                Optional.empty(),
                Optional.empty(),
                Optional.empty(),
                Optional.empty(),
                Optional.empty(),
                Optional.empty(),
                Optional.empty());
        Optional<ComboDTO> dto =  comboService.updateCombo(ID, update);
        assertEquals(new ComboDTO(combo1), dto.get());
    }

    @Test
    void deleteExistingComboReturnsTrue() {
        comboService.deleteComboById(ID);
        verify(comboRepository).deleteById(ID);
    }
    @Test
    void deleteNonexistentComboReturnsFalse() {
        boolean result = comboService.deleteComboById(2L);
        assertFalse(result);
        verify(comboRepository, never()).deleteById(anyLong());
    }
}
