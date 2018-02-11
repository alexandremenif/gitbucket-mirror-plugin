# Gitbucket Mirror Plugin

This plugin adds repository mirroring to
[Gitbucket](https://gitbucket.github.io/).

![Mirror List](gitbucket-mirror-plugin_list.png)

![Mirror View](gitbucket-mirror-plugin_view.png)

This plugin allows repository owners to configures mirrors for their
repositories. The mirrors are copies of the repositories that are kept
updated automatically thanks to a *post-receive* hook executing the equivalent
of a `git push --mirror` command. Therefore, every reference (commits, tags
and branches) pushed to a repository will be propagated to its mirrors.

A typical use case occurs when a user or an organization wants to use a
*Gitbucket* instance as the main Git repository hosting service, while keeping
read-only updated copies of the repositories on *Github*, for example to
improve the visibility of the repositories among the open source community.

## Installation

Download the jar file from the 
[release page](https://github.com/alexandremenif/gitbucket-mirror-plugin/releases)
and copy it under the plugins directory of your
[Gitbucket](https://gitbucket.github.io/) instance (`$GITBUCKET_HOME/plugins`).

## Usage

The plugin adds a section entitled *Mirrors* to the side menu of every
repository hosted on a *Gitbucket* instance. Once authenticated, repository
owners and administrators can use this section to add new mirrors and modify
or delete existing ones. It is also possible to update a mirror manually and 
to disable automatic updates.

The plugin will report the status of the last update. If the mirror has diverged
from the original repository, it is likely that the update will fail.
In this case, you should first deal with the divergence before expecting a
successful update. In principle, you should keep the mirror repository
read-only in order to avoid this kind of issues.
 
### Creating a new mirror

As an example, let's consider the creation of a mirror on *Github* for a
repository hosted on *Gitbucket*.

First, you must create the mirror repository on *Github* as a new empty
repository. Let's assume that the new mirror is located at a URL alike
`https://github.com/username/repos.git`.

Then, you can go to the *Mirrors* view of your repository on *Gitbucket* and
click on the *New Mirror* button. Choose a name for the new mirror and enter
its URL in the dedicated input field. If required, credentials should be
included in the URL. For the *Github* example, you should for instance get a
[personal access token](https://help.github.com/articles/creating-a-personal-access-token-for-the-command-line/)
and include it in the URL in the following way: `https://username:token@github.com/username/repos.git`.

Submit the new mirror and every push received by your repository on
*Gitbucket* will now be propagated to the mirror on *Github*.

### SSH support

It is also possible to use the SSH protocol instead of HTTP. Again, you need
to provide the credentials in the URL to perform authentication (it is
possible, although it is not recommended for security reasons).

If you are rather using a public/private key system, you can use the
environment variable `MIRROR_SSH_PRIVATE_KEYS` to specify a semicolon
separated list of locations where *Gitbucket* will be able to find your
private key (e.g. `~/private_key1;~/private_key2`). you should not need to
set this environment variable if your private key is located at a standard
location such as `~/.ssh/id_rsa`. Note that passphrase protected private keys
are not supported. 

You will also need to setup host keys for the SSH connexion to succeed. If
needed, you can use the environment variable `MIRROR_SSH_HOST_KEYS` to
specify a custom location for your host keys. One way to setup host keys is
to use the `ssh-keyscan` tool provided by the *OpenSSH* client:
 
```
ssh-keyscan -t ssh-rsa github.com >> ~/.ssh/known_hosts
```

## Compatibility

Plugin version | GitBucket version
:--------------|:-----------------
1.0.x -        | 4.20.x -

## Release Notes

### 1.0.2

- Add SSH protocol support.
- Improve documentation in README file.

### 1.0.1

- Fix bug about deleted references not being propagated.
- Choose a more appropriate menu icon.
- Improve README file.

### 1.0.0

- Initial release.