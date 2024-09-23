package su.plo.voice.discs.utils.extend

import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.ItemMeta
import org.bukkit.persistence.PersistentDataType
import su.plo.voice.discs.AddonKeys

context(AddonKeys)
fun ItemMeta.forbidGrindstone() {
    persistentDataContainer.set(forbidGrindstoneKey, PersistentDataType.BYTE, 1)
}

context(AddonKeys)
fun ItemMeta.allowGrindstone() {
    persistentDataContainer.remove(forbidGrindstoneKey)
}

context(AddonKeys)
fun ItemStack.isCustomDisc() = this
    .takeIf { this.type.isRecord }
    ?.hasIdentifier()
    ?: false

context(AddonKeys)
fun ItemStack.hasIdentifier() = this
    .itemMeta
    ?.persistentDataContainer
    ?.let { it.has(identifierKey, PersistentDataType.STRING) || it.has(oldIdentifierKey, PersistentDataType.STRING) }
    ?: false

context(AddonKeys)
fun ItemStack.customDiscIdentifier(): String? =
    this.takeIf { this.type.isRecord }
        ?.identifier()

context(AddonKeys)
fun ItemStack.identifier(): String? =
    this.takeIf { hasIdentifier() }
        ?.itemMeta
        ?.persistentDataContainer
        ?.let {
            it.get(identifierKey, PersistentDataType.STRING) ?:
            it.get(oldIdentifierKey, PersistentDataType.STRING)
        }