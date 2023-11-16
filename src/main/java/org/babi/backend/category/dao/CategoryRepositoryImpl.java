package org.babi.backend.category.dao;

import org.babi.backend.category.domain.Category;
import org.babi.backend.common.dao.AbstractRepository;
import org.babi.backend.common.dao.Criteria;
import org.babi.backend.common.dao.PageableResponse;
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
    public Flux<Category> findAll() {
        return findAll(null);
    }

    @Override
    public Flux<Category> findAllById(Set<? extends Long> id) {
        return findAll(CategoryCriteria.builder().ids(id).build());
    }

    private Flux<Category> findAll(CategoryCriteria criteria) {
        StringBuilder sql = new StringBuilder(SELECT_QUERY);
        DatabaseClient.GenericExecuteSpec executeSpec = executeSpecFilledByArgs(sql, criteria);
        return executeSpec.map((row, rowMetadata) -> new Category(row.get("id", Long.class), row.get("name", String.class)))
                .all();
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
    public Mono<Void> delete(Long id) {
        return databaseClient.sql("delete from category where id = :id")
                .bind("id", id)
                .fetch()
                .all()
                .then();
    }

    @Override
    public Mono<Void> delete(Category category) {
        return delete(category.getId());
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

    @Override
    public Mono<PageableResponse<Category>> search(Criteria criteria) {
        return findAll((CategoryCriteria) criteria)
                .collectList()
                .flatMap(categories -> count(criteria).map(count -> new PageableResponse<>(categories, count)));
    }

    @Override
    public Mono<Long> count(Criteria criteria) {
        final StringBuilder sql = new StringBuilder("select count(*) from category");

        DatabaseClient.GenericExecuteSpec executeSpec = executeSpecFilledByArgs(sql, criteria);

        return executeSpec
                .map((row, rowMetadata) -> row.get(0, Long.class))
                .first();
    }
}
