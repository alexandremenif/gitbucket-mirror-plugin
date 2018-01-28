package io.github.gitbucket.mirror.service

import java.io.File
import java.net.{URL, URLDecoder}
import java.util.Date

import gitbucket.core.util.Directory
import io.github.gitbucket.mirror.model.Profile.{MirrorStatuses, Mirrors}
import io.github.gitbucket.mirror.model.{Mirror, MirrorStatus}
import org.eclipse.jgit.api.Git
import org.eclipse.jgit.storage.file.FileRepositoryBuilder
import org.eclipse.jgit.transport.{RefSpec, UsernamePasswordCredentialsProvider}
import org.slf4j.LoggerFactory
import gitbucket.core.model.Profile.profile.api._
import gitbucket.core.servlet.Database

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.Try

trait MirrorService {

  private val logger = LoggerFactory.getLogger(classOf[MirrorService])

  private val db = Database()

  def deleteMirror(mirrorId: Int): Future[Boolean] = {
    db.run {
      Mirrors
        .filter {
          _.id === mirrorId.bind
        }
        .delete
        .transactionally
        .map(_ > 0)
    }
  }

  def deleteMirrorByRepository(owner: String, repositoryName: String): Future[Int] = {
    db.run {
      Mirrors
        .filter { mirror => mirror.userName === owner.bind && mirror.repositoryName === repositoryName.bind }
        .delete
        .transactionally
    }
  }

  def findMirrorByRepository(owner: String, repositoryName: String): Future[Seq[Mirror]] = {
    db.run {
      Mirrors
        .filter { mirror => mirror.userName === owner.bind && mirror.repositoryName === repositoryName.bind }
        .result
    }
  }

  def findMirrorByRepositoryWithStatus(owner: String, repositoryName: String): Future[Seq[(Mirror, Option[MirrorStatus])]] = {
    db.run {
      Mirrors
        .filter { mirror => mirror.userName === owner.bind && mirror.repositoryName === repositoryName.bind }
        .joinLeft(MirrorStatuses).on(_.id === _.mirrorId)
        .result
    }
  }

  def getMirror(mirrorId: Int): Future[Option[Mirror]] = {
    db.run {
      Mirrors
        .filter {
          _.id === mirrorId.bind
        }
        .result
        .headOption
    }
  }

  def getMirrorUpdate(mirrorId: Int): Future[Option[MirrorStatus]] = {
    db.run {
      MirrorStatuses
        .filter {
          _.mirrorId === mirrorId.bind
        }
        .result
        .headOption
    }
  }

  def insertOrUpdateMirrorUpdate(status: MirrorStatus): Future[MirrorStatus] = {
    db.run {
      MirrorStatuses.insertOrUpdate(status).map(_ => status).transactionally
    }
  }

  def insertMirror(mirror: Mirror): Future[Mirror] = {
    db.run {
      val insertQuery = Mirrors returning Mirrors.map(_.id) into ((m, id) => m.copy(id = Some(id)))
      (insertQuery += mirror).transactionally
    }
  }

  def updateMirror(mirror: Mirror): Future[Option[Mirror]] = {
    db.run {
      Mirrors
        .filter {
          _.id === mirror.id.bind
        }
        .update(mirror)
        .transactionally
        .map { rowNumber => if (rowNumber == 0) None else Some(mirror) }
    }
  }

  def renameMirrorRepository(owner: String, repositoryName: String, newRepositoryName: String): Future[Int] = {
    db.run {
      Mirrors
        .filter { mirror => mirror.userName === owner.bind && mirror.repositoryName === repositoryName.bind }
        .map { mirror => mirror.repositoryName }
        .update(newRepositoryName)
        .transactionally
    }
  }

  def executeMirrorUpdate(mirror: Mirror): Future[MirrorStatus] = {

    val result = Try {

      // Build the repository object.

      val repositoryPath =
        Directory.GitBucketHome + "/repositories/" + mirror.userName + "/" + mirror.repositoryName + ".git"

      val repository = new FileRepositoryBuilder()
        .setGitDir(new File(repositoryPath))
        .readEnvironment()
        .findGitDir()
        .build()

      // Extract credentials from the URL.

      val userInfo = Option(new URL(mirror.remoteUrl).getUserInfo).getOrElse("").split(":")
      val username = URLDecoder.decode(if (userInfo.nonEmpty) userInfo(0) else "", "UTF-8")
      val password = URLDecoder.decode(if (userInfo.size > 1) userInfo(1) else "", "UTF-8")

      val credentialsProvider = new UsernamePasswordCredentialsProvider(username, password)

      // Push to the remote.

      val git = new Git(repository)

      git.push()
        .setRemote(mirror.remoteUrl)
        .setRefSpecs(new RefSpec("+refs/*:refs/*"))
        .setCredentialsProvider(credentialsProvider)
        .call()
    }

    // Convert the result to a mirror status.

    val date = new Date(System.currentTimeMillis())

    val onFailure = (throwable: Throwable) => {
      logger.error(
        s"Error while executing mirror status for repository ${mirror.userName}/${mirror.repositoryName}: " +
          s"${throwable.getMessage}"
      )

      MirrorStatus(mirror.id.get, date, successful = false, Some(throwable.getMessage))
    }

    val onSuccess = (_: Any) => {
      logger.info(
        s"Mirror status has been successfully executed for repository ${mirror.userName}/${mirror.repositoryName}."
      )

      MirrorStatus(mirror.id.get, date, successful = true, None)
    }

    val status = result.fold(onFailure, onSuccess)

    // Save the status.

    insertOrUpdateMirrorUpdate(status)
  }

}
