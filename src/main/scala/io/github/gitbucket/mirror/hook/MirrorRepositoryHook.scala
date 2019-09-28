package io.github.gitbucket.mirror.hook

import gitbucket.core.plugin.RepositoryHook
import io.github.gitbucket.mirror.model.Profile._
import io.github.gitbucket.mirror.service.MirrorService
import profile.blockingApi._

class MirrorRepositoryHook extends RepositoryHook with MirrorService {

  override def deleted(owner: String, repository: String)(implicit session: Session): Unit =
    deleteMirrorsByRepository(owner, repository)

  override def renamed(owner: String, repository: String, newRepository: String)(implicit session: Session): Unit =
    updateMirrorRepositoryName(owner, repository, newRepository)
}