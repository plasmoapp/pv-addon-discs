package su.plo.voice.discs.command.subcommand

import org.bukkit.command.CommandSender
import su.plo.lib.api.server.permission.PermissionDefault
import su.plo.voice.discs.command.CommandHandler
import su.plo.voice.discs.utils.extend.asPlayer
import su.plo.voice.discs.utils.extend.asVoicePlayer
import su.plo.voice.discs.utils.extend.sendTranslatable
import su.plo.voice.groups.command.SubCommand

class EraseCommand(handler: CommandHandler) : SubCommand(handler) {

    override val name = "erase"

    override val permissions = listOf(
        "erase" to PermissionDefault.OP
    )

    override fun execute(source: CommandSender, arguments: Array<out String>) {

        val voicePlayer = source.asPlayer()?.asVoicePlayer(handler.plugin.voiceServer) ?: return

        if (!voicePlayer.instance.hasPermission("pv.addon.discs.erase")) {
            voicePlayer.instance.sendTranslatable("pv.addon.discs.error.no_permission")
            return
        }

        val player = source.asPlayer() ?: run {
            voicePlayer.instance.sendTranslatable("pv.error.player_only_command")
            return
        }

        val item = player.inventory.itemInMainHand
            .takeIf { it.type.isRecord && it.hasItemMeta() }
            ?: run {
                voicePlayer.instance.sendTranslatable("pv.addon.discs.error.erase_wrong_item")
                return
            }

        val meta = source.server.itemFactory.getItemMeta(item.type)

        item.itemMeta = meta

        voicePlayer.instance.sendTranslatable("pv.addon.discs.success.erase")
    }

    override fun checkCanExecute(sender: CommandSender): Boolean =
        sender.hasPermission("pv.addon.discs.erase")
}
