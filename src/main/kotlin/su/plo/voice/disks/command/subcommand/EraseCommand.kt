package su.plo.voice.disks.command.subcommand

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextDecoration
import org.bukkit.NamespacedKey
import org.bukkit.Server
import org.bukkit.command.CommandSender
import org.bukkit.inventory.ItemFactory
import org.bukkit.inventory.ItemFlag
import org.bukkit.persistence.PersistentDataType
import su.plo.lib.api.server.permission.PermissionDefault
import su.plo.voice.disks.command.CommandHandler
import su.plo.voice.disks.utils.extend.asPlayer
import su.plo.voice.disks.utils.extend.sendTranslatable
import su.plo.voice.groups.command.SubCommand

class EraseCommand(handler: CommandHandler) : SubCommand(handler) {

    override val name = "erase"

    override val permissions = listOf(
        "erase" to PermissionDefault.OP
    )

    override fun execute(source: CommandSender, arguments: Array<out String>) {

        val player = source.asPlayer() ?: run {
            source.sendTranslatable("pv.error.player_only_command")
            return
        }

        val item = player.inventory.itemInMainHand
            .takeIf { it.type.isRecord && it.hasItemMeta() }
            ?: run {
                source.sendTranslatable("pv.addon.disks.error.erase_wrong_item")
                return
            }

        val meta = source.server.itemFactory.getItemMeta(item.type)

        item.itemMeta = meta

        source.sendTranslatable("pv.addon.disks.success.erase")
    }

    override fun checkCanExecute(sender: CommandSender): Boolean =
        sender.hasPermission("pv.addon.disks.erase")
}
