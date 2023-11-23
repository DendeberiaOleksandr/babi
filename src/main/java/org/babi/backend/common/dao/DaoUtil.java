package org.babi.backend.common.dao;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import org.babi.backend.common.util.StringUtils;
import org.springframework.data.util.Pair;
import org.springframework.util.CollectionUtils;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.IntStream;

public class DaoUtil {

    private static final int ENDING_COMMA_WITH_WHITESPACE_LENGTH = 2;

    public static Pair<String, Map<String, Object>> buildUnlinkQuery(String table, Object id, Set<?> ids, String identifierColumn, String identifierBind, String nestedColumn, String nestedIdentifierBind) {
        if (CollectionUtils.isEmpty(ids)) {
            throw new IllegalArgumentException("Ids should not be empty");
        }
        return buildUnlinkQuery(table, id, List.of(ids.toArray()), identifierColumn, identifierBind, nestedColumn, nestedIdentifierBind);
    }

    private static Pair<String, Map<String, Object>> buildUnlinkQuery(String table, Object id, List<?> ids, String identifierColumn, String identifierBind, String nestedColumn, String nestedIdentifierBind) {
        final StringBuilder sql = new StringBuilder(String.format("delete from %s where %s = :%s and %s in (", table, identifierColumn, identifierBind, nestedColumn));
        Map<String, Object> args = new HashMap<>();
        args.put(identifierBind, id);
        IntStream.range(0, ids.size())
                        .forEach(index -> {
                            String nestedBindParam = nestedIdentifierBind + index;
                            sql.append(String.format(":%s, ", nestedBindParam));
                            args.put(nestedBindParam, ids.get(index));
                        });
        final int length = sql.length();
        // remove ending comma and whitespace
        return Pair.of(sql.replace(length - ENDING_COMMA_WITH_WHITESPACE_LENGTH, length, ")").toString(), args);
    }

    public static Pair<String, Map<String, Object>> buildLinkQuery(String table, Object id, Set<?> nestedIds, String identifierColumn, String nestedColumn, String identifiedBindParam, String nestedIdBindParam) {
        if (CollectionUtils.isEmpty(nestedIds)) {
            throw new IllegalArgumentException("Nested ids collection should not be empty!");
        }
        return buildLinkQuery(table, id, List.of(nestedIds.toArray()), identifierColumn, nestedColumn, identifiedBindParam, nestedIdBindParam);
    }

    private static Pair<String, Map<String, Object>> buildLinkQuery(String table, Object id, List<?> nestedIds, String identifierColumn, String nestedColumn, String identifiedBindParam, String nestedIdBindParam) {
        final StringBuilder sql = new StringBuilder(String.format("insert into %s(%s, %s) values ", table, identifierColumn, nestedColumn));
        Map<String, Object> args = new HashMap<>();
        IntStream.range(0, nestedIds.size())
                .forEach(index -> {
                    String nestedIdParamName = nestedIdBindParam + index;
                    sql.append(String.format("(:%s, :%s), ", identifiedBindParam, nestedIdParamName));
                    args.putIfAbsent(identifiedBindParam, id);
                    args.put(nestedIdParamName, nestedIds.get(index));
                });
        final int length = sql.length();
        // remove ending comma and whitespace
        return Pair.of(sql.replace(length - ENDING_COMMA_WITH_WHITESPACE_LENGTH, length, "").toString(), args);
    }

    public static String buildDeleteQuery(String table) {
        return String.format("delete from %s", table);
    }

    public static Pair<String, Pair<String, Object>> buildDeleteByIdQuery(String table, String identifierColumn, Object id) {
        String identifierBind = StringUtils.toCamelCase(identifierColumn);
        return Pair.of(String.format("delete from %s where %s = :%s", table, identifierColumn, identifierBind), Pair.of(identifierBind, id));
    }

    @Nullable
    public static Pair<String, Map<String, Object>> buildInStatementTuple(Collection<?> values, @Nonnull String bindName) {
        if (CollectionUtils.isEmpty(values)) {
            return null;
        }
        StringBuilder sql = new StringBuilder("(");
        Map<String, Object> args = new HashMap<>();
        Object[] array = values.toArray();
        IntStream.range(0, array.length)
                .forEach(index -> {
                    String bind = bindName + index;
                    sql.append(String.format(":%s, ", bind));
                    args.put(bind, array[index]);
                });
        int sqlLength = sql.length();
        return Pair.of(
                sql.replace(sqlLength - ENDING_COMMA_WITH_WHITESPACE_LENGTH, sqlLength, ")").toString(),
                args
        );
    }

}
