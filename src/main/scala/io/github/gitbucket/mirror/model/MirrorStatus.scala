package io.github.gitbucket.mirror.model

import io.github.gitbucket.mirror.model.Profile._

import java.util.Date

trait MirrorStatusComponent { self: gitbucket.core.model.Profile =>
  import profile.api._
  import self._

  lazy val MirrorStatuses = TableQuery[MirrorStatuses]

  class MirrorStatuses(tag: Tag) extends Table[MirrorStatus](tag, "MIRROR_STATUS") {
    val mirrorId = column[Int]("MIRROR_ID", O.PrimaryKey)
    val date = column[Date]("DATE")
    val successful = column[Boolean]("SUCCESSFUL")
    val error = column[Option[String]]("ERROR")

    def mirror = foreignKey("IDX_MIRROR_UPDATE_FK0", mirrorId, Mirrors)(_.id, onDelete=ForeignKeyAction.Cascade)

    def * = (
      mirrorId,
      date,
      successful,
      error
    ) <> (MirrorStatus.tupled, MirrorStatus.unapply)
  }
}

final case class MirrorStatus(mirrorId: Int, date: Date, successful: Boolean, error: Option[String])
