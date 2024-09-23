package su.plo.voice.discs.utils

import org.bukkit.Material

object MaterialUtil {

    val itemMusicDiscs: List<Material> =
        Material.values()
            .filter { it.name.startsWith("MUSIC_DISC_") }
}
