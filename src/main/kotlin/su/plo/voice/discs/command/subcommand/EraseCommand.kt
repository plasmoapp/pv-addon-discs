package su.plo.voice.discs.command.subcommand

import org.bukkit.command.CommandSender
import org.bukkit.enchantments.Enchantment
import org.bukkit.inventory.ItemFlag
import su.plo.lib.api.server.permission.PermissionDefault
import su.plo.voice.discs.command.CommandHandler
import su.plo.voice.discs.command.SubCommand
import su.plo.voice.discs.utils.extend.asPlayer
import su.plo.voice.discs.utils.extend.asVoicePlayer
import su.plo.voice.discs.utils.extend.sendTranslatable

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

        item.editMeta { meta ->
            meta.removeItemFlags(*ItemFlag.values())
            meta.persistentDataContainer.remove(handler.plugin.identifierKey)

            if (handler.plugin.addonConfig.addGlintToCustomDiscs) {
                handler.plugin.allowGrindstone(meta)
                meta.removeEnchant(Enchantment.MENDING)
            }

            meta.lore(null)
        }

        voicePlayer.instance.sendTranslatable("pv.addon.discs.success.erase")
    }

    override fun checkCanExecute(sender: CommandSender): Boolean =
        sender.hasPermission("pv.addon.discs.erase")
}
