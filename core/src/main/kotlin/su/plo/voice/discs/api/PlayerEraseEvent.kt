package su.plo.voice.discs.api

import org.bukkit.event.HandlerList
import su.plo.voice.api.server.player.VoiceServerPlayer

class PlayerEraseEvent(player: VoiceServerPlayer) : VoicePlayerEvent(player) {

    companion object {
        private val handlerList = HandlerList()

        @JvmStatic
        fun getHandlerList(): HandlerList = handlerList
    }

    override fun getHandlers(): HandlerList = handlerList

}