package io.github.gitbucket.mirror.util.git.transport

import org.eclipse.jgit.api.TransportCommand
import scala.util.Try

import io.github.gitbucket.mirror.model.RemoteInfo


trait TransportConfigurator[C <: TransportCommand[C, _]] {

  def apply(command: C): Try[C]
}

object TransportConfigurator {

  def apply[C <: TransportCommand[C, _]](remoteInfo: RemoteInfo): TransportConfigurator[C] = {

    remoteInfo.url.getScheme match {
      case "ssh" =>
        new SshTransportConfigurator[C](remoteInfo)
      case _ =>
        new DefaultTransportConfigurator[C](remoteInfo)
    }
  }
}


