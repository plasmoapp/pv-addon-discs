package su.plo.voice.discs.api

import org.bukkit.event.Event
import su.plo.voice.api.server.player.VoicePlayer
import su.plo.voice.lavaplayer.libs.com.sedmelluq.discord.lavaplayer.track.AudioTrack

abstract class PlayTrackEvent(
    val player: VoicePlayer? = null,
    val track: AudioTrack,
) : Event(true)
