package de.wenda.it.runtime;

import org.kohsuke.github.GHWorkflowRun;

import java.util.Collection;
import java.util.stream.Collectors;

public class WorkflowRunParser {
    private final Collection<GHWorkflowRun> workflowRuns;

    public static WorkflowRunParser parse(Collection<GHWorkflowRun> workflowRuns) {
        return new WorkflowRunParser(workflowRuns);
    }

    private WorkflowRunParser(Collection<GHWorkflowRun> workflowRuns) {
        this.workflowRuns = workflowRuns;
    }

    public String runIdsAsString(String delimiter) {
        return workflowRuns.stream()
                .map(GHWorkflowRun::getWorkflowId)
                .sorted(Long::compare)
                .map(Object::toString)
                .collect(Collectors.joining(delimiter));
    }
}
