package io.github.gitbucket.mirror.util.git.command

import org.eclipse.jgit.api.{Git, TransportCommand}
import org.eclipse.jgit.lib.{Constants, Repository}
import org.eclipse.jgit.transport.RefSpec

import scala.collection.JavaConverters._

class PushMirrorCommand(repo: Repository) extends TransportCommand[PushMirrorCommand, Unit](repo) {

  private var remote: String = Constants.DEFAULT_REMOTE_NAME

  def getRemote: String = remote

  def setRemote(remote: String): PushMirrorCommand = {
    this.remote = remote
    this
  }

  override def call(): Unit = {

    val mirrorRefSpec = new RefSpec("+refs/*:refs/*")
    val git = new Git(repo)

    // The mirror ref spec is not enough to propagate local deleted references. Therefore we use the remote ls
    // command to find all the deleted ref specs and add them explicitly.

    val lsRemoteCommand = git.lsRemote()
      .setRemote(remote)
      .setHeads(true)
      .setTags(true)

    configure(lsRemoteCommand)

    val deletedRefSpecs = lsRemoteCommand.call()
      .asScala
      .withFilter { ref => repo.findRef(ref.getName) == null }
      .map { ref => new RefSpec(s":${ref.getName}") }

    val pushCommand = git.push()
      .setRemote(remote)
      .setRefSpecs(((mirrorRefSpec :: Nil) ++ deletedRefSpecs).asJava)
      .setForce(true)

    configure(pushCommand)

    pushCommand.call()
  }

}
