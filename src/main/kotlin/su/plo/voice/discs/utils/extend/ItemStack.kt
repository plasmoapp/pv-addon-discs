package su.plo.voice.discs.utils.extend

import org.bukkit.inventory.ItemStack
import su.plo.voice.discs.DiscsPlugin

fun ItemStack.isCustomDisc(plugin: DiscsPlugin) = this
    .takeIf { this.type.isRecord }
    ?.itemMeta
    ?.persistentDataContainer
    ?.let { it.has(plugin.identifierKey) || it.has(plugin.oldIdentifierKey) }
    ?: false