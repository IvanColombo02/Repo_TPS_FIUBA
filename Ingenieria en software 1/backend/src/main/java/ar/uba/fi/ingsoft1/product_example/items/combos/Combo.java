package ar.uba.fi.ingsoft1.product_example.items.combos;

import ar.uba.fi.ingsoft1.product_example.items.Component;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static ar.uba.fi.ingsoft1.product_example.items.combos.ComboConstants.MIN_COMBO_QUANTITY;

@Entity
@DiscriminatorValue("Combo")
@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Combo extends Component {
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "combo_categories", foreignKey = @ForeignKey(foreignKeyDefinition = "FOREIGN KEY (combo_id) REFERENCES components(id) ON DELETE CASCADE"))
    @Column(name = "category")
    private List<String> categories;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "combo_types", foreignKey = @ForeignKey(foreignKeyDefinition = "FOREIGN KEY (combo_id) REFERENCES components(id) ON DELETE CASCADE"))
    @Column(name = "type")
    private List<String> types;

    @ElementCollection
    @CollectionTable(name = "combo_products", foreignKey = @ForeignKey(foreignKeyDefinition = "FOREIGN KEY (combo_id) REFERENCES components(id) ON DELETE CASCADE"))
    @MapKeyJoinColumn(name = "products_id", foreignKey = @ForeignKey(foreignKeyDefinition = "FOREIGN KEY (products_id) REFERENCES components(id) ON DELETE CASCADE"))
    @Column(name = "quantity")
    Map<Component, Integer> products;

    public Combo(String name, String description, float price,
            List<String> categories, List<String> types, Map<Component, Integer> products, String base64Image) {
        super(null, name, description, price, 0, base64Image);
        this.categories = categories != null ? new ArrayList<>(categories) : new ArrayList<>();
        this.types = types != null ? new ArrayList<>(types) : new ArrayList<>();
        this.products = products != null ? new HashMap<>(products) : new HashMap<>();
    }

    public Combo(Long id, String name, String description, float price,
            List<String> categories, List<String> types, Map<Component, Integer> products,
            String base64Image) {
        super(id, name, description, price, 0, base64Image);
        this.categories = categories != null ? new ArrayList<>(categories) : new ArrayList<>();
        this.types = types != null ? new ArrayList<>(types) : new ArrayList<>();
        this.products = products != null ? new HashMap<>(products) : new HashMap<>();
    }

    @PrePersist
    @PreUpdate
    public void recalculateStock() {
        this.stock = products.entrySet().stream()
                .mapToInt(entry -> entry.getKey().getStock() / entry.getValue())
                .min()
                .orElse(MIN_COMBO_QUANTITY);
    }

    @Override
    public boolean reduceStock(int stock) {
        if (this.stock < stock || stock < MIN_COMBO_QUANTITY)
            return false;
        for (Map.Entry<Component, Integer> ing : products.entrySet())
            ing.getKey().reduceStock(ing.getValue() * stock);
        this.stock -= stock;
        return true;
    }

    @Override
    public boolean addStock(int stock) {
        if (stock < MIN_COMBO_QUANTITY)
            return false;
        for (Map.Entry<Component, Integer> ing : products.entrySet())
            ing.getKey().addStock(ing.getValue() * stock);
        this.stock += stock;
        return true;
    }

    @Override
    public void add(Component component, Integer quantity) {
        this.products.put(component, quantity);
    }

    @Override
    public void remove(Component component) {
        this.products.remove(component);
    }

    @Override
    public Map<Component, Integer> getChildrens() {
        return this.products;
    }

    @Override
    public List<String> getCategories() {
        return categories != null ? new ArrayList<>(categories) : new ArrayList<>();
    }

    @Override
    public List<String> getTypes() {
        return types != null ? new ArrayList<>(types) : new ArrayList<>();
    }
}
