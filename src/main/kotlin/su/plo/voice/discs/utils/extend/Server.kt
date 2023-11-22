package su.plo.voice.discs.utils.extend

import org.bukkit.Server

fun Server.isVersionGreaterOrEqual(minVersion: String): Boolean {
    val currentParts = minecraftVersion.split(".")
    val targetParts = minVersion.split(".")

    for (i in 0 until currentParts.size.coerceAtMost(targetParts.size)) {
        val currentPart = currentParts[i].toInt()
        val targetPart = targetParts[i].toInt()

        if (currentPart < targetPart) {
            return false
        } else if (currentPart > targetPart) {
            return true
        }
    }

    return true
}
