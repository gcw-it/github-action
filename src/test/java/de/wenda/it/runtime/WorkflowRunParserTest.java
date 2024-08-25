package de.wenda.it.runtime;

import org.junit.jupiter.api.Test;
import org.kohsuke.github.GHWorkflowRun;

import java.util.List;
import java.util.stream.LongStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class WorkflowRunParserTest {

    @Test
    void shouldSuccessfullyParseAndSortInput() {
        var ids = new long[] { 42, 13, 7 };
        var given = workflowRuns(ids);
        var expected = "7/13/42";

        var sut = WorkflowRunParser.parse(given);
        assertThat(sut.runIdsAsString("/"))
                .isEqualTo(expected);
    }

    private List<GHWorkflowRun> workflowRuns(long[] ids) {
        return LongStream.of(ids)
                .mapToObj(this::mockWorkflowRun)
                .toList();
    }

    private GHWorkflowRun mockWorkflowRun(long id) {
        var workflowRun = mock(GHWorkflowRun.class);
        when(workflowRun.getWorkflowId()).thenReturn(id);
        return workflowRun;
    }
}