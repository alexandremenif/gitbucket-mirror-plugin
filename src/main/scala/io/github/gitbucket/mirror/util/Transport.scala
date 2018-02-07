package io.github.gitbucket.mirror.util

import java.io.IOException
import java.net.URI

import org.eclipse.jgit.api.TransportCommand
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider

import scala.util.{Failure, Try}

object Transport {

  def configure[C <: TransportCommand[C, _]](command: C, remoteUrl: URI): Try[C] = {

    val scheme = remoteUrl.getScheme

    scheme match {
      case "http" | "https" =>
        configureHttp[C](command, remoteUrl)
      case _ =>
        Failure(new IOException(s"$scheme is not a supported protocol."))
    }
  }

  def configureHttp[C <: TransportCommand[C, _]](command: C, remoteUrl: URI): Try[C] = {

    Credentials
      .fromUrl(remoteUrl)
      .map {
        case Some(Credentials(username, password)) =>
          command.setCredentialsProvider(new UsernamePasswordCredentialsProvider(username, password))
        case None => command
      }
  }
}
