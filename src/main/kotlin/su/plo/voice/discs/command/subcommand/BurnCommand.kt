package su.plo.voice.discs.command.subcommand

import com.sedmelluq.discord.lavaplayer.tools.FriendlyException
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextDecoration
import org.bukkit.command.CommandSender
import org.bukkit.enchantments.Enchantment
import org.bukkit.inventory.ItemFlag
import org.bukkit.persistence.PersistentDataType
import su.plo.lib.api.server.permission.PermissionDefault
import su.plo.voice.discs.command.CommandHandler
import su.plo.voice.discs.utils.extend.asPlayer
import su.plo.voice.discs.utils.extend.asVoicePlayer
import su.plo.voice.discs.utils.extend.sendTranslatable
import su.plo.voice.groups.command.SubCommand
import java.util.concurrent.ExecutionException

class BurnCommand(handler: CommandHandler) : SubCommand(handler) {

    override val name = "burn"

    override val permissions = listOf(
        "burn" to PermissionDefault.OP
    )

    override fun suggest(source: CommandSender, arguments: Array<out String>): List<String> {
        val voicePlayer = source.asPlayer()?.asVoicePlayer(handler.plugin.voiceServer) ?: return listOf()
        if (arguments.size < 2 ) return listOf()
        if (arguments.size == 2) return listOf(
            handler.getTranslationStringByKey("pv.addon.discs.arg.url", voicePlayer.instance)
        )
        return listOf(
            handler.getTranslationStringByKey("pv.addon.discs.arg.label", voicePlayer.instance)
        )
    }

    override fun execute(sender: CommandSender, arguments: Array<out String>) {

        val voicePlayer = sender.asPlayer()?.asVoicePlayer(handler.plugin.voiceServer) ?: return

        if (!voicePlayer.instance.hasPermission("pv.addon.discs.burn")) {
            voicePlayer.instance.sendTranslatable("pv.addon.discs.error.no_permission")
            return
        }

        val identifier = arguments.getOrNull(1) ?: run {
            voicePlayer.instance.sendTranslatable("pv.addon.discs.usage.burn")
            return
        }

        val track = try {
            handler.plugin.audioPlayerManager.getTrack(identifier)
        } catch (e: ExecutionException) {
            val message = when (e.cause) {
                is FriendlyException -> (e.cause as FriendlyException).message
                else -> e.message
            }
            voicePlayer.instance.sendTranslatable("pv.addon.discs.error.get_track_fail", message)
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
                voicePlayer.instance.sendTranslatable("pv.addon.discs.error.not_a_record")
                return
            }}
            .itemMeta

        meta.addItemFlags(*ItemFlag.values())

        meta.persistentDataContainer.set(
            handler.plugin.identifierKey,
            PersistentDataType.STRING,
            identifier
        )

        if (handler.plugin.addonConfig.addGlintToCustomDiscs) {
            meta.addEnchant(Enchantment.MENDING, 1, false)
        }

        val loreName = Component.text()
            .content(name)
            .decoration(TextDecoration.ITALIC, false)
            .color(NamedTextColor.GRAY)
            .build()

        meta.lore(listOf(loreName))

        player.inventory.itemInMainHand.itemMeta = meta

        voicePlayer.instance.sendTranslatable("pv.addon.discs.success.burn", name)
    }

    override fun checkCanExecute(sender: CommandSender): Boolean = sender.hasPermission("pv.addon.discs.burn")
}
