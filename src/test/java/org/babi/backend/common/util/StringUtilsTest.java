package org.babi.backend.common.util;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class StringUtilsTest {

    @Test
    void toCamelCase_whenTextIsNull_thenReturnNull() {
        // given

        // when
        String result = StringUtils.toCamelCase(null);

        // then
        assertNull(result);
    }

    @ParameterizedTest
    @MethodSource("provideNotCamelCaseText")
    void toCamelCase_whenTextIsNotNull_thenShouldBeConvertedToCamelCase(String text, String expectedResult) {
        // given

        // when
        String result = StringUtils.toCamelCase(text);

        // then
        assertEquals(expectedResult, result);
    }

    private static Stream<Arguments> provideNotCamelCaseText() {
        return Stream.of(
                Arguments.of("NOT_CAMEL_CASE", "notCamelCase"),
                Arguments.of("some_text", "someText")
        );
    }

}