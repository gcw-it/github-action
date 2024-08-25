package de.wenda.it.runtime;

import org.junit.jupiter.api.Test;
import org.kohsuke.github.GHRepository;
import org.kohsuke.github.GHWorkflowRun;
import org.mockito.Answers;

import java.io.IOException;
import java.time.Instant;
import java.time.Period;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.function.Function;

import static org.assertj.core.api.Assertions.assertThat;
import static org.kohsuke.github.GHWorkflowRun.Conclusion.*;
import static org.kohsuke.github.GHWorkflowRun.Status.COMPLETED;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class WorkflowRunResolverTest {

    @Test
    void shouldReturnWorkflowRunsOlderThan() {
        var given = Period.ofDays(10);
        var ages = List.of(9, 10, 11);
        var sut = WorkflowRunResolver.of(mockRepository(
                this::updatedAt,
                age -> Date.from(Instant.now().minus(Period.ofDays(age))),
                ages));

        assertThat(sut.completedRuns(given))
                .hasSize(2);
    }

    @Test
    void shouldReturnWorkflowRunsWithConclusion() {
        var given = List.of(SUCCESS, CANCELLED, SKIPPED);
        var conclusions = List.of(SUCCESS, FAILURE, CANCELLED, SKIPPED);
        var sut = WorkflowRunResolver.of(mockRepository(
                GHWorkflowRun::getConclusion,
                Function.identity(),
                conclusions));

        assertThat(sut.completedRuns(given))
                .hasSize(3);
    }

    private <T, U> GHRepository mockRepository(
            Function<GHWorkflowRun, U> ghWorkflowRunMethod,
            Function<T, U> transformer,
            Collection<T> args) {

        try {
            var repository = mock(GHRepository.class, Answers.RETURNS_DEEP_STUBS);
            var workflowRuns = mockWorkflowRun(ghWorkflowRunMethod, transformer, args);
            when(repository.queryWorkflowRuns().status(COMPLETED).list().toList())
                    .thenReturn(workflowRuns);
            return repository;
        } catch (IOException ex) {
            throw new ActionException(ex);
        }
    }

    private <T, U> List<GHWorkflowRun> mockWorkflowRun(
            Function<GHWorkflowRun, U> ghWorkflowRunMethod,
            Function<T, U> transformer,
            Collection<T> args) {
        var workflowRuns = new ArrayList<GHWorkflowRun>();
        for (T arg : args) {
            var workflowRun = mock(GHWorkflowRun.class);
            when(ghWorkflowRunMethod.apply(workflowRun))
                    .thenReturn(transformer.apply(arg));
            workflowRuns.add(workflowRun);
        }
        return workflowRuns;
    }

    private Date updatedAt(GHWorkflowRun run) {
        try {
            return run.getUpdatedAt();
        } catch (IOException ex) {
            throw new ActionException(ex);
        }
    }
}