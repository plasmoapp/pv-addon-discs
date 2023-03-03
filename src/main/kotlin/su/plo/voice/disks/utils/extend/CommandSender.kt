package su.plo.voice.disks.utils.extend

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.ComponentLike
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

fun CommandSender.asPlayer() = this as? Player

fun CommandSender.sendTranslatable(key: String, vararg args: ComponentLike) = this.sendMessage(
    Component.translatable(key, *args)
)