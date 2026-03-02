package ar.uba.fi.ingsoft1.product_example.items;

import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public class ComponentSpecification {
    private final static String NAME = "name";
    private final static String PRICE = "price";
    private final static String CATEGORIES = "categories";
    private final static String TYPE = "type";
    private final static String TYPES = "types";

    public static <Component> Specification<Component> searchByName(String name) {
        return (root, query, builder) -> {
            try {
                if (name == null)
                    return builder.conjunction();
                return builder.like(
                    builder.lower(root.get(NAME)),
                    "%" + name.toLowerCase() + "%"
                );
            } catch (IllegalArgumentException e) {
                return builder.disjunction();
            }
        };
    }
    public static <Component> Specification<Component> searchByCategories(List<String> categories) {
        return (root, query, builder) -> {
            try {
                if (categories == null || categories.isEmpty()) return builder.conjunction();
                List<Predicate> predicates = new ArrayList<>();
                for (String cat : categories)
                    predicates.add(builder.isMember(cat, root.get(CATEGORIES)));
                return builder.or(predicates.toArray(new Predicate[0]));
            } catch (IllegalArgumentException e) {
                return builder.disjunction();
            }
        };
    }
    public static <Component> Specification<Component> searchByTypes(List<String> types) {
        return (root, query, builder) -> {
            if (types == null || types.isEmpty()) {
                return builder.conjunction();
            }
            try {
                root.get(TYPES);
                List<Predicate> predicates = new ArrayList<>();
                for (String type : types) {
                    predicates.add(builder.isMember(type, root.get(TYPES)));
                }
                return builder.or(predicates.toArray(new Predicate[0]));
            } catch (IllegalArgumentException e) {
                try {
                    root.get(TYPE);
                    List<Predicate> predicates = new ArrayList<>();
                    for (String type : types) {
                        predicates.add(builder.equal(root.get(TYPE), type));
                    }
                    return builder.or(predicates.toArray(new Predicate[0]));
                } catch (IllegalArgumentException e2) {
                    return builder.disjunction();
                }
            }
        };
    }
    public static <Component> Specification<Component> searchByPrice(Integer priceMin, Integer priceMax) {
        return (root, query, builder) -> {
            try {
                List<Predicate> predicates = new ArrayList<>();
                if (priceMin != null)
                    predicates.add(builder.greaterThanOrEqualTo(root.get(PRICE), priceMin));
                if (priceMax != null)
                    predicates.add(builder.lessThanOrEqualTo(root.get(PRICE), priceMax));
                return builder.and(predicates.toArray(new Predicate[0]));
            } catch (IllegalArgumentException e) {
                return builder.disjunction();
            }
        };
    }

    public static <Component> Specification<Component> searchFilter(ComponentSearchDTO dto) {
        return (Specification<Component>) Specification.where(searchByName(dto.name().orElse("")))
                .and(searchByCategories(dto.categories().orElse(null)))
                .and(searchByPrice(dto.priceMin().orElse(null), dto.priceMax().orElse(null)))
                .and(searchByTypes(dto.type().orElse(null)));
    }
}
