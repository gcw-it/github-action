package de.wenda.it.runtime;

import org.junit.jupiter.api.Test;
import org.kohsuke.github.GHRepository;
import org.kohsuke.github.GHWorkflowRun;
import org.kohsuke.github.GHWorkflowRun.Conclusion;
import org.mockito.Answers;

import java.io.IOException;
import java.time.Instant;
import java.time.Period;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.function.Function;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.kohsuke.github.GHWorkflowRun.Conclusion.*;
import static org.kohsuke.github.GHWorkflowRun.Status.COMPLETED;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class WorkflowRunResolverTest {
    private static final Function<Integer, Date> DATE_TRANSFORMER =
            i -> Date.from(Instant.now().minus(Period.ofDays(i)));

    @Test
    void shouldReturnWorkflowRunsOlderThan() {
        var given = Period.ofDays(10);
        var ages = List.of(9, 10, 11);
        var sut = WorkflowRunResolver.of(mockRepositoryForAges(ages));

        assertThat(sut.completedRuns(given))
                .hasSize(2);
    }

    @Test
    void shouldReturnWorkflowRunsWithConclusion() {
        var given = List.of(SUCCESS, CANCELLED, SKIPPED);
        var conclusions = List.of(SUCCESS, FAILURE, CANCELLED, SKIPPED);
        var sut = WorkflowRunResolver.of(mockRepositoryForConclusions(conclusions));

        assertThat(sut.completedRuns(given))
                .hasSize(3);
    }

    @Test
    void shouldReturnWorkflowRuns() {
        var givenAge = Period.ofDays(10);
        var givenConclusions = List.of(SUCCESS);

        var ages = List.of(9, 10, 10, 11, 11);
        var conclusions = List.of(SUCCESS, SUCCESS, CANCELLED, SUCCESS, FAILURE);

        var sut = WorkflowRunResolver.of(mockRepository(ages, conclusions));

        assertThat(sut.completedRuns(givenAge, givenConclusions))
                .hasSize(2);
    }

    private GHRepository mockRepositoryForAges(List<Integer> ages) {
        return mockRepository(this::updatedAt, DATE_TRANSFORMER, ages);
    }

    private GHRepository mockRepositoryForConclusions(List<Conclusion> conclusions) {
        return mockRepository(GHWorkflowRun::getConclusion, c -> c, conclusions);
    }

    private <T, U> GHRepository mockRepository(
            Function<GHWorkflowRun, U> ghWorkflowRunMethod,
            Function<T, U> transformer,
            Collection<T> args) {

        try {
            var workflowRuns = new ArrayList<GHWorkflowRun>();
            for (T arg : args) {
                var workflowRun = mock(GHWorkflowRun.class);
                mockWorkflowRun(workflowRun, ghWorkflowRunMethod, transformer, arg);
                workflowRuns.add(workflowRun);
            }

            var repository = mock(GHRepository.class, Answers.RETURNS_DEEP_STUBS);
            when(repository.queryWorkflowRuns().status(COMPLETED).list().toList())
                    .thenReturn(workflowRuns);
            return repository;
        } catch (IOException ex) {
            throw new ActionException(ex);
        }
    }

    private GHRepository mockRepository(List<Integer> ages, List<Conclusion> conclusions) {
        if (ages.size() != conclusions.size()) {
            throw new IllegalArgumentException("Unequal arguments size");
        }

        try {
            var workflowRuns = IntStream.range(0, ages.size())
                    .mapToObj(i -> mockWorkflowRun(ages.get(i), conclusions.get(i)))
                    .toList();

            var repository = mock(GHRepository.class, Answers.RETURNS_DEEP_STUBS);
            when(repository.queryWorkflowRuns().status(COMPLETED).list().toList())
                    .thenReturn(workflowRuns);

            return repository;
        } catch (IOException ex) {
            throw new ActionException(ex);
        }
    }

    private <T, U> void mockWorkflowRun(
            GHWorkflowRun workflowRun,
            Function<GHWorkflowRun, U> ghWorkflowRunMethod,
            Function<T, U> argTransformer,
            T arg) {

            when(ghWorkflowRunMethod.apply(workflowRun))
                    .thenReturn(argTransformer.apply(arg));
    }

    private GHWorkflowRun mockWorkflowRun(Integer age, Conclusion conclusion) {
        var workflowRun = mock(GHWorkflowRun.class);
        mockWorkflowRun(workflowRun, this::updatedAt, DATE_TRANSFORMER, age);
        mockWorkflowRun(workflowRun, GHWorkflowRun::getConclusion, c -> c, conclusion);

        return workflowRun;
    }

    private Date updatedAt(GHWorkflowRun run) {
        try {
            return run.getUpdatedAt();
        } catch (IOException ex) {
            throw new ActionException(ex);
        }
    }
}