package su.plo.voice.discs.api

import org.bukkit.event.Cancellable
import org.bukkit.event.Event
import su.plo.voice.api.server.player.VoiceServerPlayer

abstract class VoicePlayerEvent(
    val player: VoiceServerPlayer,
) : Event(), Cancellable {
    private var cancelled = false

    override fun isCancelled(): Boolean = cancelled

    override fun setCancelled(cancel: Boolean) {
        cancelled = cancel
    }
}
