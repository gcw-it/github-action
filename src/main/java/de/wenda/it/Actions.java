package de.wenda.it;

import de.wenda.it.runtime.ActionException;
import de.wenda.it.runtime.ConclusionsParser;
import de.wenda.it.runtime.AgeParser;
import de.wenda.it.runtime.WorkflowRunParser;
import de.wenda.it.runtime.WorkflowRunResolver;
import io.quarkiverse.githubaction.Action;
import io.quarkiverse.githubaction.Commands;
import io.quarkiverse.githubaction.Inputs;
import org.kohsuke.github.GHRepository;
import org.kohsuke.github.GHWorkflowRun;

import java.io.IOException;
import java.util.Collection;

@SuppressWarnings({"unused", "CdiInjectionPointsInspection"})
public class Actions {

    @Action("delete-workflow-runs-by-conclusion")
    void deleteWorkflowsByConclusion(Inputs inputs, GHRepository repository, Commands commands) {
        var parser = ConclusionsParser.parse(inputs.getRequired("conclusions"));
        var runsToDelete = WorkflowRunResolver.of(repository).completedRuns(parser.validConclusions());

        if (parser.hasInvalidConclusions()) {
            commands.appendJobSummary(":warning: Unknown conclusions: "
                    + parser.invalidConclusions(", "));
        }
        deleteWorkflowRuns(runsToDelete, commands);
    }

    @Action("delete-workflow-runs-by-age")
    void deleteWorkflowsByAge(Inputs inputs, GHRepository repository, Commands commands) {
        var age = AgeParser.parse(inputs.getRequired("age")).days();
        var runsToDelete = WorkflowRunResolver.of(repository).completedRuns(age);

        deleteWorkflowRuns(runsToDelete, commands);
    }

    private void deleteWorkflowRuns(Collection<GHWorkflowRun> workflowRuns, Commands commands) {
        if (!workflowRuns.isEmpty()) {
            workflowRuns.forEach(run -> deleteWorkflowRun(run, commands));
            commands.appendJobSummary(":white_check_mark: Deleted workflow runs: "
                    + WorkflowRunParser.parse(workflowRuns).runIdsAsString(", "));
        } else {
            commands.appendJobSummary(":warning: There weren't any applicable conclusions, no workflow runs deleted");
        }
    }

    private void deleteWorkflowRun(GHWorkflowRun workflowRun, Commands commands) {
        try {
            workflowRun.delete();
            commands.notice("Deleted workflow run " + workflowRun.getDisplayTitle());
        } catch (IOException ex) {
            throw new ActionException(ex);
        }
    }
}
