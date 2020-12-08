package io.github.gitbucket.mirror.hook

import gitbucket.core.controller.Context
import gitbucket.core.model.Issue
import gitbucket.core.plugin.PullRequestHook
import gitbucket.core.service.RepositoryService.RepositoryInfo
import io.github.gitbucket.mirror.service.MirrorService
import gitbucket.core.model.Profile.profile.blockingApi._

class MirrorPullRequestHook extends PullRequestHook with MirrorService {

  override def merged(issue: Issue, repository: RepositoryInfo)(implicit session: Session, context: Context): Unit =
    findMirrorsByRepository(repository.owner, repository.name).foreach { mirror =>
      if (mirror.enabled) executeMirrorUpdate(mirror)
    }

}
