package ar.uba.fi.ingsoft1.product_example.items.ingredients;

import ar.uba.fi.ingsoft1.product_example.items.ComponentSearchDTO;
import ar.uba.fi.ingsoft1.product_example.items.ComponentSpecification;
import ar.uba.fi.ingsoft1.product_example.items.products.Product;
import ar.uba.fi.ingsoft1.product_example.items.products.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

import static ar.uba.fi.ingsoft1.product_example.items.ingredients.IngredientConstants.ERROR_ONLY_COMPONENT;

@Service
@Transactional
@RequiredArgsConstructor
public class IngredientService {

    private final IngredientRepository ingredientRepository;
    private final ProductRepository productRepository;

    public Page<IngredientDTO> getIngredients(ComponentSearchDTO filter, Pageable pageable) {
        Specification<Ingredient> spec = ComponentSpecification.searchFilter(filter);
        return ingredientRepository.findAll(spec, pageable)
                .map(IngredientDTO::new);
    }

    public Optional<IngredientDTO> getIngredientById(long id) {
        return ingredientRepository.findById(id).map(IngredientDTO::new);
    }

    public IngredientDTO createIngredient(IngredientCreateDTO data) {
        Specification<Ingredient> spec = ComponentSpecification.searchByName(data.name());
        if (!ingredientRepository.findAll(spec).isEmpty()) return null;
        return new IngredientDTO(ingredientRepository.save(data.asIngredient()));
    }

    public boolean deleteIngredientById(long id) {
        if (!ingredientRepository.existsById(id))
            return false;
        long count = productRepository.countProductsWhereOnlyIngredientIs(id);
        if (count > 0) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    ERROR_ONLY_COMPONENT);
        }
        ingredientRepository.deleteById(id);
        return true;
    }

    public Optional<IngredientDTO> updateIngredient(Long id, IngredientUpdateDTO update) {
        return ingredientRepository.findById(id)
                .map(update::applyTo)
                .map(ingredientRepository::save)
                .map(IngredientDTO::new);
    }

    public Optional<IngredientDTO> updateStock(Long id, IngredientStockDTO update) {
        return ingredientRepository.findById(id)
                .flatMap(update::applyTo)
                .map(ingredientRepository::save)
                .map(IngredientDTO::new);
    }
}