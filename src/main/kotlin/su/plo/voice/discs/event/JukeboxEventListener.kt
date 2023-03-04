package su.plo.voice.discs.event

import com.sedmelluq.discord.lavaplayer.tools.FriendlyException
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.Action
import org.bukkit.event.player.PlayerInteractEvent
import su.plo.lib.api.server.world.ServerPos3d
import su.plo.voice.discs.DiscsPlugin
import kotlinx.coroutines.*
import net.kyori.adventure.text.TextComponent
import org.bukkit.block.Block
import org.bukkit.event.EventPriority
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.entity.EntityExplodeEvent
import org.bukkit.persistence.PersistentDataType
import su.plo.lib.api.chat.MinecraftTextComponent
import su.plo.lib.api.chat.MinecraftTextStyle
import su.plo.voice.discs.utils.extend.*
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ExecutionException

class JukeboxEventListener(
    private val plugin: DiscsPlugin
): Listener {

    private val jobByBlock: MutableMap<Block, Job> = ConcurrentHashMap()

    @EventHandler(priority = EventPriority.HIGHEST)
    fun onDiscInsert(event: PlayerInteractEvent) {

        if (event.action != Action.RIGHT_CLICK_BLOCK) return

        val block = event.clickedBlock ?: return

        val jukebox = block.asJukebox() ?: return

        if (jukebox.isPlaying) return

        val item = event.item?.takeIf { it.type.isRecord } ?: return

        val voicePlayer = event.player.asVoicePlayer(plugin.voiceServer) ?: return

        if (!voicePlayer.instance.hasPermission("pv.addon.discs.play")) return

        val identifier = item?.itemMeta
            ?.persistentDataContainer
            ?.get(plugin.identifierKey, PersistentDataType.STRING)
            ?: return

        val track = try {
            plugin.audioPlayerManager.getTrack(identifier)
        } catch (e: ExecutionException) {
            val message = when (e.cause) {
                is FriendlyException -> (e.cause as FriendlyException).message
                else -> e.message
            }
            voicePlayer.instance.sendActionBar(
                MinecraftTextComponent.translatable("pv.addon.discs.actionbar.track_not_found", message)
                    .withStyle(MinecraftTextStyle.GOLD)
            )
            block.asJukebox()?.eject()
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
            "pv.addon.discs.actionbar.playing", trackName
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

