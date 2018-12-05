package org.varys.gitlab.model.notification;

import org.varys.common.model.NotificationType;
import org.varys.gitlab.model.GitLabMergeRequest;
import org.varys.gitlab.model.GitLabUser;

public class MergedNotification extends MergeRequestUpdateNotification {

    private final GitLabUser myself;

    MergedNotification(GitLabMergeRequest mergeRequest, GitLabMergeRequest previousVersion, GitLabUser myself) {
        super(mergeRequest, previousVersion);
        this.myself = myself;
    }

    @Override
    public boolean shouldNotify() {
        final GitLabMergeRequest mr = this.getMergeRequest();
        final GitLabMergeRequest previousVersion = this.getPreviousVersion();

        return mr.getAuthor().equals(myself) && mr.isMerged() && !previousVersion.isMerged();
    }

    @Override
    public String getTitle() {
        final String assigneeName = this.getMergeRequest().getAssignee().getName().split(" ")[0];

        return String.format("Merge request on %s has been merged by %s",
                this.getMergeRequest().getProject().getName(), assigneeName);
    }

    @Override
    public NotificationType getType() {
        return NotificationType.INFO;
    }
}
