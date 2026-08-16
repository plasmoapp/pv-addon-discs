package su.plo.voice.discs.api

import org.bukkit.event.Cancellable
import org.bukkit.event.HandlerList
import su.plo.voice.api.server.player.VoiceServerPlayer

class PrePlayerBurnEvent(player: VoiceServerPlayer, identifier: String) : VoicePlayerEvent(player), Cancellable {

    companion object {
        private val handlerList = HandlerList()

        @JvmStatic
        fun getHandlerList(): HandlerList = handlerList
    }

    private var cancelled = false

    override fun getHandlers(): HandlerList = handlerList

    override fun isCancelled(): Boolean = cancelled

    override fun setCancelled(cancel: Boolean) {
        cancelled = cancel
    }

}