package su.plo.voice.discs

import org.bukkit.NamespacedKey

data object AddonKeys {
    // PersistentDataType.STRING
    val identifierKey: NamespacedKey = createKeyOrThrow("pv-addon-discs:identifier")
    // PersistentDataType.BYTE 0 or 1 representing boolean
    val burnableKey: NamespacedKey = createKeyOrThrow("pv-addon-discs:burnable")
    // PersistentDataType.BYTE 0 or 1 representing boolean
    val forbidGrindstoneKey: NamespacedKey = createKeyOrThrow("pv-addon-discs:forbid_grindstone")
    // PersistentDataType.STRING
    val instrumentKey: NamespacedKey = createKeyOrThrow("pv-addon-discs:instrument_key")
    // PersistentDataType.STRING
    val oldIdentifierKey: NamespacedKey = createKeyOrThrow("pv-addon-disks:identifier")
}

private fun createKeyOrThrow(path: String): NamespacedKey =
    NamespacedKey.fromString(path) ?: throw IllegalArgumentException("Failed to parse NamespacedKey $path")
