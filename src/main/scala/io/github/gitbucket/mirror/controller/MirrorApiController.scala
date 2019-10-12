package io.github.gitbucket.mirror.controller

import gitbucket.core.controller.ControllerBase
import gitbucket.core.model.Profile.profile.blockingApi._
import gitbucket.core.service.{AccountService, RepositoryService}
import gitbucket.core.servlet.Database
import gitbucket.core.util.OwnerAuthenticator
import io.github.gitbucket.mirror.model.Mirror
import io.github.gitbucket.mirror.service.MirrorService
import org.scalatra.{Ok, _}
import scala.util.Try

import org.slf4j.LoggerFactory

class MirrorApiController extends ControllerBase
  with AccountService
  with MirrorService
  with OwnerAuthenticator
  with RepositoryService {

  private val db = Database()

  private val logger = LoggerFactory.getLogger(classOf[MirrorApiController])

  delete("/api/v3/repos/:owner/:repository/mirrors/:id") (ownerOnly { _ =>

    db.withTransaction { implicit session =>
      val deleted = params.getAs[Int]("id").exists(deleteMirror(_))

      if (deleted) NoContent() else NotFound()
    }
  })

  get("/api/v3/repos/:owner/:repository/mirrors") (ownerOnly { repository =>
    db.withSession { implicit session =>
      findMirrorsByRepository(repository.owner, repository.name)
    }
  })

  get("/api/v3/repos/:owner/:repository/mirrors/:id") (ownerOnly { _ =>
    db.withSession { implicit session =>
      params.getAs[Int]("id")
        .flatMap(mirrorId => getMirror(mirrorId))
        .map { mirror => Ok(mirror) }
        .getOrElse(NotFound())
    }
  })

  post("/api/v3/repos/:owner/:repository/mirrors") (ownerOnly { repository =>
    db.withTransaction { implicit session =>
      Try(parsedBody.extract[Mirror])
        .fold(
          error => {
            logger.error(error.getMessage)
            BadRequest(error)
          },
          body => {
            val mirror = insertMirror(body)
            val location = s"${context.path}/api/v3/${repository.owner}/${repository.name}/mirrors/${mirror.id.get}"

            Created(mirror, Map("location" -> location))
          }
      )
    }
  })

  put("/api/v3/repos/:owner/:repository/mirrors/:id") (ownerOnly { _ =>
    db.withTransaction { implicit session =>
      val result = for {
        mirrorId <- params.getAs[Int]("id").toRight(NotFound())
        body <- Try(parsedBody.extract[Mirror]).fold[Either[ActionResult, Mirror]](
          error => {
            logger.error(error.getMessage)
            Left(BadRequest(error))
          },
          Right(_)
        )
        mirror <- updateMirror(body.copy(id = Some(mirrorId))).toRight(NotFound())
      } yield Ok(mirror)

      result.merge
    }
  })

  put("/api/v3/repos/:owner/:repository/mirrors/:id/status") (ownerOnly { _ =>
    db.withTransaction { implicit session =>
      val status = for {
        mirrorId <- params.getAs[Int]("id")
        mirror <- getMirror(mirrorId)
      } yield executeMirrorUpdate(mirror)

      status
        .map(Ok(_))
        .getOrElse(NotFound())
    }
  })
}
