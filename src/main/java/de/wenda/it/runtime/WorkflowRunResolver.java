package de.wenda.it.runtime;

import org.kohsuke.github.GHRepository;
import org.kohsuke.github.GHWorkflowRun;
import org.kohsuke.github.GHWorkflowRun.Conclusion;

import java.io.IOException;
import java.time.Instant;
import java.time.temporal.TemporalAmount;
import java.util.Collection;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static org.kohsuke.github.GHWorkflowRun.Status.COMPLETED;

public class WorkflowRunResolver {
    private final Collection<GHWorkflowRun> completedWorkflowRuns;

    public static WorkflowRunResolver of(GHRepository repository) {
        try {
            var workflowRuns = repository.queryWorkflowRuns().status(COMPLETED).list().toList();
            return new WorkflowRunResolver(workflowRuns);
        } catch (IOException ex) {
            throw new ActionException(ex);
        }
    }

    private WorkflowRunResolver(Collection<GHWorkflowRun> workflowRuns) {
        completedWorkflowRuns = workflowRuns;
    }

    public Collection<GHWorkflowRun> completedRuns(TemporalAmount age) {
        return completedRuns(run -> isBefore(age, run));
    }

    public Collection<GHWorkflowRun> completedRuns(Collection<Conclusion> conclusions) {
        return completedRuns(run -> conclusions.contains(run.getConclusion()));
    }

    public Collection<GHWorkflowRun> completedRuns(TemporalAmount age, Collection<Conclusion> conclusions) {
        return completedRuns(run -> isBefore(age, run) && conclusions.contains(run.getConclusion()));
    }

    private Collection<GHWorkflowRun> completedRuns(Predicate<GHWorkflowRun> predicate) {
        return completedWorkflowRuns.stream()
                .filter(predicate)
                .collect(Collectors.toSet());
    }

    private boolean isBefore(TemporalAmount days, GHWorkflowRun run) {
        try {
            return run.getUpdatedAt().toInstant().isBefore(Instant.now().minus(days));
        } catch (IOException ex) {
            throw new ActionException(ex);
        }
    }
}
