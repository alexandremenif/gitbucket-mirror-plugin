package io.github.gitbucket.mirror.util

import java.io.File
import java.net.URI

import gitbucket.core.util.Directory
import org.eclipse.jgit.api.Git
import org.eclipse.jgit.lib.Repository
import org.eclipse.jgit.storage.file.FileRepositoryBuilder

import scala.util.Try
import wrapper._

object GitUtil {

  def repository(owner: String, repositoryName: String): Try[Repository] = Try {

    val repositoryPath = s"${Directory.GitBucketHome}/repositories/$owner/$repositoryName.git"

    new FileRepositoryBuilder()
      .setGitDir(new File(repositoryPath))
      .readEnvironment()
      .findGitDir()
      .build()
  }

  def pushMirror(owner: String, repositoryName: String, remoteUrl: URI): Try[Unit] = {

    for {
      repository <- repository(owner, repositoryName)
      pushMirrorCommand <- new Git(repository).pushMirror()
        .setRemote(remoteUrl.toString)
        .configureTransport(remoteUrl)
      _ <- Try { pushMirrorCommand.call() }
    } yield ()

  }

}
