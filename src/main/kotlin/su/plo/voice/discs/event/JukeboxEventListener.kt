package su.plo.voice.discs.event

import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.Action
import org.bukkit.event.player.PlayerInteractEvent
import su.plo.lib.api.server.world.ServerPos3d
import su.plo.voice.discs.DiscsPlugin
import kotlinx.coroutines.*
import net.kyori.adventure.text.TextComponent
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.block.Block
import org.bukkit.event.EventPriority
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.entity.EntityExplodeEvent
import org.bukkit.persistence.PersistentDataType
import su.plo.lib.api.chat.MinecraftTextComponent
import su.plo.lib.api.chat.MinecraftTextStyle
import su.plo.voice.discs.utils.extend.*
import su.plo.voice.discs.utils.suspendSync
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ExecutionException

class JukeboxEventListener(
    private val plugin: DiscsPlugin
): Listener {

    private val jobByBlock: MutableMap<Block, Job> = ConcurrentHashMap()

    private val scope = CoroutineScope(Dispatchers.Default)

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    fun onDiscInsert(event: PlayerInteractEvent) {

        if (event.action != Action.RIGHT_CLICK_BLOCK) return

        val block = event.clickedBlock ?: return

        val jukebox = block.asJukebox() ?: return

        if (jukebox.isPlaying) return

        val item = event.item?.takeIf { it.isCustomDisc(plugin) } ?: return

        val voicePlayer = event.player.asVoicePlayer(plugin.voiceServer) ?: return

        if (!voicePlayer.instance.hasPermission("pv.addon.discs.play")) return

        val identifier = item.itemMeta
            ?.persistentDataContainer
            ?.let {
                it.get(plugin.identifierKey, PersistentDataType.STRING) ?:
                it.get(plugin.oldIdentifierKey, PersistentDataType.STRING)
            }
            ?: return

        voicePlayer.instance.sendActionBar(
            MinecraftTextComponent.translatable("pv.addon.discs.actionbar.loading")
                .withStyle(MinecraftTextStyle.YELLOW)
        )

        scope.launch {

        val track = try {
            plugin.audioPlayerManager.getTrack(identifier)
        } catch (e: ExecutionException) {
            voicePlayer.instance.sendActionBar(
                MinecraftTextComponent.translatable("pv.addon.discs.actionbar.track_not_found", e.friendlyMessage())
                    .withStyle(MinecraftTextStyle.RED)
            )
            suspendSync(plugin) { block.asJukebox()?.eject() }
            return@launch
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

        val source = plugin.sourceLine.createStaticSource(pos, true)

        source.setName(trackName)

        val distance = when (plugin.addonConfig.distance.enableBeaconLikeDistance) {
            true -> plugin.addonConfig.distance.beaconLikeDistanceList[getBeaconLevel(block)]
            false -> plugin.addonConfig.distance.jukeboxDistance
        }

        val job = plugin.audioPlayerManager.startTrackJob(track, source, distance)

        jobByBlock[block]?.cancel()
        jobByBlock[block] = job

        val actionbarMessage = MinecraftTextComponent.translatable(
            "pv.addon.discs.actionbar.playing", trackName
        )

        voicePlayer.visualizeDistance(
            pos.toPosition(),
            distance.toInt(),
            0xf1c40f
        )

        suspendSync(plugin) { block.world.getNearbyPlayers(block.location, distance.toDouble()) }
            .map { it.asVoicePlayer(plugin.voiceServer) }
            .forEach { it?.sendAnimatedActionBar(actionbarMessage) }
    }}

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    fun onDiskEject(event: PlayerInteractEvent) {
        if (event.action != Action.RIGHT_CLICK_BLOCK) return

        if ((event.player.inventory.itemInMainHand.type != Material.AIR ||
                    event.player.inventory.itemInOffHand.type != Material.AIR) &&
            event.player.isSneaking
        ) return

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

    private fun getBeaconLevel(block: Block) = (1 until plugin.addonConfig.distance.beaconLikeDistanceList.size).takeWhile { level ->
        (-level..level).all { xOffset ->
            (-level..level).all { zOffset ->
                Location(
                    block.world,
                    (block.x + xOffset).toDouble(),
                    (block.y - level).toDouble(),
                    (block.z + zOffset).toDouble()
                ).block.isBeaconBaseBlock()
            }
        }
    }.count()
}
