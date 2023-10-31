package org.babi.backend.common.dao;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class DaoUtilTest {

    @Test
    void buildValuesTuple_whenNullListProvided_thenReturnEmptyString() {
        // given

        // when
        String result = DaoUtil.buildValuesTuple(null);

        // then
        assertFalse(StringUtils.hasText(result));
    }

    @ParameterizedTest
    @MethodSource("provideTuples")
    void buildValuesTuple_whenOneTupleWithOneValueProvided_thenReturnTuple(String expectedResult, List<List<Object>> values) {
        // given

        // when
        String result = DaoUtil.buildValuesTuple(values);

        // then
        assertEquals(expectedResult, result);
    }

    private static Stream<Arguments> provideTuples() {
        return Stream.of(
                Arguments.of("(1)", List.of(
                        List.of("1")
                )),
                Arguments.of("(1, 2)", List.of(
                        List.of("1", "2")
                )),
                Arguments.of("(1), (1)", List.of(
                        List.of("1"),
                        List.of("1")
                )),
                Arguments.of("(1, 2), (1)", List.of(
                        List.of("1", "2"),
                        List.of("1")
                )),
                Arguments.of("(1, 2), (1, 2)", List.of(
                        List.of("1", "2"),
                        List.of("1", "2")
                ))
        );
    }

}