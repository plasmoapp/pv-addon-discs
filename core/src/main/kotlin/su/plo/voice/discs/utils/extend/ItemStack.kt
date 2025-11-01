package su.plo.voice.discs.utils.extend

import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.ItemMeta
import org.bukkit.persistence.PersistentDataType
import su.plo.voice.discs.AddonKeys
import su.plo.voice.discs.item.DiscHelper

fun ItemMeta.forbidGrindstone() {
    persistentDataContainer.set(AddonKeys.forbidGrindstoneKey, PersistentDataType.BYTE, 1)
}

fun ItemMeta.allowGrindstone() {
    persistentDataContainer.remove(AddonKeys.forbidGrindstoneKey)
}

fun ItemStack.isCustomDisc(discHelper: DiscHelper) = this
    .takeIf { discHelper.isRecord(this) }
    ?.hasIdentifier()
    ?: false

fun ItemStack.hasIdentifier() = this
    .itemMeta
    ?.persistentDataContainer
    ?.let {
        it.has(AddonKeys.identifierKey, PersistentDataType.STRING) ||
                it.has(AddonKeys.oldIdentifierKey, PersistentDataType.STRING)
    }
    ?: false

fun ItemStack.customDiscIdentifier(discHelper: DiscHelper): String? =
    this.takeIf { discHelper.isRecord(this) }
        ?.identifier()

fun ItemStack.identifier(): String? =
    this.takeIf { hasIdentifier() }
        ?.itemMeta
        ?.persistentDataContainer
        ?.let {
            it.get(AddonKeys.identifierKey, PersistentDataType.STRING) ?:
            it.get(AddonKeys.oldIdentifierKey, PersistentDataType.STRING)
        }
