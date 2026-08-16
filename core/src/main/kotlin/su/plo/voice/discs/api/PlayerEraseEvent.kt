package su.plo.voice.discs.api

import org.bukkit.event.HandlerList
import org.bukkit.inventory.ItemStack
import su.plo.voice.api.server.player.VoiceServerPlayer

class PlayerEraseEvent(
    player: VoiceServerPlayer,
    val item: ItemStack,
) : VoicePlayerEvent(player) {
    override fun getHandlers(): HandlerList = handlerList

    companion object {
        private val handlerList = HandlerList()

        @JvmStatic
        fun getHandlerList(): HandlerList = handlerList
    }
}
