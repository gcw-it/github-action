package de.wenda.it.runtime;

import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.Test;
import org.kohsuke.github.GHWorkflowRun.Conclusion;

import static org.assertj.core.api.Assertions.assertThat;
import static org.kohsuke.github.GHWorkflowRun.Conclusion.*;


class ConclusionsParserTest {

    @Test
    void shouldParseInputWithoutInvalidConclusions() {
        var given = "timed_out, skipped, failure";
        var expected = new Conclusion[] { FAILURE, SKIPPED, TIMED_OUT };
        var sut = ConclusionsParser.parse(given);

        assertThat(sut.validConclusions()).containsExactlyInAnyOrder(expected);
    }

    @Test
    void shouldContainInvalidConclusions() {
        var given = "__invalid__";
        var sut = ConclusionsParser.parse(given);

        assertThat(sut.hasInvalidConclusions()).isTrue();
    }

    @Test
    void shouldReturnInvalidConclusions() {
        var given = "__invalid2__, success, __invalid__";
        var expected = "__invalid2__/__invalid__";
        var sut = ConclusionsParser.parse(given);

        assertThat(sut.invalidConclusions("/"))
                .isEqualTo(expected);
    }

    @Test
    void shouldParseConclusionsCorrectly() {
        var given = "__invalid2__, success, __invalid__, cancelled, skipped";
        var expectedValid = new Conclusion[] { CANCELLED, SKIPPED, SUCCESS };
        var expectedInvalid = "__invalid2__/__invalid__";
        var sut = ConclusionsParser.parse(given);

        var softly = new SoftAssertions();
        softly.assertThat(sut.validConclusions())
                        .containsExactlyInAnyOrder(expectedValid);
        softly.assertThat(sut.invalidConclusions("/"))
                        .isEqualTo(expectedInvalid);
        softly.assertAll();
    }
}