package su.plo.voice.disks.event

import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.Action
import org.bukkit.event.player.PlayerInteractEvent
import su.plo.lib.api.server.world.ServerPos3d
import su.plo.voice.disks.TestPlugin
import kotlinx.coroutines.*
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.TextComponent
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextColor
import org.bukkit.block.Block
import org.bukkit.event.EventPriority
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.entity.EntityExplodeEvent
import org.bukkit.persistence.PersistentDataType
import su.plo.voice.disks.utils.extend.asJukebox
import su.plo.voice.disks.utils.extend.isJukebox
import java.util.concurrent.ConcurrentHashMap

class JukeboxEventListener(
    private val plugin: TestPlugin
): Listener {

    private val jobByBlock: MutableMap<Block, Job> = ConcurrentHashMap()

    @EventHandler(priority = EventPriority.HIGHEST)
    fun onDiscInsert(event: PlayerInteractEvent) {

        if (event.action != Action.RIGHT_CLICK_BLOCK) return

        val block = event.clickedBlock ?: return

        val jukebox = block.asJukebox() ?: return

        if (jukebox.isPlaying) return

        val item = event.item?.takeIf { it.type.isRecord } ?: return

        val identifier = item?.itemMeta
            ?.persistentDataContainer
            ?.get(plugin.identifierKey, PersistentDataType.STRING)
            ?: return

        val world = plugin.voiceServer.minecraftServer.getWorld(block.world)

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

        val track = plugin.audioPlayerManager.getTrack(identifier) ?: run {
            block.asJukebox()?.eject()
            event.player.sendActionBar(
                Component.translatable("pv.addon.disks.actionbar.track_not_found")
                    .color(NamedTextColor.RED)
            )
            return
        }

        val job = plugin.audioPlayerManager.startTrackJob(track, source)

        jobByBlock[block]?.cancel()
        jobByBlock[block] = job

        val trackName = item.itemMeta
            ?.lore()
            ?.getOrNull(0)
            ?.let { it as? TextComponent }
            ?.content()
            ?: track.info.title

        val actionbarMessage = Component.translatable(
            "pv.addon.disks.actionbar.playing",
            Component.text(trackName)
        ).color(NamedTextColor.GOLD)

        block.world.getNearbyPlayers(block.location, plugin.addonConfig.jukeboxDistance.toDouble())
            .forEach { it.sendActionBar(actionbarMessage) }
    }

    @EventHandler
    fun onDiskEject(event: PlayerInteractEvent) {
        if (event.action != Action.RIGHT_CLICK_BLOCK) return

        val block = event.clickedBlock ?: return

        block.asJukebox()?.takeIf { it.isPlaying } ?: return

        jobByBlock[block]?.cancel()
    }

    @EventHandler
    fun onJukeboxBreak(event: BlockBreakEvent) {
        event.block
            .takeIf { it.isJukebox() }
            ?.let { jobByBlock[it] }
            ?.cancel()
    }

    @EventHandler
    fun onJukeboxExplode(event: EntityExplodeEvent) {
        event.blockList()
            .filter { it.isJukebox() }
            .forEach { jobByBlock[it]?.cancel() }
    }
}

