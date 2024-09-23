package su.plo.voice.discs

import org.bukkit.NamespacedKey
import org.bukkit.plugin.java.JavaPlugin

data class AddonKeys private constructor(
    // PersistentDataType.STRING
    val identifierKey: NamespacedKey,
    // PersistentDataType.BYTE 0 or 1 representing boolean
    val burnableKey: NamespacedKey,
    // PersistentDataType.BYTE 0 or 1 representing boolean
    val forbidGrindstoneKey: NamespacedKey,
    // PersistentDataType.STRING
    val instrumentKey: NamespacedKey,
    // PersistentDataType.STRING
    val oldIdentifierKey: NamespacedKey = NamespacedKey("pv-addon-disks", "identifier"),
) {
    companion object {
        fun of(plugin: JavaPlugin): AddonKeys =
            AddonKeys(
                NamespacedKey(plugin, "identifier"),
                NamespacedKey(plugin, "burnable"),
                NamespacedKey(plugin, "forbid_grindstone"),
                NamespacedKey(plugin, "instrument_key"),
            )
    }
}