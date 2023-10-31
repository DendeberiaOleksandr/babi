package org.babi.backend.category.service;

import org.babi.backend.category.domain.Category;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface CategoryService {

    default Mono<Long> save(Mono<Category> category) {
        return category.flatMap(this::save);
    }
    Mono<Long> save(Category category);
    Flux<Category> getAll();
    Mono<Category> findById(Long id);
    Mono<Category> update(Long id, Category category);
    Flux<Category> findAllById(Iterable<Long> id);
}
