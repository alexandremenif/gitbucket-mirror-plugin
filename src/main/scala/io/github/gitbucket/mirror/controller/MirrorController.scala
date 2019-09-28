package io.github.gitbucket.mirror.controller

import gitbucket.core.controller.ControllerBase
import gitbucket.core.model.Profile.profile.blockingApi._
import gitbucket.core.service.{AccountService, RepositoryService}
import gitbucket.core.servlet.Database
import gitbucket.core.util.OwnerAuthenticator
import io.github.gitbucket.mirror.service.MirrorService

class MirrorController extends ControllerBase
  with AccountService
  with MirrorService
  with OwnerAuthenticator
  with RepositoryService {

  private val db = Database()

  get("/:owner/:repository/mirrors")(ownerOnly { repository =>
    db.withSession { implicit session =>
      gitbucket.mirror.html.list(
        findMirrorsWithStatusByRepository(repository.owner, repository.name),
        repository
      )
    }
  })

  get("/:owner/:repository/mirrors/new")(ownerOnly { repository =>
    db.withSession { implicit session =>
      gitbucket.mirror.html.create(repository)
    }
  })

  get("/:owner/:repository/mirrors/:id/edit")(ownerOnly { repository =>
    db.withSession { implicit session =>
      val mirrorWithStatusOption = for {
        mirrorId <- params.getAs[Int]("id")
        mirrorWithStatus <- getMirrorWithStatus(mirrorId)
      } yield mirrorWithStatus

      mirrorWithStatusOption
        .map { case (mirror, mirrorStatus) =>
          gitbucket.mirror.html.mirror(mirror, mirrorStatus, repository)
        }
        .getOrElse(NotFound())
    }
  })
}
