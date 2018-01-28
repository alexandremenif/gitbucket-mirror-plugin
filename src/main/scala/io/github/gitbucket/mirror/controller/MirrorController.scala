package io.github.gitbucket.mirror.controller

import gitbucket.core.controller.ControllerBase
import gitbucket.core.service.{AccountService, RepositoryService}
import gitbucket.core.util.OwnerAuthenticator
import io.github.gitbucket.mirror.service.MirrorService
import org.slf4j.LoggerFactory

import scala.concurrent.Await
import scala.concurrent.duration._

class MirrorController extends ControllerBase
  with AccountService
  with MirrorService
  with OwnerAuthenticator
  with RepositoryService {

  private val logger = LoggerFactory.getLogger(classOf[MirrorController])

  get("/:owner/:repository/mirrors")(ownerOnly { repository =>

    val mirrorsWithUpdate = Await.result(
      findMirrorByRepositoryWithStatus(repository.owner, repository.name),
      60 seconds
    )

    gitbucket.mirror.html.list(mirrorsWithUpdate, repository)

  })

  get("/:owner/:repository/mirrors/new")(ownerOnly { repository =>

    gitbucket.mirror.html.create(repository)

  })

  get("/:owner/:repository/mirrors/:id/edit")(ownerOnly { repository =>

    val option = for {
      mirrorId <- params.getAs[Int]("id")
      mirror <- Await.result(getMirror(mirrorId), 60 seconds)
    } yield (mirror, Await.result(getMirrorUpdate(mirrorId), 60 seconds))

    option
      .map { case (mirror, updateOption) =>
        gitbucket.mirror.html.mirror(mirror, updateOption, repository)
      }
      .getOrElse(NotFound())

  })
}
