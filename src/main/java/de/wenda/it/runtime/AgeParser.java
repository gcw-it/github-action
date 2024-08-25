package de.wenda.it.runtime;

import java.time.Period;
import java.time.temporal.TemporalAmount;

public class AgeParser {
    private final Period period;

    public static AgeParser parse(String age) {
        return new AgeParser(age);
    }

    private AgeParser(String age) {
        period = Period.ofDays(Integer.parseInt(age));
    }

    public TemporalAmount days() {
        return period;
    }
}
