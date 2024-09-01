package de.wenda.it.runtime;

import io.quarkiverse.githubaction.Inputs;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.kohsuke.github.GHWorkflowRun.Conclusion;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Period;
import java.util.EnumSet;
import java.util.Optional;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.kohsuke.github.GHWorkflowRun.Conclusion.*;
import static org.mockito.ArgumentMatchers.matches;
import static org.mockito.Mockito.when;

@SuppressWarnings("OptionalUsedAsFieldOrParameterType")
@ExtendWith(MockitoExtension.class)
class InputsParserTest {
    @Mock
    Inputs inputs;

    @Test
    void shouldParseAge() {
        var given = "42";
        when(inputs.get(matches("age"))).thenReturn(Optional.of(given));

        var sut = InputsParser.of(inputs);
        assertThat(sut.age()).hasValue(Period.ofDays(42));
    }

    static Stream<Optional<String>> ageProvider() {
        return Stream.of(Optional.of("invalid"), Optional.empty());
    }
    @ParameterizedTest
    @MethodSource("ageProvider")
    void shouldReturnOptionalEmpty(Optional<String> age) {
        when(inputs.get(matches("age"))).thenReturn(age);

        var sut = InputsParser.of(inputs);
        assertThat(sut.age()).isEmpty();
    }

    @Test
    void shouldParseConclusions() {
        var given = "timed_out, skipped, failure";
        when(inputs.get(matches("conclusions"))).thenReturn(Optional.of(given));

        var expected = new Conclusion[] { FAILURE, SKIPPED, TIMED_OUT };
        var sut = InputsParser.of(inputs).conclusions();
        assertThat(sut.validConclusions()).containsExactlyInAnyOrder(expected);
    }

    @Test
    void shouldReturnInvalidConclusions() {
        var given = "invalid2, skipped, invalid1";
        when(inputs.get(matches("conclusions"))).thenReturn(Optional.of(given));

        var expected = ("invalid1/invalid2");
        var sut = InputsParser.of(inputs).conclusions();
        assertThat(sut.invalidConclusions("/")).isEqualTo(expected);
    }

    @Test
    void shouldReturnAllConclusions() {
        var given = "all";
        when(inputs.get(matches("conclusions"))).thenReturn(Optional.of(given));

        var validExpected = EnumSet.allOf(Conclusion.class);
        var invalidExpected = "";
        var sut = InputsParser.of(inputs).conclusions();

        var softly = new SoftAssertions();
        softly.assertThat(sut.validConclusions()).isEqualTo(validExpected);
        softly.assertThat(sut.invalidConclusions(",")).isEqualTo(invalidExpected);
        softly.assertAll();
    }

    @Test
    void shouldSkipAllIfNotSingle() {
        var given = "all, success, invalid1";
        when(inputs.get(matches("conclusions"))).thenReturn(Optional.of(given));

        var validExpected = new Conclusion[] { SUCCESS };
        var invalidExpected = "all,invalid1";
        var sut = InputsParser.of(inputs).conclusions();

        var softly = new SoftAssertions();
        softly.assertThat(sut.validConclusions()).containsExactly(validExpected);
        softly.assertThat(sut.invalidConclusions(",")).isEqualTo(invalidExpected);
        softly.assertAll();
    }

    @Test
    void shouldHaveValidConclusions() {
        var given = "success";
        when(inputs.get(matches("conclusions"))).thenReturn(Optional.of(given));

        var sut = InputsParser.of(inputs).conclusions();
        var softly = new SoftAssertions();
        softly.assertThat(sut.hasValidResults()).isTrue();
        softly.assertThat(sut.hasInvalidResults()).isFalse();
        softly.assertAll();
    }

    @Test
    void shouldHaveInvalidConclusions() {
        var given = "invalid0";
        when(inputs.get(matches("conclusions"))).thenReturn(Optional.of(given));

        var sut = InputsParser.of(inputs).conclusions();
        var softly = new SoftAssertions();
        softly.assertThat(sut.hasValidResults()).isFalse();
        softly.assertThat(sut.hasInvalidResults()).isTrue();
        softly.assertAll();
    }
}