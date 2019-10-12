package io.github.gitbucket.mirror.model

trait MirrorComponent { self: gitbucket.core.model.Profile =>
  import profile.api._
  import self._

  lazy val Mirrors = TableQuery[Mirrors]

  class Mirrors(tag: Tag) extends Table[Mirror](tag, "MIRROR") {
    val id = column[Int]("ID", O.PrimaryKey, O.AutoInc)
    val username = column[String]("USERNAME")
    val repositoryName = column[String]("REPOSITORY_NAME")
    val name = column[String]("NAME")
    val remoteUrl = column[String]("REMOTE_URL")
    val remotePassword = column[Option[String]]("REMOTE_PASSWORD")
    val enabled = column[Boolean]("ENABLED")

    def * = (
      id.?,
      username,
      repositoryName,
      name,
      remoteUrl,
      remotePassword,
      enabled
    ) <> (Mirror.tupled, Mirror.unapply)

    def byId(mirrorId: Int): Rep[Boolean] = this.id === mirrorId.bind

    def byId(mirrorId: Option[Int]): Rep[Option[Boolean]] = this.id === mirrorId.bind

    def byRepository(owner: String, repositoryName: String): Rep[Boolean] =
      this.username === owner.bind && this.repositoryName === repositoryName.bind
  }
}

final case class Mirror(
  id: Option[Int],
  username: String,
  repositoryName: String,
  name: String,
  remoteUrl: String,
  remotePassword: Option[String],
  enabled: Boolean
)
