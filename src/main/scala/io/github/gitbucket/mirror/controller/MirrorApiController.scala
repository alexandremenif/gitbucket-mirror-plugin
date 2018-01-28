package io.github.gitbucket.mirror.controller

import gitbucket.core.controller.ControllerBase
import gitbucket.core.service.{AccountService, RepositoryService}
import gitbucket.core.util.OwnerAuthenticator
import io.github.gitbucket.mirror.model.Mirror
import io.github.gitbucket.mirror.service.MirrorService
import org.scalatra.{Ok, _}

import scala.concurrent.Await
import scala.concurrent.duration._
import scala.util.Try

class MirrorApiController extends ControllerBase
  with AccountService
  with MirrorService
  with OwnerAuthenticator
  with RepositoryService {

  delete("/api/v3/repos/:owner/:repository/mirrors/:id") (ownerOnly { _ =>

    val deleted = params.getAs[Int]("id").exists { mirrorId =>
      Await.result(deleteMirror(mirrorId), 60 seconds)
    }

    if (deleted) NoContent() else NotFound()

  })

  get("/api/v3/repos/:owner/:repository/mirrors") (ownerOnly { repository =>

    Await.result(findMirrorByRepository(repository.owner, repository.name), 60 seconds)
  })

  get("/api/v3/repos/:owner/:repository/mirrors/:id") (ownerOnly { repository =>

    params.getAs[Int]("id")
      .flatMap(mirrorId => Await.result(getMirror(mirrorId), 60 seconds))
      .map { mirror => Ok(mirror) }
      .getOrElse(NotFound())
  })

  post("/api/v3/repos/:owner/:repository/mirrors") (ownerOnly { repository =>

    Try(parsedBody.extract[Mirror])
      .map { body =>

        val mirror = Await.result(insertMirror(body), 60 seconds)
        val location = s"${context.path}/api/v3/${repository.owner}/${repository.name}/mirrors/${mirror.id.get}"

        Created(mirror, Map("location" -> location))
      }
      .getOrElse(BadRequest())

  })

  put("/api/v3/repos/:owner/:repository/mirrors/:id") (ownerOnly { repository =>

    val result = for {
      mirrorId <- params.getAs[Int]("id").toRight(NotFound())
      body <- Try(parsedBody.extract[Mirror]).fold[Either[ActionResult, Mirror]](_ => Left(BadRequest()), Right(_))
      mirror <- Await.result(updateMirror(body.copy(id = Some(mirrorId))), 60 seconds).toRight(NotFound())
    } yield Ok(mirror)

    result.merge
  })

  put("/api/v3/repos/:owner/:repository/mirrors/:id/status") (ownerOnly { repository =>

    val status = for {
      mirrorId <- params.getAs[Int]("id")
      mirror <- Await.result(getMirror(mirrorId), 60 seconds)
    } yield Await.result(executeMirrorUpdate(mirror), 60 seconds)

    status
      .map(Ok(_))
      .getOrElse(NotFound())
  })
}
