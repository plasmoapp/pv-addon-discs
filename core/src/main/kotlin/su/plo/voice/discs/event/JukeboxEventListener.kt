package su.plo.voice.discs.event

import kotlinx.coroutines.*
import kotlinx.coroutines.future.await
import net.kyori.adventure.text.TextComponent
import org.bukkit.Bukkit
import org.bukkit.GameMode
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.block.Block
import org.bukkit.block.Jukebox
import org.bukkit.entity.minecart.HopperMinecart
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.block.Action
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.entity.EntityExplodeEvent
import org.bukkit.event.inventory.InventoryMoveItemEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.event.world.ChunkLoadEvent
import org.bukkit.event.world.ChunkUnloadEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.plugin.java.JavaPlugin
import org.koin.core.component.inject
import su.plo.slib.api.chat.component.McTextComponent
import su.plo.slib.api.chat.style.McTextStyle
import su.plo.slib.api.server.position.ServerPos3d
import su.plo.voice.api.logging.DebugLogger
import su.plo.voice.api.server.PlasmoVoiceServer
import su.plo.voice.api.server.audio.line.ServerSourceLine
import su.plo.voice.api.server.player.VoicePlayer
import su.plo.voice.discs.AddonConfig
import su.plo.voice.discs.AddonKeys
import su.plo.voice.discs.PlasmoAudioPlayerManager
import su.plo.voice.discs.utils.PluginKoinComponent
import su.plo.voice.discs.utils.extend.*

class JukeboxEventListener : Listener, PluginKoinComponent {

    private val keys: AddonKeys by inject()
    private val plugin: JavaPlugin by inject()
    private val config: AddonConfig by getter()
    private val voiceServer: PlasmoVoiceServer by inject()
    private val audioPlayerManager: PlasmoAudioPlayerManager by getter()
    private val debugLogger: DebugLogger by getter()
    private val sourceLine: ServerSourceLine by getter()

    private val jobByBlock: MutableMap<Block, Job> = HashMap()

    @EventHandler
    fun onChunkLoad(event: ChunkLoadEvent): Unit = with(keys) {
        event.chunk.getTileEntities({ it.isJukebox() }, true)
            .forEach {
                val jukebox = it as? Jukebox ?: return@forEach
                if (!it.record.isCustomDisc()) return@forEach

                jukebox.stopPlayingWithUpdate()
            }
    }

