package io.github.gitbucket.mirror.util.git

import java.net.URI

import org.eclipse.jgit.api.TransportCommand

import scala.util.Try

package object transport {

  implicit class TransportCommandWrapper[C <: TransportCommand[C, _]](command: C) {

    def configureTransport(remoteUrl: URI): Try[C] = TransportConfigurator[C](remoteUrl)(command)
  }
}
