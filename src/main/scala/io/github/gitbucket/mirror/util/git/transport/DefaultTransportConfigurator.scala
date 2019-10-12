package io.github.gitbucket.mirror.util.git.transport

import org.eclipse.jgit.api.TransportCommand
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider
import scala.util.Try

import io.github.gitbucket.mirror.model.RemoteInfo

class DefaultTransportConfigurator[C <: TransportCommand[C, _]](remoteInfo: RemoteInfo) extends TransportConfigurator[C] {

  override def apply(command: C): Try[C] =
    for {
      usernameOption <- remoteInfo.username
      passwordOption <- remoteInfo.password
    } yield (usernameOption, passwordOption) match {
      case (None, None) =>
        command
      case _ =>
        command.setCredentialsProvider(
          new UsernamePasswordCredentialsProvider(usernameOption.getOrElse(""), passwordOption.getOrElse(""))
        )
    }
}
