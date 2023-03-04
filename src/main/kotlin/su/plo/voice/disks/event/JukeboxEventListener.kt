package su.plo.voice.disks.event

import com.sedmelluq.discord.lavaplayer.tools.FriendlyException
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.Action
import org.bukkit.event.player.PlayerInteractEvent
import su.plo.lib.api.server.world.ServerPos3d
import su.plo.voice.disks.DisksPlugin
import kotlinx.coroutines.*
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.TextComponent
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.block.Block
import org.bukkit.event.EventPriority
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.entity.EntityExplodeEvent
import org.bukkit.persistence.PersistentDataType
import su.plo.lib.api.chat.MinecraftTextComponent
import su.plo.lib.api.chat.MinecraftTextStyle
import su.plo.voice.disks.utils.extend.*
import java.util.concurrent.ConcurrentHashMap

class JukeboxEventListener(
    private val plugin: DisksPlugin
): Listener {

    private val jobByBlock: MutableMap<Block, Job> = ConcurrentHashMap()

    @EventHandler(priority = EventPriority.HIGHEST)
    fun onDiscInsert(event: PlayerInteractEvent) {

        if (event.action != Action.RIGHT_CLICK_BLOCK) return

        val block = event.clickedBlock ?: return

        val jukebox = block.asJukebox() ?: return

        if (jukebox.isPlaying) return

        val item = event.item?.takeIf { it.type.isRecord } ?: return

        if (!event.player.hasPermission("pv.addon.disks.play")) return

        val identifier = item?.itemMeta
            ?.persistentDataContainer
            ?.get(plugin.identifierKey, PersistentDataType.STRING)
            ?: return

        val voicePlayer = event.player.asVoicePlayer(plugin.voiceServer) ?: return

        val track = try {
            plugin.audioPlayerManager.getTrack(identifier)
        } catch (e: FriendlyException) {
            block.asJukebox()?.eject()
            voicePlayer.instance.sendActionBar(
                MinecraftTextComponent.translatable("pv.addon.disks.actionbar.track_not_found", e.message)
                    .withStyle(MinecraftTextStyle.GOLD)
            )
            return
        }

        val trackName = item.itemMeta
            ?.lore()
            ?.getOrNull(0)
            ?.let { it as? TextComponent }
            ?.content()
            ?: track.info.title

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

        source.setName(trackName)

        val job = plugin.audioPlayerManager.startTrackJob(track, source)

        jobByBlock[block]?.cancel()
        jobByBlock[block] = job

        val actionbarMessage = MinecraftTextComponent.translatable(
            "pv.addon.disks.actionbar.playing", trackName
        )

        block.world.getNearbyPlayers(block.location, plugin.addonConfig.jukeboxDistance.toDouble())
            .map { it.asVoicePlayer(plugin.voiceServer) }
            .forEach { it?.sendAnimatedActionBar(actionbarMessage) }
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

