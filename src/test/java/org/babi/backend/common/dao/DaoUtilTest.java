package org.babi.backend.common.dao;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.util.Pair;

import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(MockitoExtension.class)
class DaoUtilTest {

    @Test
    void buildUnlinkQuery_whenIdsCollectionIsEmpty_thenThrowException() {
        // given

        // when
        assertThrows(IllegalArgumentException.class, () -> DaoUtil.buildUnlinkQuery("table", 1L, null, "column", "bind", "nested", "nestedBind"));

        // then
    }

    @Test
    void buildUnlinkQuery_whenAllArgsProvided_thenBuildUnlinkQuery() {
        // given
        final String expectedQuery = "delete from table where column = :bind and nested in (:nestedBind0, :nestedBind1, :nestedBind2)";
        final Set<String> expectedBindMapKeys = Set.of(
                "bind",
                "nestedBind0",
                "nestedBind1",
                "nestedBind2"
        );

        // when
        Pair<String, Map<String, Object>> result = DaoUtil.buildUnlinkQuery("table", 1L, Set.of(1L, 2L, 3L), "column", "bind", "nested", "nestedBind");

        // then
        assertExpectedResultForLinkOrUnlinkQuery(expectedQuery, expectedBindMapKeys, result);
    }

    @Test
    void buildLinkQuery_whenIdsCollectionIsEmpty_thenThrowException() {
        // given

        // when
        assertThrows(IllegalArgumentException.class, () -> DaoUtil.buildLinkQuery("table", 1L, null, "column", "bind", "nested", "nestedBind"));

        // then
    }

    @Test
    void buildLinkQuery_whenAllArgsProvided_thenBuildLinkQuery() {
        // given
        final String expectedQuery = "insert into table(column, nested) values (:bind, :nestedBind0), (:bind, :nestedBind1), (:bind, :nestedBind2)";
        final Set<String> expectedBindMapKeys = Set.of(
                "bind",
                "nestedBind0",
                "nestedBind1",
                "nestedBind2"
        );

        // when
        Pair<String, Map<String, Object>> result = DaoUtil.buildLinkQuery("table", 1L, Set.of(1L, 2L, 3L), "column", "nested", "bind", "nestedBind");

        // then
        assertExpectedResultForLinkOrUnlinkQuery(expectedQuery, expectedBindMapKeys, result);
    }

    private void assertExpectedResultForLinkOrUnlinkQuery(String expectedQuery, Set<String> expectedBindMapKeys, Pair<String, Map<String, Object>> result) {
        assertNotNull(result);
        String resultQuery = result.getFirst();
        assertEquals(expectedQuery, resultQuery);
        Map<String, Object> resultBindMap = result.getSecond();
        assertTrue(expectedBindMapKeys.stream().allMatch(resultBindMap::containsKey));
    }

    @Test
    void buildDeleteQuery_whenTableProvided_thenBuildDeleteQuery() {
        // given
        final String expectedQuery = "delete from table";
        final String table = "table";

        // when
        String resultQuery = DaoUtil.buildDeleteQuery(table);

        // then
        assertEquals(expectedQuery, resultQuery);
    }

    @Test
    void buildDeleteByIdQuery_whenAllArgsProvided_thenBuildDeleteByIdQuery() {
        // given
        final String expectedQuery = "delete from table where column = :column";
        final Set<String> expectedBindKeys = Set.of("column");

        // when
        Pair<String, Pair<String, Object>> result = DaoUtil.buildDeleteByIdQuery("table", "column", 1L);

        // then
        assertNotNull(result);
        assertEquals(expectedQuery, result.getFirst());
        Pair<String, Object> bindPair = result.getSecond();
        assertEquals("column", bindPair.getFirst());
        assertEquals(1L, bindPair.getSecond());
    }

}