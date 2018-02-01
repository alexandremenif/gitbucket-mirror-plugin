# Gitbucket Mirror Plugin

This plugin adds repository mirroring to
[Gitbucket](https://gitbucket.github.io/).

![Mirror List](gitbucket-mirror-plugin_list.png)

![Mirror View](gitbucket-mirror-plugin_view.png)

This plugin allows repository owners to configures mirrors for their
repositories. The mirrors are copies of the repositories that are kept
updated automatically thanks to a post-receive hook. Therefore, every
reference (commits, tags and branches) pushed to a repository will be
propagated to its mirrors.

A typical use case occurs when a user or an organization wants to use a
Gitbucket instance as its main Git repository hosting service, while keeping
read-only updated copies of his repositories on Github, for example to improve
the visibility of his repositories among the open source community.

## Installation

Download the jar file from the 
[release page](https://github.com/alexandremenif/gitbucket-mirror-plugin/releases)
and copy it under the plugins directory of your
[Gitbucket](https://gitbucket.github.io/) instance (`$GITBUCKET_HOME/plugins`).

## Usage

When authenticated into your Gitbucket instance, you have access to a *Mirrors*
view for each of your repositories. This view lets you create new mirrors and
modify or delete existing ones. You can also update a mirror manually or disable
automatic updates.

For each mirror, you need to specify its remote URL. So far, only the *http* and
*https* protocols are supported. If the repository requires authentication, you
should provide them in the URL (for example: 
https://username:password@example.com/repo.git).

Credentials are currently stored in plain text into the database, so you should
rather use an authentication token like the ones provided by *Github*.

The plugin will report the status of the last update. If the mirror has diverged
from the original repository, it is likely that the update will fail.
In this case, you should first deal with the divergence before expecting a
successful update.

## Compatibility

Plugin version | GitBucket version
:--------------|:-----------------
1.0.x -        | 4.20.x -

## Release Notes

### 1.0.1

- Fix bug about deleted references not being propagated.
- Choose a more appropriate menu icon.
- Improve README file.

### 1.0.0

- Initial release.