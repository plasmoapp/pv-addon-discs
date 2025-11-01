package su.plo.voice.discs.command.subcommand

import kotlinx.coroutines.future.await
import kotlinx.coroutines.launch
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextDecoration
import org.bukkit.Bukkit
import org.bukkit.command.CommandSender
import org.bukkit.enchantments.Enchantment
import org.bukkit.inventory.ItemFlag
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType
import org.bukkit.plugin.java.JavaPlugin
import org.koin.core.component.inject
import su.plo.slib.api.chat.component.McTextComponent
import su.plo.slib.api.permission.PermissionDefault
import su.plo.voice.api.server.player.VoicePlayer
import su.plo.voice.discs.AddonConfig
import su.plo.voice.discs.AddonKeys
import su.plo.voice.discs.GoatHornManager
import su.plo.voice.discs.command.SubCommand
import su.plo.voice.discs.item.DiscHelper
import su.plo.voice.discs.item.GoatHornHelper
import su.plo.voice.discs.utils.extend.asPlayer
import su.plo.voice.discs.utils.extend.asVoicePlayer
import su.plo.voice.discs.utils.extend.forbidGrindstone
import su.plo.voice.discs.utils.extend.getMinecraftVersionInt
import su.plo.voice.discs.utils.extend.hasIdentifier
import su.plo.voice.discs.utils.extend.isCustomDisc
import su.plo.voice.discs.utils.extend.render
import su.plo.voice.discs.utils.extend.sendTranslatable
import su.plo.voice.discs.utils.extend.suspendSync
import su.plo.voice.discs.utils.extend.toPlainText

class BurnCommand : SubCommand() {

    private val plugin: JavaPlugin by inject()
    private val discHelper: DiscHelper by inject()
    private val hornHelper: GoatHornHelper by inject()
    private val hornManager: GoatHornManager by inject()

    override val name = "burn"

    override val permissions = listOf(
        "burn" to PermissionDefault.OP,
        "burn.burnable_check_bypass" to PermissionDefault.OP,
    )

    override fun suggest(source: CommandSender, arguments: Array<out String>): List<String> {
        val voicePlayer = source.asPlayer()?.asVoicePlayer(voiceServer) ?: return listOf()

        if (!voicePlayer.instance.hasPermission("pv.addon.discs.burn")) return listOf()

        if (arguments.size < 2) return listOf()
        if (arguments.size == 2) return listOf(
            McTextComponent.translatable("pv.addon.discs.arg.url")
                .render(voicePlayer, voiceServer)
                .toPlainText()
        )
        return listOf(
            McTextComponent.translatable("pv.addon.discs.arg.label")
                .render(voicePlayer, voiceServer)
                .toPlainText()
        )
    }

    private fun checkBurnable(voicePlayer: VoicePlayer, item: ItemStack): Boolean {

        if (!discHelper.isRecord(item) && !(config.goatHorn.enabled && item.type.name == "GOAT_HORN")) {
            voicePlayer.instance.sendTranslatable("pv.addon.discs.error.not_a_record")
            return false
        }

        if (
            config.burnableTag.requireBurnableTag &&
            (
                !item.itemMeta.persistentDataContainer.has(AddonKeys.burnableKey, PersistentDataType.BYTE) &&
                !voicePlayer.instance.hasPermission("pv.addon.discs.burn.burnable_check_bypass")
            )
        ) {
            voicePlayer.instance.sendTranslatable("pv.addon.discs.error.not_burnable")
            return false
        }

        return true
    }

    override fun execute(sender: CommandSender, arguments: Array<out String>) { scope.launch {

        val voicePlayer = sender.asPlayer()?.asVoicePlayer(voiceServer) ?: return@launch

        if (!voicePlayer.instance.hasPermission("pv.addon.discs.burn")) {
            voicePlayer.instance.sendTranslatable("pv.addon.discs.error.no_permission")
            return@launch
        }

        val identifier = arguments.getOrNull(1) ?: run {
            voicePlayer.instance.sendTranslatable("pv.addon.discs.usage.burn")
            return@launch
        }

        val track = try {
            audioPlayerManager.getTrack(identifier).await()
        } catch (e: Exception) {
            voicePlayer.instance.sendTranslatable("pv.addon.discs.error.get_track_fail", e.message ?: "Unexpected error")
            debugLogger.log("Failed to load track", e)
            return@launch
        }

        val name = arguments.drop(2)
            .joinToString(" ")
            .ifEmpty { track.info.title }

        val player = sender.asPlayer() ?: run {
            voicePlayer.instance.sendTranslatable("pv.error.player_only_command")
            return@launch
        }

        val item = plugin.suspendSync(player) { player.inventory.itemInMainHand }

        if (!checkBurnable(voicePlayer, item)) return@launch

        val isGoatHorn = item.type.name == "GOAT_HORN"

        if (isGoatHorn && !hornManager.canPlay(track)) {
            voicePlayer.instance.sendTranslatable(
                "pv.addon.discs.error.horn_too_long", config.goatHorn.maxDurationSeconds
            )
            return@launch
        }

        plugin.suspendSync(player.location) {
            if (Bukkit.getServer().getMinecraftVersionInt() >= 12103) {
                discHelper.showSongTooltip(item, false)
            }

            val hasIdentifier = item.hasIdentifier()

            item.editMeta { meta ->
                meta.addItemFlags(*ItemFlag.values())

                meta.persistentDataContainer.set(
                    AddonKeys.identifierKey,
                    PersistentDataType.STRING,
                    identifier
                )

                if (config.addGlintToCustomDiscs) {
                    meta.forbidGrindstone()
                    meta.addEnchant(Enchantment.MENDING, 1, false)
                }

                if (isGoatHorn && !hasIdentifier) {
                    hornHelper.getInstrument(item)
                        .takeIf { it.isNotEmpty() }
                        ?.let {
                            meta.persistentDataContainer.set(
                                AddonKeys.instrumentKey,
                                PersistentDataType.STRING,
                                it
                            )
                        }
                }

                val loreName = Component.text()
                    .content(name)
                    .decoration(TextDecoration.ITALIC, false)
                    .color(NamedTextColor.GRAY)
                    .build()

                when (config.burnLoreMethod) {
                    AddonConfig.LoreMethod.REPLACE -> {
                        meta.lore(listOf(loreName))
                    }

                    AddonConfig.LoreMethod.APPEND -> {
                        val currentLore = meta.lore()?.let {
                            if (item.isCustomDisc(discHelper)) {
                                it.subList(0, it.size - 1)
                            } else {
                                it
                            }
                        } ?: emptyList()
                        meta.lore(currentLore + listOf(loreName))
                    }

                    AddonConfig.LoreMethod.DISABLE -> {} // do nothing
                }
            }

            if (isGoatHorn && !hasIdentifier) {
                hornHelper.setEmptyInstrument(item)
            }

            voicePlayer.instance.sendTranslatable("pv.addon.discs.success.burn", name)
        }
    }}

    override fun checkCanExecute(sender: CommandSender): Boolean = sender.hasPermission("pv.addon.discs.burn")
}
