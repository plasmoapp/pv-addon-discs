package su.plo.voice.discs.command.subcommand

import org.bukkit.Bukkit
import org.bukkit.command.CommandSender
import org.bukkit.enchantments.Enchantment
import org.bukkit.inventory.ItemFlag
import org.bukkit.persistence.PersistentDataType
import org.koin.core.component.inject
import su.plo.slib.api.permission.PermissionDefault
import su.plo.voice.discs.AddonConfig
import su.plo.voice.discs.AddonKeys
import su.plo.voice.discs.api.PlayerEraseEvent
import su.plo.voice.discs.command.SubCommand
import su.plo.voice.discs.item.DiscHelper
import su.plo.voice.discs.item.GoatHornHelper
import su.plo.voice.discs.utils.extend.allowGrindstone
import su.plo.voice.discs.utils.extend.asPlayer
import su.plo.voice.discs.utils.extend.asVoicePlayer
import su.plo.voice.discs.utils.extend.getMinecraftVersionInt
import su.plo.voice.discs.utils.extend.sendTranslatable

class EraseCommand : SubCommand() {

    private val discHelper: DiscHelper by inject()
    private val goatHornHelper: GoatHornHelper by inject()

    override val name = "erase"

    override val permissions = listOf(
        "erase" to PermissionDefault.OP
    )

    override fun execute(sender: CommandSender, arguments: Array<out String>) {

        val voicePlayer = sender.asPlayer()?.asVoicePlayer(voiceServer) ?: return

        if (!voicePlayer.instance.hasPermission("pv.addon.discs.erase")) {
            voicePlayer.instance.sendTranslatable("pv.addon.discs.error.no_permission")
            return
        }

        val player = sender.asPlayer() ?: run {
            voicePlayer.instance.sendTranslatable("pv.error.player_only_command")
            return
        }

        val item = player.inventory.itemInMainHand
            .takeIf { (discHelper.isRecord(it) || (config.goatHorn.enabled && it.type.name == "GOAT_HORN")) && it.hasItemMeta() }
            ?: run {
                voicePlayer.instance.sendTranslatable("pv.addon.discs.error.erase_wrong_item")
                return
            }

        if (Bukkit.getServer().getMinecraftVersionInt() >= 12103) {
            val discHelper by inject<DiscHelper>()
            discHelper.showSongTooltip(item, true)
        }

        PlayerEraseEvent(voicePlayer).callEvent()

        item.editMeta { meta ->
            meta.removeItemFlags(*ItemFlag.values())
            meta.persistentDataContainer.remove(AddonKeys.identifierKey)

            if (config.addGlintToCustomDiscs) {
                meta.allowGrindstone()
                meta.removeEnchant(Enchantment.MENDING)
            }

            when (config.burnLoreMethod) {
                AddonConfig.LoreMethod.REPLACE -> {
                    meta.lore(null)
                }

                AddonConfig.LoreMethod.APPEND -> {
                    val currentLore = meta.lore() ?: return@editMeta
                    if (currentLore.isEmpty()) return@editMeta
                    val newLore = currentLore.subList(0, currentLore.size - 1)
                    if (newLore.isEmpty()) {
                        meta.lore(null)
                    } else {
                        meta.lore(newLore)
                    }
                }

                AddonConfig.LoreMethod.DISABLE -> {} // do nothing
            }
        }

        if (item.type.name == "GOAT_HORN") {
            val pdc = item.itemMeta.persistentDataContainer
            pdc.get(AddonKeys.instrumentKey, PersistentDataType.STRING)
                ?.let { instrument ->
                    goatHornHelper.setInstrument(item, instrument)
                }
            pdc.remove(AddonKeys.instrumentKey)
        }

        voicePlayer.instance.sendTranslatable("pv.addon.discs.success.erase")
    }

    override fun checkCanExecute(sender: CommandSender): Boolean =
        sender.hasPermission("pv.addon.discs.erase")
}
