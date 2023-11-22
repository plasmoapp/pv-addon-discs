package su.plo.voice.discs.utils.extend

import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType
import su.plo.voice.discs.DiscsPlugin

fun ItemStack.isCustomDisc(plugin: DiscsPlugin) = this
    .takeIf { this.type.isRecord }
    ?.itemMeta
    ?.persistentDataContainer
    ?.let { it.has(plugin.identifierKey, PersistentDataType.STRING) || it.has(plugin.oldIdentifierKey, PersistentDataType.STRING) }
    ?: false

fun ItemStack.customDiscIdentifier(plugin: DiscsPlugin): String? =
    this.takeIf { isCustomDisc(plugin) }
        ?.itemMeta
        ?.persistentDataContainer
        ?.let {
            it.get(plugin.identifierKey, PersistentDataType.STRING) ?:
            it.get(plugin.oldIdentifierKey, PersistentDataType.STRING)
        }
