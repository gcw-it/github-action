package de.wenda.it.runtime;

import java.time.Period;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AgeParserTest {

    @Test
    void shouldParseAge() {
        var given = "42";
        var expected = Period.ofDays(42);
        var sut = AgeParser.parse(given);

        assertThat(sut.days())
                .isEqualTo(expected);
    }

    @Test
    void shouldThrowNumberFormatException() {
        var given = "invalid";
        var expected = NumberFormatException.class;

        assertThatThrownBy(() -> AgeParser.parse(given))
                .isInstanceOf(expected);
    }
}