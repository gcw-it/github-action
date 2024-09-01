package de.wenda.it.runtime;

import io.quarkiverse.githubaction.Inputs;
import org.kohsuke.github.GHWorkflowRun.Conclusion;

import java.time.Period;
import java.time.temporal.TemporalAmount;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import static java.util.function.Predicate.not;

public class InputsParser {
    private final Inputs inputs;

    public static InputsParser of(Inputs inputs) {
        return new InputsParser(inputs);
    }

    private InputsParser(Inputs inputs) {
        this.inputs = inputs;
    }

    public Optional<TemporalAmount> age() {
        return inputs.get("age")
                .flatMap(this::parseInt)
                .map(Period::ofDays);
    }

    public ConclusionsResult conclusions() {
        return ConclusionsResult.of(inputs.get("conclusions").stream()
                .map(s -> s.split(","))
                .flatMap(Arrays::stream)
                .map(String::trim)
                .collect(Collectors.toMap(Function.identity(), Conclusion::from)));
    }

    private Optional<Integer> parseInt(String integer) {
        try {
            return Optional.of(Integer.parseInt(integer));
        } catch (NumberFormatException ignored) {
            return Optional.empty();
        }
    }

    public static final class ConclusionsResult {
        private final Set<Conclusion> validConclusions;
        private final Set<String> invalidConclusions;

        private static ConclusionsResult of(Map<String,Conclusion> conclusionMap) {
            return new ConclusionsResult(conclusionMap);
        }

        private ConclusionsResult(Map<String,Conclusion> conclusionMap) {
            if (conclusionMap.size() == 1 && conclusionMap.containsKey("all")) {
                validConclusions = EnumSet.allOf(Conclusion.class);
                invalidConclusions = Collections.emptySet();
            } else {
                validConclusions = conclusionMap.values().stream()
                        .filter(not(Conclusion.UNKNOWN::equals))
                        .collect(Collectors.toCollection(() ->EnumSet.noneOf(Conclusion.class)));

                invalidConclusions = conclusionMap.entrySet().stream()
                        .filter(entry -> entry.getValue().equals(Conclusion.UNKNOWN))
                        .map(Map.Entry::getKey)
                        .collect(Collectors.toSet());

            }
        }

        public Collection<Conclusion> validConclusions() {
            return Set.copyOf(validConclusions);
        }

        public String invalidConclusions(String delimiter) {
            return invalidConclusions.stream()
                    .sorted()
                    .collect(Collectors.joining(delimiter));
        }

        public boolean hasValidResults() {
            return !validConclusions.isEmpty();
        }

        public boolean hasInvalidResults() {
            return !invalidConclusions.isEmpty();
        }
    }
}
