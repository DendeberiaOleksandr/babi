package org.babi.backend.category.dao;

import org.babi.backend.category.domain.Category;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

@Repository
public interface CategoryRepository extends ReactiveCrudRepository<Category, Long> {

}
