package su.plo.voice.discs.api

import org.bukkit.block.Block
import org.bukkit.event.Cancellable
import org.bukkit.event.Event
import org.bukkit.event.HandlerList
import su.plo.voice.api.server.player.VoicePlayer
import su.plo.voice.lavaplayer.libs.com.sedmelluq.discord.lavaplayer.track.AudioTrack

class PlayTrackFromDiscEvent(
    player: VoicePlayer? = null,
    track: AudioTrack,
    val block: Block,
) : PlayTrackEvent(player, track) {
    override fun getHandlers(): HandlerList = handlerList

    companion object {
        private val handlerList = HandlerList()

        @JvmStatic
        fun getHandlerList(): HandlerList = handlerList
    }
}
