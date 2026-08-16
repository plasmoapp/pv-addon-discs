package su.plo.voice.discs.api

import org.bukkit.event.HandlerList
import su.plo.voice.api.server.player.VoicePlayer
import su.plo.voice.lavaplayer.libs.com.sedmelluq.discord.lavaplayer.track.AudioTrack

class PlayTrackFromGoatHornEvent(player: VoicePlayer, track: AudioTrack) : PlayTrackEvent(player, track) {

    companion object {
        private val handlerList = HandlerList()

        @JvmStatic
        fun getHandlerList(): HandlerList = handlerList
    }

    override fun getHandlers(): HandlerList = handlerList

}