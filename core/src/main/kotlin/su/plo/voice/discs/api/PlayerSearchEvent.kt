package su.plo.voice.discs.api

import org.bukkit.event.HandlerList
import su.plo.voice.api.server.player.VoiceServerPlayer

class PlayerSearchEvent(
    player: VoiceServerPlayer,
    val query: String,
) : VoicePlayerEvent(player) {
    override fun getHandlers(): HandlerList = handlerList

    companion object {
        private val handlerList = HandlerList()

        @JvmStatic
        fun getHandlerList(): HandlerList = handlerList
    }
}
