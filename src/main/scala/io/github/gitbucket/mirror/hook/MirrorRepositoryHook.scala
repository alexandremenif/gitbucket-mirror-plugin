package io.github.gitbucket.mirror.hook

import gitbucket.core.plugin.RepositoryHook
import io.github.gitbucket.mirror.model.Profile._
import io.github.gitbucket.mirror.service.MirrorService
import profile.blockingApi._

import scala.concurrent.duration._
import scala.concurrent.Await

class MirrorRepositoryHook extends RepositoryHook with MirrorService {

  override def deleted(owner: String, repository: String)(implicit session: Session): Unit = {
    Await.result(deleteMirrorByRepository(owner, repository), 60 seconds)
  }

  override def renamed(owner: String, repository: String, newRepository: String)(implicit session: Session): Unit = {
    Await.result(renameMirrorRepository(owner, repository, newRepository), 60 seconds)
  }

}