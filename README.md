# Varys
[![Build Status](https://travis-ci.org/mbouchenoire/varys.svg?branch=master)](https://travis-ci.org/mbouchenoire/varys)&nbsp;
[![Coverage Status](https://coveralls.io/repos/github/mbouchenoire/varys/badge.svg?branch=master)](https://coveralls.io/github/mbouchenoire/varys?branch=master)

**Varys** is a local process that crawls trough your dev environment (GitLab, Jenkins...) giving you instant feedback
(via desktop notifications) on things that you care about. This (currently) includes:
- new merge requests (assigned to you, or other people),
- updates on those merge requests:
  - status change (merged, closed...),
  - assignee change,
  - new comments,
  - new commits.
- build statuses of your local Git branches (configurable).



## Build & Run from sources

### Linux
```console
root@localhost:~/git$ git clone https://github.com/mbouchenoire/varys.git
root@localhost:~/git$ cd varys
root@localhost:~/git/varys$ ./mvnw clean package
root@localhost:~/git/varys$ vi src/main/resources/config.json  # see the Configuration section
root@localhost:~/git/varys$ java -cp target/varys-<version>.jar org.varys.App src/main/resources/config.json
```

### Windows
```console
C:\Users\user\git> git clone https://github.com/mbouchenoire/varys.git
C:\Users\user\git> cd varys
C:\Users\user\git\varys> mvnw clean package
C:\Users\user\git\varys> notepad src\main\resources\config.json  # see the Configuration section
C:\Users\user\git\varys> java -cp target/varys-<version>.jar org.varys.App src\main\resources\config.json
```

## Configuration
The configuration file is currently located here (temporary):
`src/main/resources/config.json`.

### Configuration values:

#### Common
- `git.parent_directory`: The directory in which you put all your git projects.
Varys will search for `.git` directories as deep as 3 levels below.

#### Jenkins module (`modules[name=jenkins].config`)
- `jenkins_api.api_token`: You can obtain your Jenkins API token using ;
 [this procedure](https://stackoverflow.com/questions/45466090/how-to-get-the-api-token-for-jenkins) ;
- `notifications.period`: The time (in seconds) between each notification process ;
- `notifications.filters.local_branches_only`: `true` if you want to receive notifications only
 for builds regarding your local Git branches ;
- `notifications.filters.sucessful_builds`: `false` if you don't want to receive notifications
for successful builds.

#### GitLab module (`modules[name=gitlab].config`)
- `gitlab_api.version`: `4` since GitLab version 9.0, `3` before that ;
- `gitlab_api.private_token`: You can obtain your GitLab personal access token using [this procedure](https://docs.gitlab.com/ee/user/profile/personal_access_tokens.html) ;
- `notifications.period`: The time (in seconds) between each notification process ;
- `notifications.filters.assigned_to_me_only`: `true` if you want to receive notifications only
 for merge requests assigned to yourself.