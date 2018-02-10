package io.github.gitbucket.mirror.util.git.transport

import java.io.IOException
import java.net.URI
import org.eclipse.jgit.api.TransportCommand

import scala.util.{Success, Try}


trait TransportConfigurator[C <: TransportCommand[C, _]] {

  def apply(command: C): Try[C]

}

object TransportConfigurator {

  def apply[C <: TransportCommand[C, _]](remoteUrl: URI): TransportConfigurator[C] = {

    val scheme = remoteUrl.getScheme

    scheme match {
      case "http" | "https" =>
        new HttpTransportConfigurator[C](remoteUrl)
      case "ssh" =>
        new SshTransportConfigurator[C](remoteUrl)
      case _ =>
        new TransportConfigurator[C] {
          override def apply(command: C): Try[C] = Success(command)
        }
    }
  }

}


