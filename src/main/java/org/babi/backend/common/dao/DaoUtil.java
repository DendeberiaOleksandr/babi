package org.babi.backend.common.dao;

import java.util.List;
import java.util.Optional;

public class DaoUtil {

    public static String buildValuesTuple(List<List<Object>> values) {
        final StringBuilder sql = new StringBuilder();
        Optional.ofNullable(values)
                .ifPresent(v -> v.forEach(tuple -> {
                    sql.append("(");
                    tuple.forEach(t -> {
                        sql.append(String.format("%s, ", t));
                    });
                    final int length = sql.length();
                    sql.replace(length - 2, length, "), ");
                }));
        final int length = sql.length();

        if (length >= 2) {
            sql.replace(length - 2, length, "");
        }

        return sql.toString();
    }

}