    @EventHandler
    fun onChunkUnload(event: ChunkUnloadEvent) {
        val chunk = event.chunk

        jobByBlock.keys
            .filter { it.inChunk(chunk) }
            .forEach {
                jobByBlock.remove(it)?.cancel()
            }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    fun onDiscInsert(event: PlayerInteractEvent) = with(keys) {

        if (event.action != Action.RIGHT_CLICK_BLOCK) return

        if (event.player.gameMode == GameMode.ADVENTURE) return

        val block = event.clickedBlock ?: return

        val jukebox = block.asJukebox() ?: return

        if (jukebox.record.type != Material.AIR) return

        val item = event.item?.takeIf { it.isCustomDisc() } ?: return

        val player = event.player
            .takeIf {
                Bukkit.getServer().getMinecraftVersionInt() < 12100 || !it.isSneaking
            } ?: return

        val voicePlayer = player.asVoicePlayer(voiceServer) ?: return

        if (!voicePlayer.instance.hasPermission("pv.addon.discs.play")) return

        val identifier = item.customDiscIdentifier() ?: return

        voicePlayer.instance.sendActionBar(
            McTextComponent.translatable("pv.addon.discs.actionbar.loading")
                .withStyle(McTextStyle.YELLOW)
        )

        jobByBlock[block]?.cancel()
        jobByBlock[block] = playTrack(identifier, block, item, voicePlayer)
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    fun onDiskEject(event: PlayerInteractEvent) {
        if (event.action != Action.RIGHT_CLICK_BLOCK) return

        if (
            (event.player.inventory.itemInMainHand.type != Material.AIR ||
                    event.player.inventory.itemInOffHand.type != Material.AIR) &&
            event.player.isSneaking
        ) return

        val block = event.clickedBlock ?: return

        val jukebox = block.asJukebox()?: return

        jukebox.takeIf { it.isPlaying } ?: return

        jukebox.stopPlaying()

        jobByBlock.remove(block)?.cancel()
    }

    @EventHandler
    fun onJukeboxBreak(event: BlockBreakEvent) {
        event.block
            .takeIf { it.isJukebox() }
            ?.also {
                it.asJukebox()?.stopPlaying()
            }
            ?.let { jobByBlock.remove(it) }
            ?.cancel()
    }

    @EventHandler
    fun onJukeboxExplode(event: EntityExplodeEvent) {
        event.blockList()
            .filter { it.isJukebox() }
            .forEach { jobByBlock.remove(it)?.cancel() }
    }

    private fun playTrack(
        identifier: String,
        block: Block,
        item: ItemStack,
        voicePlayer: VoicePlayer? = null,
    ): Job = CoroutineScope(Dispatchers.Default).launch {

        val track = try {
            audioPlayerManager.getTrack(identifier).await()
        } catch (e: Exception) {
            // todo: send error to who?
            voicePlayer?.instance?.sendActionBar(
                McTextComponent.translatable(
                    "pv.addon.discs.actionbar.track_not_found",
                    e.message ?: "Unexpected error"
                )
                    .withStyle(McTextStyle.RED)
            )

            debugLogger.log("Failed to load track", e)

            plugin.suspendSync(block.location) { block.asJukebox()?.eject() }
            return@launch
        }

        val trackName = item.itemMeta
            ?.lore()
            ?.getOrNull(0)
            ?.let { it as? TextComponent }
            ?.content()
            ?: track.info.title

        val world = voiceServer.minecraftServer.getWorld(block.world)

        val pos = ServerPos3d(
            world,
            block.x.toDouble() + 0.5,
            block.y.toDouble() + 1.5,
            block.z.toDouble() + 0.5
        )

        val source = sourceLine.createStaticSource(pos, !config.monoSources)
        source.setName(trackName)

        val distance = when (config.distance.enableBeaconLikeDistance) {
            true -> {
                val beaconLevel = plugin.suspendSync(block.location) {
                    getBeaconLevel(block)
                }
                config.distance.beaconLikeDistanceList[beaconLevel]
            }

            false -> config.distance.jukeboxDistance
        }

        val actionbarMessage = McTextComponent.translatable(
            "pv.addon.discs.actionbar.playing", trackName
        )

        // todo: visualize distance to who?
        if (config.distance.visualizeDistance) {
            voicePlayer?.visualizeDistance(
                pos.toPosition(),
                distance.toInt(),
                0xf1c40f
            )
        }
        debugLogger.log("Starting track job \"$trackName\" with distance $distance at ${block.location}")

        plugin.suspendSync(block.location) { block.world.getNearbyPlayers(block.location, distance.toDouble()) }
            .map { it.asVoicePlayer(voiceServer) }
            .forEach { it?.sendAnimatedActionBar(actionbarMessage) }

        val job = audioPlayerManager.startTrackJob(track, source, distance)
        try {
            var lastTick = System.currentTimeMillis()

            while (job.isActive) {
                // every 30 seconds we need to reset record state
                if (System.currentTimeMillis() - lastTick < 30_000L) {
                    delay(100L)
                    continue
                }

                plugin.suspendSync(block.location) {
                    val jukebox = block.asJukebox() ?: return@suspendSync

                    jukebox.setRecord(jukebox.record)
                    try {
                        val startPlayingMethod = jukebox.javaClass.getMethod("startPlaying")
                        startPlayingMethod.invoke(jukebox)
                    } catch (_: ReflectiveOperationException) {
                        // ignore on old mc versions
                    }
                    jukebox.update()
                }
                lastTick = System.currentTimeMillis()

            }
        } finally {
            withContext(NonCancellable) {
                debugLogger.log("Track \"${source.sourceInfo.name}\" on $source was ended or cancelled")

                job.cancelAndJoin()
                source.remove()

                plugin.suspendSync(block.location) {
                    val jukebox = block.asJukebox() ?: return@suspendSync
                    val currentJob = jobByBlock[block] ?: return@suspendSync
                    if (currentJob != this@launch) return@suspendSync
                    jukebox.stopPlayingWithUpdate()
                    jobByBlock.remove(block)
                }
            }
        }
    }

    private fun getBeaconLevel(block: Block) =
        (1 until config.distance.beaconLikeDistanceList.size).takeWhile { level ->
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

    inner class HopperEventListener : Listener {

        @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
        fun onMinecartHopperPull(event: InventoryMoveItemEvent) = with(keys) {
            if (event.source.type.name != "JUKEBOX") return@with
            if (event.destination.holder !is HopperMinecart) return@with

            val block = event.source.location?.block ?: return@with

            val item = event.item
            if (!item.isCustomDisc()) return@with

            jobByBlock.remove(block)?.cancel()
        }

        @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
        fun onHopperInsertToJukebox(event: InventoryMoveItemEvent) = with(keys) {
            if (event.destination.type.name != "JUKEBOX") return

            val block = event.destination.location?.block ?: return

            val item = event.item
            val identifier = item.customDiscIdentifier() ?: return

            jobByBlock.remove(block)?.cancel()
            jobByBlock[block] = playTrack(identifier, block, item)
        }
    }
}
