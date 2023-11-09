package org.babi.backend.common.dao;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(MockitoExtension.class)
class DaoUtilTest {

    @Test
    void buildUnlinkQuery_whenAllArgsProvided_thenShouldBuildSQL() {
        // given
        String expected = "delete from table where id = :id and nested_id in (1)";

        // when
        String result = DaoUtil.buildUnlinkQuery("table", Set.of(1L), "id", "id", "nested_id");

        // then
        assertEquals(expected, result);
    }

    @Test
    void buildLinkQuery_whenAllArgsProvided_thenShouldBuildSQL() {
        // given
        String expected = "insert into table(id, nested_id) values (1, 1)";

        // when
        String result = DaoUtil.buildLinkQuery("table", 1L, Set.of(1L), "id", "nested_id");

        // then
        assertEquals(expected, result);
    }

}