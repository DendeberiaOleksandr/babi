package org.babi.backend.common.dao;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.babi.backend.common.util.StringUtils;
import org.springframework.data.util.Pair;
import org.springframework.r2dbc.core.DatabaseClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Slf4j
public abstract class AbstractRepository<T, K> {

    protected DatabaseClient databaseClient;

    public AbstractRepository(DatabaseClient databaseClient) {
        this.databaseClient = databaseClient;
    }

    protected DatabaseClient.GenericExecuteSpec executeSpecFilledByArgs(StringBuilder sql, Criteria criteria) {
        Map<String, Object> args = new HashMap<>();
        if (criteria != null) {
            args = criteria.mapCriteriaToQueryArgs(sql);
        }
        DatabaseClient.GenericExecuteSpec executeSpec = databaseClient.sql(sql.toString());

        return bindArgsToExecuteSpec(executeSpec, args);
    }

    protected Mono<T> linkNestedEntities(String tableName, T id, Set<K> nestedIds, String identifierColumn, String nestedColumn) {
        return linkNestedEntities(tableName, id, nestedIds, identifierColumn, nestedColumn, StringUtils.toCamelCase(identifierColumn), StringUtils.toCamelCase(nestedColumn));
    }

    protected Mono<T> linkNestedEntities(String tableName, T id, Set<K> nestedIds, String identifierColumn, String nestedColumn, String identifiedBindParam, String nestedIdBindParam) {
        Pair<String, Map<String, Object>> sqlArgumentsPair;
        try {
            sqlArgumentsPair = DaoUtil.buildLinkQuery(tableName, id, nestedIds, identifierColumn, nestedColumn, identifiedBindParam, nestedIdBindParam);
        } catch (IllegalArgumentException e) {
            log.error(e.getMessage());
            return Mono.just(id);
        }
        DatabaseClient.GenericExecuteSpec executeSpec = databaseClient.sql(sqlArgumentsPair.getFirst());
        Map<String, Object> args = sqlArgumentsPair.getSecond();
        return bindArgsToExecuteSpec(executeSpec, args)
                .fetch()
                .all()
                .then(Mono.just(id));
    }

    protected Mono<T> unlinkNestedEntities(String tableName, String identifierColumn, T id) {
        return databaseClient.sql(String.format("delete from %s where %s = :id", tableName, identifierColumn))
                .bind("id", id)
                .fetch()
                .all()
                .then(Mono.just(id));
    }

    protected Flux<T> findAllNestedIds(String tableName, String identifierColumn, T id, String nestedColumn) {
        String identifierBind = String.format("%s", StringUtils.toCamelCase(identifierColumn));
        return databaseClient.sql(String.format("select %s from %s where %s = :%s", nestedColumn, tableName, identifierColumn, identifierBind))
                .bind(identifierBind, id)
                .map((row, rowMetadata) -> (T) row.get(nestedColumn))
                .all();
    }

    protected Mono<T> unlinkNestedEntities(String tableName, T id, Set<K> nestedIds, String identifierColumn, String nestedColumn) {
        return unlinkNestedEntities(tableName, id, nestedIds, identifierColumn, StringUtils.toCamelCase(identifierColumn), nestedColumn, StringUtils.toCamelCase(nestedColumn));
    }

    protected Mono<T> unlinkNestedEntities(String tableName, T id, Set<K> nestedIds, String identifierColumn, String identifierBind, String nestedColumn, String nestedIdentifierBind) {
        Pair<String, Map<String, Object>> sqlArgumentsPair;
        try {
            sqlArgumentsPair = DaoUtil.buildUnlinkQuery(tableName, id, nestedIds, identifierColumn, identifierBind, nestedColumn, nestedIdentifierBind);
        } catch (IllegalArgumentException e) {
            log.error(e.getMessage());
            return Mono.just(id);
        }
        DatabaseClient.GenericExecuteSpec executeSpec = databaseClient.sql(sqlArgumentsPair.getFirst());
        Map<String, Object> args = sqlArgumentsPair.getSecond();
        return bindArgsToExecuteSpec(executeSpec, args)
                .fetch()
                .all()
                .then(Mono.just(id));
    }

    protected Mono<Void> deleteAll(List<String> tables) {
        return Flux.fromStream(tables.stream())
                .flatMap(table -> databaseClient.sql(DaoUtil.buildDeleteQuery(table)).then())
                .then();
    }

    protected Mono<Void> deleteById(List<DeleteByIdParam> deleteByIdParams) {
        return Flux.fromStream(deleteByIdParams.stream())
                .flatMap(deleteByIdParam -> {
                    Pair<String, Pair<String, Object>> deleteByIdSqlBindParam = DaoUtil.buildDeleteByIdQuery(deleteByIdParam.table, deleteByIdParam.identifierColumn, deleteByIdParam.id);
                    Pair<String, Object> bindParam = deleteByIdSqlBindParam.getSecond();
                    return databaseClient.sql(deleteByIdSqlBindParam.getFirst())
                            .bind(bindParam.getFirst(), bindParam.getSecond())
                            .fetch()
                            .all()
                            .then();
                })
                .then();
    }

    private DatabaseClient.GenericExecuteSpec bindArgsToExecuteSpec(DatabaseClient.GenericExecuteSpec executeSpec, Map<String, Object> args) {
        for (Map.Entry<String, Object> arg : args.entrySet()) {
            executeSpec = executeSpec.bind(arg.getKey(), arg.getValue());
        }
        return executeSpec;
    }

    @AllArgsConstructor
    @Data
    protected class DeleteByIdParam {
        private String table;
        private String identifierColumn;
        private T id;
    }

}
