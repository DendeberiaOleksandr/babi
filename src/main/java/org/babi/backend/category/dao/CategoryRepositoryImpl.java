package org.babi.backend.category.dao;

import org.babi.backend.category.domain.Category;
import org.babi.backend.common.dao.AbstractRepository;
import org.babi.backend.common.dao.Criteria;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Set;

@Repository
public class CategoryRepositoryImpl extends AbstractRepository<Long, Category> implements CategoryRepository {

    private static final String SELECT_QUERY = "select id, name from category c";

    public CategoryRepositoryImpl(DatabaseClient databaseClient) {
        super(databaseClient);
    }

    @Override
    public Flux<Category> findAll(Criteria criteria) {
        return databaseClient.sql(SELECT_QUERY)
                .map((row, rowMetadata) -> new Category(row.get("id", Long.class), row.get("name", String.class)))
                .all();
    }

    @Override
    public Flux<Category> findAllById(Set<? extends Long> id) {
        return findAll(CategoryCriteria.builder().ids(id).build());
    }

    @Override
    public Mono<Category> findById(Long id) {
        return findAll(CategoryCriteria.builder().id(id).build()).single();
    }

    @Override
    public Mono<Category> save(Category category) {
        final String id = "id";
        return databaseClient.sql("insert into category (name) values (:name)")
                .bind("name", category.getName())
                .filter((statement, next) -> statement.returnGeneratedValues(id).execute())
                .fetch()
                .first()
                .doOnNext(result -> category.setId(Long.parseLong(result.get(id).toString())))
                .thenReturn(category);
    }

    @Override
    public Mono<Void> remove(Long id) {
        return databaseClient.sql("delete from category where id = :id")
                .bind("id", id)
                .fetch()
                .all()
                .then();
    }

    @Override
    public Mono<Void> remove(Category category) {
        return remove(category.getId());
    }

    @Override
    public Mono<Category> update(Long id, Category category) {
        return databaseClient.sql("update category set name = :name where id = :id")
                .bind("name", category.getName())
                .bind("id", id)
                .fetch()
                .all()
                .then(Mono.just(category));
    }

    @Override
    public Mono<Void> deleteAll() {
        return databaseClient.sql("delete from category")
                .fetch()
                .all()
                .then();
    }
}
