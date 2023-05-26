package su.plo.voice.discs.command.subcommand

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextDecoration
import org.bukkit.command.CommandSender
import org.bukkit.enchantments.Enchantment
import org.bukkit.inventory.ItemFlag
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType
import su.plo.lib.api.server.permission.PermissionDefault
import su.plo.voice.api.server.player.VoicePlayer
import su.plo.voice.discs.command.CommandHandler
import su.plo.voice.discs.command.SubCommand
import su.plo.voice.discs.utils.extend.asPlayer
import su.plo.voice.discs.utils.extend.asVoicePlayer
import su.plo.voice.discs.utils.extend.friendlyMessage
import su.plo.voice.discs.utils.extend.sendTranslatable
import su.plo.voice.discs.utils.suspendSync
import java.util.concurrent.ExecutionException

class BurnCommand(handler: CommandHandler) : SubCommand(handler) {

    override val name = "burn"

    override val permissions = listOf(
        "burn" to PermissionDefault.OP,
        "burn.burnable_check_bypass" to PermissionDefault.OP,
    )

    private val scope = CoroutineScope(Dispatchers.Default)

    override fun suggest(source: CommandSender, arguments: Array<out String>): List<String> {

        val voicePlayer = source.asPlayer()?.asVoicePlayer(handler.plugin.voiceServer) ?: return listOf()

        if (!voicePlayer.instance.hasPermission("pv.addon.discs.burn")) return listOf()

        if (arguments.size < 2 ) return listOf()
        if (arguments.size == 2) return listOf(
            handler.getTranslationStringByKey("pv.addon.discs.arg.url", voicePlayer.instance)
        )
        return listOf(
            handler.getTranslationStringByKey("pv.addon.discs.arg.label", voicePlayer.instance)
        )
    }

    private fun checkBurnable(voicePlayer: VoicePlayer, item: ItemStack): Boolean {

        if (!item.type.isRecord) {
            voicePlayer.instance.sendTranslatable("pv.addon.discs.error.not_a_record")
            return false
        }

        if (
            handler.plugin.addonConfig.burnableTag.requireBurnableTag &&
            (
                !item.itemMeta.persistentDataContainer.has(handler.plugin.burnableKey) &&
                !voicePlayer.instance.hasPermission("pv.addon.discs.burn.burnable_check_bypass")
            )
        ) {
            voicePlayer.instance.sendTranslatable("pv.addon.discs.error.not_burnable")
            return false
        }

        return true
    }

    override fun execute(sender: CommandSender, arguments: Array<out String>) { scope.launch {

        val voicePlayer = sender.asPlayer()?.asVoicePlayer(handler.plugin.voiceServer) ?: return@launch

        if (!voicePlayer.instance.hasPermission("pv.addon.discs.burn")) {
            voicePlayer.instance.sendTranslatable("pv.addon.discs.error.no_permission")
            return@launch
        }

        val identifier = arguments.getOrNull(1) ?: run {
            voicePlayer.instance.sendTranslatable("pv.addon.discs.usage.burn")
            return@launch
        }

        val track = try {
            handler.plugin.audioPlayerManager.getTrack(identifier)
        } catch (e: ExecutionException) {
            voicePlayer.instance.sendTranslatable("pv.addon.discs.error.get_track_fail", e.friendlyMessage)
            return@launch
        }

        val name = arguments.drop(2)
            .joinToString(" ")
            .ifEmpty { track.info.title }

        val player = sender.asPlayer() ?: run {
            voicePlayer.instance.sendTranslatable("pv.error.player_only_command")
            return@launch
        }

        val item = suspendSync(handler.plugin) { player.inventory.itemInMainHand }

        if (!checkBurnable(voicePlayer, item)) return@launch

        val meta = item.itemMeta

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

        suspendSync(handler.plugin) {
            val item = player.inventory.itemInMainHand
            if (!checkBurnable(voicePlayer, item)) return@suspendSync
            item.itemMeta = meta
            voicePlayer.instance.sendTranslatable("pv.addon.discs.success.burn", name)
        }
    }}

    override fun checkCanExecute(sender: CommandSender): Boolean = sender.hasPermission("pv.addon.discs.burn")
}
