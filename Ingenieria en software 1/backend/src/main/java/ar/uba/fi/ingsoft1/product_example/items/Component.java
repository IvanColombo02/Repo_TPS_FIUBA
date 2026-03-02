package ar.uba.fi.ingsoft1.product_example.items;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Entity
@Table(name = "components")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "component_type", discriminatorType = DiscriminatorType.STRING)
@Data
@NoArgsConstructor
@AllArgsConstructor
public abstract class Component {
    @Id
    @GeneratedValue
    private Long id;
    private String name;
    private String description;
    private float price;
    protected int stock;
    @Column(columnDefinition = "TEXT")
    private String base64Image;

    public abstract boolean reduceStock(int stock);

    public abstract boolean addStock(int stock);

    public abstract void add(Component component, Integer quantity);

    public abstract void remove(Component component);

    public abstract Map<Component, Integer> getChildrens();

 
    public abstract List<String> getCategories();

    public abstract List<String> getTypes();
}
