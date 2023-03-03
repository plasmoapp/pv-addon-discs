package su.plo.voice.disks.command.subcommand

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextDecoration
import org.bukkit.command.CommandSender
import org.bukkit.enchantments.Enchantment
import org.bukkit.inventory.ItemFlag
import org.bukkit.persistence.PersistentDataType
import su.plo.lib.api.server.permission.PermissionDefault
import su.plo.voice.disks.command.CommandHandler
import su.plo.voice.disks.utils.extend.asPlayer
import su.plo.voice.disks.utils.extend.asVoicePlayer
import su.plo.voice.disks.utils.extend.sendTranslatable
import su.plo.voice.groups.command.SubCommand

class BurnCommand(handler: CommandHandler) : SubCommand(handler) {

    override val name = "burn"

    override val permissions = listOf(
        "burn" to PermissionDefault.OP
    )

    override fun suggest(source: CommandSender, arguments: Array<out String>): List<String> {
        val voicePlayer = source.asPlayer()?.asVoicePlayer(handler.plugin.voiceServer) ?: return listOf()
        if (arguments.size < 2 ) return listOf()
        if (arguments.size == 2) return listOf(
            handler.getTranslationStringByKey("pv.addon.disks.arg.url", voicePlayer.instance)
        )
        return listOf(
            handler.getTranslationStringByKey("pv.addon.disks.arg.label", voicePlayer.instance)
        )
    }

    override fun execute(sender: CommandSender, arguments: Array<out String>) {

        val voicePlayer = sender.asPlayer()?.asVoicePlayer(handler.plugin.voiceServer) ?: return

        val identifier = arguments.getOrNull(1) ?: run {
            voicePlayer.instance.sendTranslatable("pv.addon.disks.usage.burn")
            return
        }

        val track = handler.plugin.audioPlayerManager.getTrack(identifier) ?: run {
            voicePlayer.instance.sendTranslatable("pv.addon.disks.error.get_track_fail")
            return
        }

        val name = arguments.drop(2)
            .joinToString(" ")
            .ifEmpty { track.info.title }

        val player = sender.asPlayer() ?: run {
            voicePlayer.instance.sendTranslatable("pv.error.player_only_command")
            return
        }

        val meta = player.inventory.itemInMainHand
            .also { if (!it.type.isRecord) {
                voicePlayer.instance.sendTranslatable("pv.addon.disks.error.not_a_record")
                return
            }}
            .itemMeta

        meta.addItemFlags(*ItemFlag.values())

        meta.persistentDataContainer.set(
            handler.plugin.identifierKey,
            PersistentDataType.STRING,
            identifier
        )

        if (handler.plugin.addonConfig.addGlintToCustomDisks) {
            meta.addEnchant(Enchantment.MENDING, 1, false)
        }

        val loreName = Component.text()
            .content(name)
            .decoration(TextDecoration.ITALIC, false)
            .color(NamedTextColor.GRAY)
            .build()

        meta.lore(listOf(loreName))

        player.inventory.itemInMainHand.itemMeta = meta

        voicePlayer.instance.sendTranslatable("pv.addon.disks.success.burn", name)
    }

    override fun checkCanExecute(sender: CommandSender): Boolean = sender.hasPermission("pv.addon.disks.burn")
}
