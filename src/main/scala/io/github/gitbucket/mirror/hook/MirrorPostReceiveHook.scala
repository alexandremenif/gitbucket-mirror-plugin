package io.github.gitbucket.mirror.hook

import gitbucket.core.model.Profile
import gitbucket.core.plugin.ReceiveHook
import io.github.gitbucket.mirror.service.MirrorService
import org.eclipse.jgit.transport.{ReceiveCommand, ReceivePack}

class MirrorPostReceiveHook extends ReceiveHook with MirrorService {

  override def postReceive(
    owner: String,
    repository: String,
    receivePack: ReceivePack,
    command: ReceiveCommand,
    pusher: String,
    mergePullRequest: Boolean
  )(implicit session: Profile.profile.api.Session): Unit =
    findMirrorsByRepository(owner, repository).foreach { mirror =>
      if (mirror.enabled) executeMirrorUpdate(mirror)
    }
}
