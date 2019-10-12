package io.github.gitbucket.mirror.util.git.transport

import com.jcraft.jsch.{JSch, Session}
import org.eclipse.jgit.api.TransportCommand
import org.eclipse.jgit.transport.OpenSshConfig.Host
import org.eclipse.jgit.transport.{JschConfigSessionFactory, SshSessionFactory, SshTransport}
import org.eclipse.jgit.util.FS
import scala.util.Try

import io.github.gitbucket.mirror.model.RemoteInfo

class SshTransportConfigurator[C <: TransportCommand[C, _]](remoteInfo: RemoteInfo) extends TransportConfigurator[C] {

  override def apply(command: C): Try[C] = {

    for {
      passwordOption <- remoteInfo.password
    } yield {

      val sshSessionFactory: SshSessionFactory = new JschConfigSessionFactory() {

        override def createDefaultJSch(fs: FS): JSch = {
          val defaultJsch = super.createDefaultJSch(fs)

          sshPrivateKeys().foreach { privateKey => defaultJsch.addIdentity(privateKey) }
          sshHostKeys().foreach { knownHosts => defaultJsch.setKnownHosts(knownHosts) }

          defaultJsch
        }

        override def configure(hc: Host, session: Session): Unit = {

          passwordOption.foreach { password =>
            session.setPassword(password)
          }
        }
      }

      command.setTransportConfigCallback(_.asInstanceOf[SshTransport].setSshSessionFactory(sshSessionFactory))
    }
  }

  def sshPrivateKeys(): List[String] = {
    sys.env.get(SshTransportProperties.SshPrivateKeys)
      .withFilter(_.nonEmpty)
      .map(_.split(";").toList)
      .getOrElse(Nil)
  }

  def sshHostKeys(): Option[String] = sys.env.get(SshTransportProperties.SshHostKeys)
}

object SshTransportProperties {

  val SshPrivateKeys: String = "MIRROR_SSH_PRIVATE_KEYS"
  val SshHostKeys: String = "MIRROR_SSH_HOST_KEYS"
}