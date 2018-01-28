# Gitbucket Mirror Plugin

This plugin adds repository mirroring to Gitbucket.

It enables users to configure several mirrors to remote repositories. If
enabled, each mirror will automatically update a remote repository for every
push or every reference updates received by the repository hosted on the
Gitbucket instance. The update will include all references, which means that
it will also include branches and tags in addition to commits.

For example, users can work on repositories hosted on a Gitbucket instance
while keeping repositories hosted on another instance or provider (like
Github) synchronized.

![Mirror List](gitbucket-mirror-plugin_list.png)

![Mirror View](gitbucket-mirror-plugin_view.png)

## Installation

Download the jar file from the 
[release page](https://github.com/alexandremenif/gitbucket-mirror-plugin/releases)
and place it under the *plugins* repository located in your
*GITBUCKET_HOME*.

## Compatibility

Plugin version | GitBucket version
:--------------|:-----------------
1.0.x          | 4.20.x

## Release Notes

### 1.0.0

Initial release.