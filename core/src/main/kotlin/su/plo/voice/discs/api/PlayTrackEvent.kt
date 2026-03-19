package su.plo.voice.discs.api

import org.bukkit.block.Block
import org.bukkit.event.Cancellable
import org.bukkit.event.Event
import org.bukkit.event.HandlerList
import su.plo.voice.api.server.player.VoicePlayer
import su.plo.voice.lavaplayer.libs.com.sedmelluq.discord.lavaplayer.track.AudioTrack

class PlayTrackEvent(val player: VoicePlayer? = null, val block: Block, val track: AudioTrack) : Event(true), Cancellable {

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