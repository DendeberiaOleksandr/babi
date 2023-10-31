package org.babi.backend.category.service;

import lombok.extern.slf4j.Slf4j;
import org.babi.backend.category.dao.CategoryRepository;
import org.babi.backend.category.domain.Category;
import org.babi.backend.common.exception.ResourceNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

@Service
@Slf4j
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;

    @Autowired
    public CategoryServiceImpl(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    @Transactional
    @Override
    public Mono<Long> save(Category category) {
        return categoryRepository.save(category).map(Category::getId);
    }

    @Override
    public Flux<Category> getAll() {
        return categoryRepository.findAll();
    }

    @Override
    public Mono<Category> findById(Long id) {
        return categoryRepository.findById(id)
                .switchIfEmpty(Mono.error(new ResourceNotFoundException(Category.class, "id", id)));
    }

    @Override
    public Mono<Category> update(Long id, Category category) {
        return findById(id)
                .map(dbCategory -> {
                    dbCategory.setName(category.getName());
                    return dbCategory;
                })
                .flatMap(categoryRepository::save);
    }

    @Override
    public Flux<Category> findAllById(Iterable<Long> ids) {
        return categoryRepository.findAllById(ids);
    }
}
