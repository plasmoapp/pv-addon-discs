package su.plo.voice.discs.api

import org.bukkit.event.HandlerList
import org.bukkit.inventory.ItemStack
import su.plo.voice.api.server.player.VoiceServerPlayer
import su.plo.voice.lavaplayer.libs.com.sedmelluq.discord.lavaplayer.track.AudioTrack

class PlayerBurnEvent(player: VoiceServerPlayer, audioTrack: AudioTrack, item: ItemStack) : VoicePlayerEvent(player) {

    companion object {
        private val handlerList = HandlerList()

        @JvmStatic
        fun getHandlerList(): HandlerList = handlerList
    }

    override fun getHandlers(): HandlerList = handlerList

}