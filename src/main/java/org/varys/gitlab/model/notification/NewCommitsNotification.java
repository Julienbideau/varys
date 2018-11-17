package org.varys.gitlab.model.notification;

import org.varys.common.model.NotificationType;
import org.varys.gitlab.model.GitLabMergeRequest;

class NewCommitsNotification extends MergeRequestUpdateNotification {

    NewCommitsNotification(GitLabMergeRequest mergeRequest, GitLabMergeRequest previousVersion) {
        super(mergeRequest, previousVersion);
    }

    @Override
    public boolean shouldNotify() {
        return this.getMergeRequest().addedCommitsCount(this.getPreviousVersion()) > 0;
    }

    @Override
    public String getTitle() {
        final long addedCommitsCount = this.getMergeRequest().addedCommitsCount(this.getPreviousVersion());
        final String s = addedCommitsCount > 1 ? "s" : "";
        return String.format("%d new commit%s on merge request\n%s",
                addedCommitsCount, s, this.getMergeRequest().getTitle());
    }

    @Override
    public NotificationType getType() {
        return NotificationType.INFO;
    }
}