package ar.uba.fi.ingsoft1.product_example.promotions;

import static ar.uba.fi.ingsoft1.product_example.promotions.PromotionConstants.DEFAULT_PRIORITY;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@Table(name = "promotions")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Promotion {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(length = 500)
    private String description;

    @Column(nullable = false)
    private LocalDate fromDate;

    @Column(nullable = false)
    private LocalDate toDate;

    private String expression; // JSON string
    @Column(columnDefinition = "TEXT")
    private String base64Image;

    @Column(nullable = false, columnDefinition = "integer default 0")
    private Integer priority = DEFAULT_PRIORITY;

    public Promotion(String name, String description, LocalDate fromDate, LocalDate toDate, String expression,
            String base64Image, Integer priority) {
        this.name = name;
        this.description = description;
        this.fromDate = fromDate;
        this.toDate = toDate;
        this.expression = expression;
        this.base64Image = base64Image;
        this.priority = priority != null ? priority : DEFAULT_PRIORITY;
    }
}
