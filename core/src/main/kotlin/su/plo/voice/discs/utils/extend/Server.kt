package su.plo.voice.discs.utils.extend

import org.bukkit.Bukkit
import org.bukkit.Server

fun Server.getMinecraftVersionInt(): Int {
    val versions = Bukkit.getVersion()
        .substring(version.lastIndexOf(" ") + 1, version.length - 1)
        .split(".")
        .mapNotNull { it.toIntOrNull() }
        .let {
            listOf(
                it.getOrNull(0) ?: 0,
                it.getOrNull(1) ?: 0,
                it.getOrNull(2) ?: 0
            )
        }

    return versions[0] * 10000 + versions[1] * 100 + versions[2]
}