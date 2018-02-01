package io.github.gitbucket.mirror.util

import java.io.File
import java.net.{URL, URLDecoder}

import gitbucket.core.util.Directory
import org.eclipse.jgit.api.{Git, PushCommand}
import org.eclipse.jgit.lib.Repository
import org.eclipse.jgit.storage.file.FileRepositoryBuilder
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider

import scala.collection.JavaConverters._
import scala.util.Try

object GitUtil {

  def repository(owner: String, repositoryName: String): Try[Repository] = Try {

    val repositoryPath = s"${Directory.GitBucketHome}/repositories/$owner/$repositoryName.git"

    new FileRepositoryBuilder()
      .setGitDir(new File(repositoryPath))
      .readEnvironment()
      .findGitDir()
      .build()
  }

  def pushMirror(owner: String, repositoryName: String, remoteUrl: URL): Try[Unit] = {

    repository(owner, repositoryName) flatMap { repository =>

      Try {

        // Extract credentials from the URL.

        val userInfo = Option(remoteUrl.getUserInfo).getOrElse("").split(":")
        val username = URLDecoder.decode(if (userInfo.nonEmpty) userInfo(0) else "", "UTF-8")
        val password = URLDecoder.decode(if (userInfo.size > 1) userInfo(1) else "", "UTF-8")

        val credentialsProvider = new UsernamePasswordCredentialsProvider(username, password)

        // Build a command equivalent to "git push --mirror".

        val git = new Git(repository)

        val pushCommand: PushCommand = git
          .push()
          .setRemote(remoteUrl.toString)
          .setForce(true)
          .add("+refs/*:refs/*")
          .setCredentialsProvider(credentialsProvider)

        // The command built so far does not propagate local deleted references. We have to add them explicitly.

        val lsRemoteCommand = git.lsRemote()
          .setRemote(remoteUrl.toString)
          .setHeads(true)
          .setTags(true)

        lsRemoteCommand.call()
          .asScala
          .withFilter { ref => repository.findRef(ref.getName) == null }
          .foreach { ref => pushCommand.add(s":${ref.getName}") }

        pushCommand.call()
      }
    }
  }
}
