package io.github.gitbucket.mirror.util.git

import org.eclipse.jgit.api.TransportCommand
import scala.util.Try

import io.github.gitbucket.mirror.model.RemoteInfo

package object transport {

  implicit class TransportCommandWrapper[C <: TransportCommand[C, _]](command: C) {

    def configureTransport(remoteInfo: RemoteInfo): Try[C] =
      TransportConfigurator[C](remoteInfo)(command)
  }
}
