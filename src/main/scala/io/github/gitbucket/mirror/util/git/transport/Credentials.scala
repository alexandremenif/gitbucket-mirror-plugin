package io.github.gitbucket.mirror.util.git.transport

import java.net.{URI, URLDecoder}

import scala.util.Try

final case class Credentials(username: String, password: String)

object Credentials {

  def fromUrl(url: URI): Try[Option[Credentials]] = Try {

    Option(url.getUserInfo)
      .withFilter { userInfo => userInfo.nonEmpty && userInfo.charAt(0) != ':' }
      .map { userInfo =>

        val colonIndex = userInfo.indexOf(":")

        Credentials(
          URLDecoder.decode(if (colonIndex == -1) userInfo else userInfo.substring(0, colonIndex), "UTF-8"),
          URLDecoder.decode(if (colonIndex == -1) "" else userInfo.substring(colonIndex + 1), "UTF-8")
        )
      }
  }
}