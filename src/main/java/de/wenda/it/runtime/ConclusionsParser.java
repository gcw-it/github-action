package de.wenda.it.runtime;

import org.kohsuke.github.GHWorkflowRun.Conclusion;

import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Set;

public class ConclusionsParser {
    private final Set<Conclusion> validConclusions;
    private final Set<String> invalidConclusions;

    public static ConclusionsParser parse(String conclusions) {
        return new ConclusionsParser(conclusions);
    }

    private ConclusionsParser(String conclusions) {
        validConclusions = EnumSet.noneOf(Conclusion.class);
        invalidConclusions = new HashSet<>();

        var tokens = Arrays.stream(conclusions.split(","))
                .map(String::trim)
                .toList();

        for (var token : tokens) {
            var conclusion = Conclusion.from(token);
            if (conclusion.equals(Conclusion.UNKNOWN)) {
                invalidConclusions.add(token);
            } else {
                validConclusions.add(conclusion);
            }
        }
    }

    public Set<Conclusion> validConclusions() {
        return Collections.unmodifiableSet(validConclusions);
    }

    public boolean hasInvalidConclusions() {
        return !invalidConclusions.isEmpty();
    }

    public String invalidConclusions(String delimiter) {
        return String.join(delimiter, invalidConclusions);
    }
}
