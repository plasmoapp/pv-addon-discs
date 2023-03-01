package su.plo.template.event

import org.bukkit.Material
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.block.Action
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.ItemStack
import su.plo.lib.api.server.world.ServerPos3d
import su.plo.template.TestPlugin
import kotlinx.coroutines.*

class JukeboxEventLister(
    val plugin: TestPlugin
): Listener {

    val scope = CoroutineScope(Dispatchers.Default)

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    fun onDiscInsert(event: PlayerInteractEvent) {

        if (event.action != Action.RIGHT_CLICK_BLOCK) return

        val block = event.clickedBlock
            ?.takeIf { it.type == Material.JUKEBOX }
            ?: return

        val item = event.item
            ?.takeIf { it.isMusicDisk() }
            ?: return

        val world = plugin.voiceServer.minecraftServer.worlds.toList()[0]

        val pos = ServerPos3d(
            world,
            block.x.toDouble() + 0.5,
            block.y.toDouble() + 1.5,
            block.z.toDouble() + 0.5
        )

        val source = plugin.voiceServer.sourceManager.createStaticSource(
            plugin,
            pos,
            plugin.sourceLine,
            "opus",
            true
        )

        plugin.audioPlayerManager.playTrack(source, "https://www.youtube.com/watch?v=9HBRXbU9Y5I")
    }
}

val musicDiskMaterials = setOf(
    Material.MUSIC_DISC_11,
    Material.MUSIC_DISC_13,
    Material.MUSIC_DISC_5,
    Material.MUSIC_DISC_BLOCKS,
    Material.MUSIC_DISC_CAT,
    Material.MUSIC_DISC_CHIRP,
    Material.MUSIC_DISC_FAR,
    Material.MUSIC_DISC_MALL,
    Material.MUSIC_DISC_MELLOHI,
    Material.MUSIC_DISC_OTHERSIDE,
    Material.MUSIC_DISC_PIGSTEP,
    Material.MUSIC_DISC_STAL,
    Material.MUSIC_DISC_STRAD,
    Material.MUSIC_DISC_WAIT,
    Material.MUSIC_DISC_WARD,
)

fun ItemStack.isMusicDisk() = musicDiskMaterials.contains(this.type)

