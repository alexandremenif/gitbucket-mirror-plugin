package io.github.gitbucket.mirror.model

trait MirrorComponent { self: gitbucket.core.model.Profile =>
  import profile.api._
  import self._

  lazy val Mirrors = TableQuery[Mirrors]

  class Mirrors(tag: Tag) extends Table[Mirror](tag, "MIRROR") {
    val id = column[Int]("ID", O.PrimaryKey, O.AutoInc)
    val userName = column[String]("USER_NAME")
    val repositoryName = column[String]("REPOSITORY_NAME")
    val name = column[String]("NAME")
    val remoteUrl = column[String]("REMOTE_URL")
    val enabled = column[Boolean]("ENABLED")

    def * = (
      id.?,
      userName,
      repositoryName,
      name,
      remoteUrl,
      enabled
    ) <> (Mirror.tupled, Mirror.unapply)

    def byId(mirrorId: Int): Rep[Boolean] = this.id === mirrorId.bind

    def byId(mirrorId: Option[Int]): Rep[Option[Boolean]] = this.id === mirrorId.bind

    def byRepository(owner: String, repositoryName: String): Rep[Boolean] =
      this.userName === owner.bind && this.repositoryName === repositoryName.bind
  }
}

final case class Mirror(
  id: Option[Int],
  userName: String,
  repositoryName: String,
  name: String,
  remoteUrl: String,
  enabled: Boolean
)
