package io.github.gitbucket.mirror.service

import java.io.File
import java.net.URI
import java.util.Date

import io.github.gitbucket.mirror.model.Profile.{MirrorStatuses, Mirrors}
import io.github.gitbucket.mirror.model.{Mirror, MirrorStatus}
import io.github.gitbucket.mirror.util.git._
import io.github.gitbucket.mirror.util.git.transport._
import gitbucket.core.model.Profile.profile.blockingApi._
import gitbucket.core.util.Directory
import org.eclipse.jgit.api.Git
import org.eclipse.jgit.lib.Repository
import org.eclipse.jgit.storage.file.FileRepositoryBuilder
import org.slf4j.LoggerFactory

import scala.util.Try

trait MirrorService {

  private val logger = LoggerFactory.getLogger(classOf[MirrorService])

  type MirrorWithStatus = (Mirror, Option[MirrorStatus])

  def deleteMirror(mirrorId: Int)(implicit session: Session): Boolean =
    Mirrors
      .filter(_.byId(mirrorId))
      .delete > 0

  def deleteMirrorsByRepository(owner: String, repositoryName: String)(implicit session: Session): Int =
    Mirrors
      .filter(_.byRepository(owner, repositoryName))
      .delete

  def findMirrorsByRepository(owner: String, repositoryName: String)(implicit session: Session): List[Mirror] =
    Mirrors
      .filter(_.byRepository(owner, repositoryName))
      .list

  def findMirrorsWithStatusByRepository(
    owner: String,
    repositoryName: String
  )(implicit session: Session): List[MirrorWithStatus] =
    Mirrors
      .filter(_.byRepository(owner, repositoryName))
      .joinLeft(MirrorStatuses).on(_.id === _.mirrorId)
      .list

  def getMirror(mirrorId: Int)(implicit session: Session): Option[Mirror] =
    Mirrors
      .filter(_.byId(mirrorId))
      .list
      .headOption

  def getMirrorStatus(mirrorId: Int)(implicit session: Session): Option[MirrorStatus] =
    MirrorStatuses
      .filter(_.byMirrorId(mirrorId))
      .list
      .headOption

  def getMirrorWithStatus(mirrorId: Int)(implicit session: Session): Option[MirrorWithStatus] =
    Mirrors
      .filter(_.byId(mirrorId))
      .joinLeft(MirrorStatuses).on(_.id === _.mirrorId)
      .list
      .headOption

  def insertMirror(mirror: Mirror)(implicit session: Session): Mirror = {
    val insertQuery = Mirrors returning Mirrors.map(_.id) into ((m, id) => m.copy(id = Some(id)))
    insertQuery insert mirror
  }

  def updateMirror(mirror: Mirror)(implicit session: Session): Option[Mirror] = {
    val updatedRowCount = Mirrors
      .filter(_.byId(mirror.id))
      .update(mirror)

    Option.when(updatedRowCount > 0)(mirror)
  }

  def updateMirrorRepositoryName(
    owner: String,
    repositoryName: String,
    newRepositoryName: String
  )(implicit session: Session): Unit =
    Mirrors
      .filter(_.byRepository(owner, repositoryName))
      .map(_.repositoryName)
      .update(newRepositoryName)

  def executeMirrorUpdate(mirror: Mirror)(implicit session: Session): MirrorStatus = {

    def onFailure(throwable: Throwable): MirrorStatus = {

      val repositoryName = s"${mirror.userName}/${mirror.repositoryName}"
      val message = s"Error while executing mirror status for repository $repositoryName: ${throwable.getMessage}"

      logger.error(message, throwable)

      MirrorStatus(mirror.id.get, new Date(System.currentTimeMillis()), successful = false, Some(throwable.getMessage))
    }

    def onSuccess(): MirrorStatus = {
      logger.info(
        s"Mirror status has been successfully executed for repository ${mirror.userName}/${mirror.repositoryName}."
      )

      MirrorStatus(mirror.id.get, new Date(System.currentTimeMillis()), successful = true, None)
    }

    // Execute the push, get the result and convert it to a mirror status.

    val result = for {
      repository <- repository(mirror.userName, mirror.repositoryName)
      remoteUrl <- Try(URI.create(mirror.remoteUrl))
      pushMirrorCommand <- new Git(repository).pushMirror()
        .setRemote(remoteUrl.toString)
        .configureTransport(remoteUrl)
      _ <- Try { pushMirrorCommand.call() }
    } yield ()

    val status = result.fold(onFailure, _ => onSuccess())

    // Save the status.

    MirrorStatuses.insertOrUpdate(status)

    status
  }

  private def repository(owner: String, repositoryName: String): Try[Repository] = Try {

    val repositoryPath = s"${Directory.GitBucketHome}/repositories/$owner/$repositoryName.git"

    new FileRepositoryBuilder()
      .setGitDir(new File(repositoryPath))
      .readEnvironment()
      .findGitDir()
      .build()
  }

}
