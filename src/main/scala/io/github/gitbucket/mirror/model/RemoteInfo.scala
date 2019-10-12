package io.github.gitbucket.mirror.model

import java.net.{URI, URLDecoder}

import scala.util.Try

final case class RemoteInfo(url: URI, passwordOption: Option[String]) {

  def username: Try[Option[String]] = userInfoUsername

  def password: Try[Option[String]] = passwordOption.fold(userInfoPassword)(password => Try(Some(password)))

  private def userInfoOption: Try[Option[String]] = Try {
    Option(url.getUserInfo)
      .withFilter(_.nonEmpty)
      .map(URLDecoder.decode(_, "UTF-8"))
  }

  private def userInfoUsername: Try[Option[String]] = userInfoOption.map(_.map { userInfo =>
    val colonIndex = userInfo.indexOf(":")
    if (colonIndex == -1) userInfo else userInfo.substring(0, colonIndex)
  })

  private def userInfoPassword: Try[Option[String]] = userInfoOption.map(_.map { userInfo =>
    val colonIndex = userInfo.indexOf(":")
    if (colonIndex == -1) "" else userInfo.substring(colonIndex + 1)
  })
}
