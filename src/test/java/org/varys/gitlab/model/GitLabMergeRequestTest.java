package org.varys.gitlab.model;

import org.junit.Test;

import java.util.Collections;
import java.util.Date;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class GitLabMergeRequestTest {

    @Test
    public void getTasks() {
        final GitLabUser userAlain = new GitLabUser(1, "alain");
        final GitLabUser userKevin = new GitLabUser(2, "kevin");

        final GitLabMergeRequest mergeRequest = new GitLabMergeRequest(
                new GitLabMergeRequestListItem(
                        1,
                        1,
                        1,
                        "title",
                        "- [ ] task 1\\r\\n- [x] task 2\\r\\n\\r\\nnot a task\\r\\n- [ ] task 3",
                        GitLabMergeRequestState.OPENED,
                        GitLabMergeStatus.CAN_BE_MERGED,
                        false,
                        new Date(),
                        "target",
                        "source",
                        userAlain,
                        userKevin,
                        0,
                        "http://example.com"
                ),
                new GitLabProject(1, "project", "gitlab/project"),
                Collections.singletonList(
                        new GitLabNote(1, "marked the task **task 2** as complete", new Date(), userAlain)),
                Collections.emptyList()
        );

        assertEquals(3, mergeRequest.getTasks().size());

        assertTrue(mergeRequest.getTasks().stream()
                .anyMatch(t -> t.getDescription().equals("task 1") && !t.isCompleted() && !t.getCompletor().isPresent()));

        assertTrue(mergeRequest.getTasks().stream()
                .anyMatch(t -> t.getDescription().equals("task 2") && t.isCompleted() && t.getCompletor().isPresent()));

        assertTrue(mergeRequest.getTasks().stream()
                .anyMatch(t -> t.getDescription().equals("task 3") && !t.isCompleted() && !t.getCompletor().isPresent()));
    }
}