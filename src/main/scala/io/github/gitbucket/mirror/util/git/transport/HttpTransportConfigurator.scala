package io.github.gitbucket.mirror.util.git.transport

import java.net.URI

import org.eclipse.jgit.api.TransportCommand
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider

import scala.util.Try

class HttpTransportConfigurator[C <: TransportCommand[C, _]](val remoteUrl: URI) extends TransportConfigurator[C] {

  override def apply(command: C): Try[C] = {

    Credentials
      .fromUrl(remoteUrl)
      .map {
        case Some(Credentials(username, password)) =>
          command.setCredentialsProvider(new UsernamePasswordCredentialsProvider(username, password))
        case None => command
      }
  }
}
