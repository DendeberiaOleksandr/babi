package org.babi.backend.common.dao;

import java.util.Set;

public class DaoUtil {

    private static final int ENDING_COMMA_WITH_WHITESPACE_LENGTH = 2;

    public static String buildUnlinkQuery(String table, Set<Long> ids, String identifierColumn, String identifierBind, String nestedColumn) {
        final StringBuilder sql = new StringBuilder(String.format("delete from %s where %s = :%s and %s in (", table, identifierColumn, identifierBind, nestedColumn));
        ids.forEach(id -> sql.append(String.format("%d, ", id)));
        final int length = sql.length();
        // remove ending comma and whitespace
        return sql.replace(length - ENDING_COMMA_WITH_WHITESPACE_LENGTH, length, ")").toString();
    }

    public static String buildLinkQuery(String table, Long id, Set<Long> nestedIds, String identifierColumn, String nestedColumn) {
        final StringBuilder sql = new StringBuilder(String.format("insert into %s(%s, %s) values ", table, identifierColumn, nestedColumn));
        nestedIds.forEach(nestedId -> sql.append(String.format("(%d, %d), ", id, nestedId)));
        final int length = sql.length();
        // remove ending comma and whitespace
        return sql.replace(length - ENDING_COMMA_WITH_WHITESPACE_LENGTH, length, "").toString();
    }

}
