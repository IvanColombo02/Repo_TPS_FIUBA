package ar.uba.fi.ingsoft1.product_example.items.products;

import ar.uba.fi.ingsoft1.product_example.items.Component;
import ar.uba.fi.ingsoft1.product_example.items.ingredients.Ingredient;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static ar.uba.fi.ingsoft1.product_example.items.products.ProductConstants.MIN_STOCK_QUANTITY;

@Entity
@DiscriminatorValue("Product")
@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Product extends Component {
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "product_categories", foreignKey = @ForeignKey(foreignKeyDefinition = "FOREIGN KEY (product_id) REFERENCES components(id) ON DELETE CASCADE"))
    @Column(name = "category")
    private List<String> categories;
    private String type;
    private int estimatedTime; // On minutes

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "product_ingredients", foreignKey = @ForeignKey(foreignKeyDefinition = "FOREIGN KEY (product_id) REFERENCES components(id) ON DELETE CASCADE"))
    @MapKeyJoinColumn(name = "ingredients_id", foreignKey = @ForeignKey(foreignKeyDefinition = "FOREIGN KEY (ingredients_id) REFERENCES components(id) ON DELETE CASCADE"))
    @Column(name = "quantity")
    private Map<Ingredient, Integer> ingredients;

    public Product(String name, String description, float price, List<String> categories, String type,
            int estimatedTime, Map<Ingredient, Integer> ingredients, String base64Image) {
        super(null, name, description, price, 0, base64Image);
        this.categories = categories != null ? new ArrayList<>(categories) : new ArrayList<>();
        this.estimatedTime = estimatedTime;
        this.type = type;
        this.ingredients = ingredients != null ? new HashMap<>(ingredients) : new HashMap<>();
    }

    public Product(Long id, String name, String description, float price, List<String> categories, String type,
            int estimatedTime, Map<Ingredient, Integer> ingredients, String base64Image) {
        super(id, name, description, price, 0, base64Image);
        this.categories = categories != null ? new ArrayList<>(categories) : new ArrayList<>();
        this.ingredients = ingredients != null ? new HashMap<>(ingredients) : new HashMap<>();
        this.estimatedTime = estimatedTime;
        this.type = type;
    }

    @PrePersist
    @PreUpdate
    public void recalculateStock() {
        this.stock = ingredients.entrySet().stream()
                .mapToInt(entry -> entry.getKey().getStock() / entry.getValue())
                .min()
                .orElse(0);
    }

    @Override
    public boolean reduceStock(int stock) {
        if (this.stock < stock || stock < MIN_STOCK_QUANTITY)
            return false;
        for (Map.Entry<Ingredient, Integer> ing : ingredients.entrySet())
            if (ing.getKey().getStock() < ing.getValue() * stock)
                return false;
        for (Map.Entry<Ingredient, Integer> ing : ingredients.entrySet())
            ing.getKey().reduceStock(ing.getValue() * stock);
        this.stock -= stock;
        return true;
    }

    @Override
    public boolean addStock(int stock) {
        if (stock < MIN_STOCK_QUANTITY)
            return false;
        for (Map.Entry<Ingredient, Integer> ing : ingredients.entrySet())
            ing.getKey().addStock(ing.getValue() * stock);
        this.stock += stock;
        return true;
    }

    @Override
    public void add(Component component, Integer quantity) {
        this.ingredients.put((Ingredient) component, quantity);
    }

    @Override
    public void remove(Component component) {
        this.ingredients.remove(component);
    }

    @Override
    public Map<Component, Integer> getChildrens() {
        Map<Component, Integer> result = new java.util.HashMap<>();
        for (Map.Entry<Ingredient, Integer> entry : ingredients.entrySet()) {
            result.put(entry.getKey(), entry.getValue());
        }
        return result;
    }

    @Override
    public List<String> getCategories() {
        return categories != null ? new ArrayList<>(categories) : new ArrayList<>();
    }

    @Override
    public List<String> getTypes() {
        return type != null ? List.of(type) : new ArrayList<>();
    }
}
