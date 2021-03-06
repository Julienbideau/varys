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

package org.varys.gitlab.api;

import org.pmw.tinylog.Logger;
import org.varys.common.model.RestApiStatus;
import org.varys.common.model.exception.BadPrivateTokenConfigurationException;
import org.varys.common.model.exception.BadSslConfigurationException;
import org.varys.common.model.exception.ConfigurationException;
import org.varys.common.model.exception.UnreachableApiConfigurationException;
import org.varys.gitlab.model.UnsupportedGitLabApiVersionException;

public class GitLabApiLocator {

    public GitLabApi findUsable(GitLabApiV3 gitLabApiV3, GitLabApiV4 gitLabApiV4) throws ConfigurationException {
        final RestApiStatus v3Status = gitLabApiV3.getStatus();
        final RestApiStatus v4Status = gitLabApiV4.getStatus();

        if (!v3Status.isOnline() && !v4Status.isOnline()) {
            throw new UnreachableApiConfigurationException(gitLabApiV3);
        }

        if (!v3Status.isValidPrivateToken() && !v4Status.isValidPrivateToken()) {
            throw new BadPrivateTokenConfigurationException(gitLabApiV3);
        }

        if (!v3Status.isValidSslCertificate() && !v4Status.isValidSslCertificate()) {
            throw new BadSslConfigurationException(gitLabApiV3);
        }

        if (v3Status.isCompatible()) {
            Logger.info("Using compatible GitLab API v3");
            return gitLabApiV3;
        } else if (v4Status.isCompatible()) {
            Logger.info("GitLab API v3 is not compatible, using compatible API v4");
            return gitLabApiV4;
        } else {
            throw new UnsupportedGitLabApiVersionException();
        }
    }
}
