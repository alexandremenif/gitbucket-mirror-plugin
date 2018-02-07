package io.github.gitbucket.mirror.util

import java.net.URI

import org.eclipse.jgit.api.{Git, TransportCommand}

import scala.util.Try

package object wrapper {

  implicit class GitWrapper(git: Git) {

    def pushMirror(): PushMirrorCommand = new PushMirrorCommand(git.getRepository)

  }

  implicit class TransportCommandWrapper[C <: TransportCommand[C, _]](command: C) {

    def configureTransport(remoteUrl: URI): Try[C] = Transport.configure[C](command, remoteUrl)
  }
}
