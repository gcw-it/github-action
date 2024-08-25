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
import org.kohsuke.github.GHWorkflowRun.Conclusion;

import java.io.IOException;
import java.util.Collection;
import java.util.function.Function;
import java.util.stream.Collectors;

@SuppressWarnings({"unused", "CdiInjectionPointsInspection"})
public class Actions {

    @Action("delete-workflow-runs-by-conclusion")
    void deleteWorkflowsByConclusion(Inputs inputs, GHRepository repository, Commands commands) {
        inputs.get("conclusions")
                .map(ConclusionsParser::parse)
                .ifPresentOrElse(
                        parser -> handleConclusions(parser, repository, commands),
                        () -> reportNoAction(commands));
    }

    @Action("delete-workflow-runs-by-age")
    void deleteWorkflowsByAge(Inputs inputs, GHRepository repository, Commands commands) {
        inputs.get("age")
                .map(AgeParser::parse)
                .map(AgeParser::days)
                .map(age -> WorkflowRunResolver.of(repository).completedRuns(age))
                .ifPresentOrElse(
                        runsToDelete -> deleteWorkflowRuns(runsToDelete, commands),
                        () -> reportNoAction(commands));
    }

    private void handleConclusions(ConclusionsParser parser, GHRepository repository, Commands commands) {
        var validConclusions = parser.validConclusions();
        var runsToDelete = WorkflowRunResolver.of(repository).completedRuns(validConclusions);
        if (parser.hasInvalidConclusions()) {
            commands.appendJobSummary(":warning: Unknown conclusions: "
                    + parser.invalidConclusions(", "));
        }

        Function<Collection<Conclusion>, String> transformer = cs -> cs.stream()
                .map(Object::toString)
                .map(String::toLowerCase)
                .collect(Collectors.joining(", "));
        deleteWorkflowRuns(runsToDelete, commands);
    }

    private <T> void deleteWorkflowRuns(Collection<GHWorkflowRun> workflowRuns, Commands commands) {
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
