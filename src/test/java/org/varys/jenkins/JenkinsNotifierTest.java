/*
 * This file is part of Varys.
 *
 * Foobar is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Foobar is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Varys.  If not, see <https://www.gnu.org/licenses/>.
 */

package org.varys.jenkins;

import org.junit.Before;
import org.junit.Test;
import org.varys.common.model.RestApiStatus;
import org.varys.common.service.CacheService;
import org.varys.common.service.NotificationService;
import org.varys.git.service.GitService;
import org.varys.jenkins.api.JenkinsApi;
import org.varys.jenkins.model.JenkinsBuild;
import org.varys.jenkins.model.JenkinsBuildListItem;
import org.varys.jenkins.model.JenkinsBuildNotifierNotificationsFiltersConfig;
import org.varys.jenkins.model.JenkinsBuildResult;
import org.varys.jenkins.model.JenkinsNode;
import org.varys.jenkins.model.JenkinsNodeListItem;
import org.varys.jenkins.model.JenkinsNotifierConfig;
import org.varys.jenkins.model.JenkinsNotifierNotificationsConfig;

import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

public class JenkinsNotifierTest {

    private final CacheService cacheService;

    public JenkinsNotifierTest() {
        this.cacheService = new CacheService("JenkinsNotifierTest");
    }

    @Before
    public void before() {
        this.cacheService.clear();
    }

    private static JenkinsBuild build(String nodeId, long id, JenkinsBuildResult result) {
        return new JenkinsBuild(
                Collections.emptyList(),
                String.format("node '%s' build %d: %s", nodeId, id, result),
                1000 * id,
                String.valueOf(id),
                id,
                result,
                "http://example.com/" + nodeId + "/" + id
        );
    }

    @Test
    public void iterate() {
        final JenkinsApi jenkinsApi = mock(JenkinsApi.class);

        when(jenkinsApi.getStatus()).thenReturn(new RestApiStatus(true, true, true, true));
        when(jenkinsApi.getBaseUrl()).thenReturn("http://example.com");

        when(jenkinsApi.getRootNode())
                .thenReturn(Optional.of(new JenkinsNode(
                        "1",
                        "root node",
                        Arrays.asList(
                                new JenkinsNodeListItem("2", "http://example.com/2"),
                                new JenkinsNodeListItem("3", "http://example.com/3"),
                                new JenkinsNodeListItem("3", "http://example.com/4")),
                        Arrays.asList(
                                new JenkinsBuildListItem(1, "http://example.com/1/1"),
                                new JenkinsBuildListItem(2, "http://example.com/1/2"),
                                new JenkinsBuildListItem(3, "http://example.com/1/3")
                        )
                )));
        when(jenkinsApi.getBuild("http://example.com/1/1/api/json"))
                .thenReturn(Optional.of(build("1", 1, JenkinsBuildResult.SUCCESS)));
        when(jenkinsApi.getBuild("http://example.com/1/2/api/json"))
                .thenReturn(Optional.of(build("2", 2, JenkinsBuildResult.FAILURE)));
        final JenkinsBuild rootNodeFailedLastBuild = build("3", 3, JenkinsBuildResult.FAILURE);
        when(jenkinsApi.getBuild("http://example.com/1/3/api/json"))
                .thenReturn(Optional.of(rootNodeFailedLastBuild));


        when(jenkinsApi.getNode("http://example.com/2/api/json"))
                .thenReturn(Optional.of(new JenkinsNode(
                        "2",
                        "2",
                        Collections.emptyList(),
                        Arrays.asList(
                                new JenkinsBuildListItem(1, "http://example.com/2/1"),
                                new JenkinsBuildListItem(2, "http://example.com/2/2")
                        )
                )));
        when(jenkinsApi.getBuild("http://example.com/2/1/api/json"))
                .thenReturn(Optional.of(build("2", 1, JenkinsBuildResult.SUCCESS)));
        when(jenkinsApi.getBuild("http://example.com/2/2/api/json"))
                .thenReturn(Optional.of(build("2", 2, JenkinsBuildResult.NONE)));


        when(jenkinsApi.getNode("http://example.com/3/api/json"))
                .thenReturn(Optional.of(new JenkinsNode(
                        "3",
                        "3",
                        Collections.emptyList(),
                        Arrays.asList(
                                new JenkinsBuildListItem(1, "http://example.com/3/1"),
                                new JenkinsBuildListItem(2, "http://example.com/3/2")
                        )
                )));
        final JenkinsBuild node3FailedFirstBuild = build("3", 1, JenkinsBuildResult.FAILURE);
        when(jenkinsApi.getBuild("http://example.com/3/1/api/json"))
                .thenReturn(Optional.of(node3FailedFirstBuild));
        when(jenkinsApi.getBuild("http://example.com/3/2/api/json"))
                .thenReturn(Optional.of(build("3", 2, JenkinsBuildResult.NONE)));


        when(jenkinsApi.getNode("http://example.com/4/api/json"))
                .thenReturn(Optional.of(new JenkinsNode(
                        "4",
                        "4",
                        Collections.emptyList(),
                        Arrays.asList(
                                new JenkinsBuildListItem(1, "http://example.com/4/1"),
                                new JenkinsBuildListItem(2, "http://example.com/4/2")
                        )
                )));
        when(jenkinsApi.getBuild("http://example.com/4/1/api/json"))
                .thenReturn(Optional.of(build("4", 1, JenkinsBuildResult.SUCCESS)));
        final JenkinsBuild node4FailedLastBuild = build("4", 2, JenkinsBuildResult.FAILURE);
        this.cacheService.save("4/2", node4FailedLastBuild);
        when(jenkinsApi.getBuild("http://example.com/4/2/api/json"))
                .thenReturn(Optional.of(node4FailedLastBuild));

        final GitService gitService = mock(GitService.class);
        when(gitService.hasLocalBranch(anyString())).thenReturn(false);

        final NotificationService notificationService = mock(NotificationService.class);

        final JenkinsNotifier notifier = new JenkinsNotifier(
                jenkinsApi,
                new JenkinsNotifierConfig(new JenkinsNotifierNotificationsConfig(
                        30, new JenkinsBuildNotifierNotificationsFiltersConfig(false, false)
                )),
                gitService,
                this.cacheService,
                notificationService
        );

        notifier.iterate();

        verify(notificationService, times(2)).send(any());
        verify(notificationService, times(1)).send(rootNodeFailedLastBuild);
        verify(notificationService,  times(1)).send(node3FailedFirstBuild);
    }
}