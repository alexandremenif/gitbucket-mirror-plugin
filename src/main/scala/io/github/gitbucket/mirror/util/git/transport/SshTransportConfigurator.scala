package io.github.gitbucket.mirror.util.git.transport

import java.net.URI

import com.jcraft.jsch.{JSch, Session}
import org.eclipse.jgit.api.{TransportCommand, TransportConfigCallback}
import org.eclipse.jgit.transport.OpenSshConfig.Host
import org.eclipse.jgit.transport.{JschConfigSessionFactory, SshSessionFactory}
import org.eclipse.jgit.transport.{Transport => JGitTransport}
import org.eclipse.jgit.transport.{SshTransport => JGitSssTransport}
import org.eclipse.jgit.util.FS

import scala.util.Try

class SshTransportConfigurator[C <: TransportCommand[C, _]](val remoteUrl: URI) extends TransportConfigurator[C] {

  override def apply(command: C): Try[C] = {

    Credentials.fromUrl(remoteUrl).flatMap { optCredential =>
      Try {

        val sshSessionFactory: SshSessionFactory = new JschConfigSessionFactory() {

          override def createDefaultJSch(fs: FS): JSch = {
            val defaultJsch = super.createDefaultJSch(fs)

            sshPrivateKeys().foreach { privateKey => defaultJsch.addIdentity(privateKey) }
            sshHostKeys().foreach { knownHosts => defaultJsch.setKnownHosts(knownHosts) }

            defaultJsch
          }

          override def configure(hc: Host, session: Session): Unit = {

            optCredential.foreach { case Credentials(_, password) =>
              session.setPassword(password)
            }
          }
        }

        command.setTransportConfigCallback(new TransportConfigCallback() {

          override def configure(transport: JGitTransport): Unit = {
            transport.asInstanceOf[JGitSssTransport].setSshSessionFactory(sshSessionFactory)
          }
        })
      }
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