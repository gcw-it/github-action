package de.wenda.it;

import de.wenda.it.runtime.ActionException;
import de.wenda.it.runtime.InputsParser;
import de.wenda.it.runtime.WorkflowRunParser;
import de.wenda.it.runtime.WorkflowRunResolver;
import io.quarkiverse.githubaction.Action;
import io.quarkiverse.githubaction.Commands;
import io.quarkiverse.githubaction.Inputs;
import org.kohsuke.github.GHRepository;
import org.kohsuke.github.GHWorkflowRun;

import java.io.IOException;
import java.util.Collection;
import java.util.Collections;

@SuppressWarnings({"unused", "CdiInjectionPointsInspection"})
public class WorkflowRunActions {

    @Action("delete-workflow-runs-by-age")
    void deleteWorkflowRunsByAge(Inputs inputs, GHRepository repository, Commands commands) {
        InputsParser.of(inputs).age()
                .map(age -> WorkflowRunResolver.of(repository).completedRuns(age))
                .ifPresentOrElse(
                        runsToDelete -> deleteWorkflowRuns(runsToDelete, commands),
                        () -> reportNoAction(commands));
    }

    @Action("delete-workflow-runs-by-conclusion")
    void deleteWorkflowRunsByConclusion(Inputs inputs, GHRepository repository, Commands commands) {
        var result = InputsParser.of(inputs).conclusions();
        if (!result.hasValidResults()) {
            reportNoAction(commands);
        }
        if (result.hasInvalidResults()) {
            commands.appendJobSummary(":warning: Unknown conclusions: "
                    + result.invalidConclusions(", "));
        }

        var runsToDelete = WorkflowRunResolver.of(repository).completedRuns(result.validConclusions());
        if (runsToDelete.isEmpty()) {
            reportNoAction(commands);
        } else {
            deleteWorkflowRuns(runsToDelete, commands);
        }
    }

    @Action("delete-workflow-runs-by-age-and-conclusion")
    void deleteWorkflowRunsByAgeAndConclusion(Inputs inputs, GHRepository repository, Commands commands) {
        var parser = InputsParser.of(inputs);
        var age = parser.age();
        var conclusions = parser.conclusions();

        if (conclusions.hasInvalidResults()) {
            commands.appendJobSummary(":warning: Unknown conclusions: "
                    + conclusions.invalidConclusions(", "));
        }

        var resolver = WorkflowRunResolver.of(repository);
        Collection<GHWorkflowRun> runsToDelete;
        if(age.isPresent() && conclusions.hasValidResults()) {
            runsToDelete = resolver.completedRuns(age.get(), conclusions.validConclusions());
        } else if (age.isEmpty() && !conclusions.hasValidResults()) {
            runsToDelete = Collections.emptyList();
            reportNoAction(commands);
        } else if (age.isPresent()) {
            runsToDelete = resolver.completedRuns(age.get());
        } else {
            runsToDelete = resolver.completedRuns(conclusions.validConclusions());
        }
        deleteWorkflowRuns(runsToDelete, commands);
    }

    private void deleteWorkflowRuns(Collection<GHWorkflowRun> workflowRuns, Commands commands) {
        if (!workflowRuns.isEmpty()) {
            workflowRuns.forEach(run -> deleteWorkflowRun(run, commands));
            commands.appendJobSummary(":white_check_mark: Deleted workflow runs: "
                    + WorkflowRunParser.parse(workflowRuns).runIdsAsString(", "));
        } else {
            reportNoAction(commands);
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

    private void reportNoAction(Commands commands) {
        var msg = ":warning: Nothing deleted. There weren't any applicable workflow runs for your inputs.";
        commands.appendJobSummary(msg);
    }
}
