package su.plo.voice.discs.api

import org.bukkit.event.Event
import su.plo.voice.api.server.player.VoiceServerPlayer

abstract class VoicePlayerEvent(val player: VoiceServerPlayer) : Event(true)