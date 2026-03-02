package ar.uba.fi.ingsoft1.product_example.items.ingredients;

import ar.uba.fi.ingsoft1.product_example.items.Component;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Entity
@DiscriminatorValue("Ingredient")
@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
public class Ingredient extends Component {
    // TODO: add description and price in ingredients?
    public Ingredient(String name, int stock, String base64Image) {
        super(null, name, "notDescription", 0, stock, base64Image);
    }

    public Ingredient(Long id, String name, int stock, String base64Image) {
        super(id, name, "notDescription", 0, stock, base64Image);
    }

    @Override
    public boolean reduceStock(int stock) {
        if (this.stock < stock || stock < 0)
            return false;
        this.stock -= stock;
        return true;
    }

    @Override
    public boolean addStock(int stock) {
        if (stock < 0)
            return false;
        this.stock += stock;
        return true;
    }

    @Override
    public void add(Component component, Integer quantity) {
    }

    @Override
    public void remove(Component component) {
    }

    @Override
    public Map<Component, Integer> getChildrens() {
        return Map.of();
    }

    @Override
    public List<String> getCategories() {
        return new ArrayList<>(); 
    }

    @Override
    public List<String> getTypes() {
        return new ArrayList<>(); 
    }
}
