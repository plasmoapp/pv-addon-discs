package su.plo.voice.disks.utils.extend

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.ComponentLike
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import su.plo.lib.api.chat.MinecraftTextComponent
import su.plo.lib.api.server.command.MinecraftCommandSource
import su.plo.voice.api.server.PlasmoVoiceServer
import su.plo.voice.api.server.player.VoicePlayer

fun CommandSender.asPlayer() = this as? Player

fun MinecraftCommandSource.sendTranslatable(key: String, vararg args: Any?) = this.sendMessage(
    MinecraftTextComponent.translatable(key, *args)
)

fun CommandSender.asVoicePlayer(voiceServer: PlasmoVoiceServer): VoicePlayer? {
    return this.asPlayer()?.let { voiceServer.playerManager.getPlayerById(it.uniqueId).orElse(null) }
}