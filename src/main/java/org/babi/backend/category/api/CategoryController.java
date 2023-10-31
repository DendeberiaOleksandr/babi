package org.babi.backend.category.api;

import org.babi.backend.category.domain.Category;
import org.babi.backend.category.service.CategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/v1/categories")
public class CategoryController {

    private final CategoryService categoryService;

    @Autowired
    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @Secured("hasRole('ADMIN')")
    @PostMapping
    public Mono<ResponseEntity<?>> save(@RequestBody Mono<Category> category) {
        return categoryService.save(category).map(ResponseEntity::ok);
    }

    @Secured("permitAll()")
    @GetMapping
    public Flux<Category> getAll() {
        return categoryService.getAll();
    }

    @Secured("permitAll()")
    @GetMapping("/{id}")
    public Mono<ResponseEntity<Category>> getById(@PathVariable Long id) {
        return categoryService.findById(id)
                .map(ResponseEntity::ok);
    }

    @Secured("hasRole('ADMIN')")
    @PutMapping("/{id}")
    public Mono<Long> update(@PathVariable Long id, @RequestBody Category category) {
        return categoryService.update(id, category).map(Category::getId);
    }

}
