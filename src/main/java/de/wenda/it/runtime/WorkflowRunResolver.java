package de.wenda.it.runtime;

import org.kohsuke.github.GHRepository;
import org.kohsuke.github.GHWorkflowRun;
import org.kohsuke.github.GHWorkflowRun.Conclusion;

import java.io.IOException;
import java.time.Instant;
import java.time.temporal.TemporalAmount;
import java.util.Collection;
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

    private WorkflowRunResolver(Collection<GHWorkflowRun> completedWorkflowRuns) {
        this.completedWorkflowRuns = completedWorkflowRuns;
    }

    public Collection<GHWorkflowRun> completedRuns(Collection<Conclusion> conclusions) {
        return completedWorkflowRuns.stream()
                .filter(run -> conclusions.contains(run.getConclusion()))
                .collect(Collectors.toSet());
    }

    public Collection<GHWorkflowRun> completedRuns(TemporalAmount age) {
        return completedWorkflowRuns.stream()
                .filter(run -> isBefore(age, run))
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
