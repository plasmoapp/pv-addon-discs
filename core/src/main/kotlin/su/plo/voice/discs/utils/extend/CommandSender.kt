package su.plo.voice.discs.utils.extend

import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import su.plo.slib.api.chat.component.McTextComponent
import su.plo.slib.api.command.McCommandSource
import su.plo.voice.api.server.PlasmoVoiceServer
import su.plo.voice.api.server.player.VoiceServerPlayer

fun CommandSender.asPlayer() = this as? Player

fun McCommandSource.sendTranslatable(key: String, vararg args: Any) = this.sendMessage(
    McTextComponent.translatable(key, *args)
)

fun CommandSender.asVoicePlayer(voiceServer: PlasmoVoiceServer): VoiceServerPlayer? {
    return this.asPlayer()?.let { voiceServer.playerManager.getPlayerById(it.uniqueId).orElse(null) }
}
