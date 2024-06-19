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
import su.plo.slib.api.chat.component.McTextComponent
import su.plo.slib.api.chat.style.McTextStyle
import su.plo.slib.api.server.position.ServerPos3d
import su.plo.voice.api.server.player.VoicePlayer
import su.plo.voice.discs.DiscsPlugin
import su.plo.voice.discs.utils.extend.*
import su.plo.voice.discs.utils.suspendSync

class JukeboxEventListener(
    private val plugin: DiscsPlugin
) : Listener {

    private val jobByBlock: MutableMap<Block, Job> = HashMap()

    private val scope = CoroutineScope(Dispatchers.Default)

    init {
        if (Bukkit.getServer().isVersionGreaterOrEqual("1.19.4")) {
            Bukkit.getServer().pluginManager.registerEvents(HopperEventListener(), plugin)
        }
    }

    @EventHandler
    fun onChunkLoad(event: ChunkLoadEvent) {
        event.chunk.getTileEntities({ it.isJukebox() }, true)
            .forEach {
                val jukebox = it as? Jukebox ?: return@forEach
                if (!it.record.isCustomDisc(plugin)) return@forEach

                jukebox.stopPlayingWithUpdate()
            }
    }

    @EventHandler
    fun onChunkUnload(event: ChunkUnloadEvent) {
        event.chunk.getTileEntities({ it.isJukebox() }, true)
            .forEach {
                jobByBlock.remove(it.block)?.cancel()
            }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    fun onDiscInsert(event: PlayerInteractEvent) {

        if (event.action != Action.RIGHT_CLICK_BLOCK) return

        if (event.player.gameMode == GameMode.ADVENTURE) return

        val block = event.clickedBlock ?: return

        val jukebox = block.asJukebox() ?: return

        if (jukebox.isPlaying) return

        val item = event.item?.takeIf { it.isCustomDisc(plugin) } ?: return

        val voicePlayer = event.player.asVoicePlayer(plugin.voiceServer) ?: return

        if (!voicePlayer.instance.hasPermission("pv.addon.discs.play")) return

        val identifier = item.customDiscIdentifier(plugin) ?: return

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

        block.asJukebox()?.takeIf { it.isPlaying } ?: return

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
    ): Job = scope.launch {

        val track = try {
            plugin.audioPlayerManager.getTrack(identifier).await()
        } catch (e: Exception) {
            // todo: send error to who?
            voicePlayer?.instance?.sendActionBar(
                McTextComponent.translatable("pv.addon.discs.actionbar.track_not_found", e.message ?: "Unexpected error")
                    .withStyle(McTextStyle.RED)
            )

            DiscsPlugin.DEBUG_LOGGER.log("Failed to load track", e)

            suspendSync(block.location, plugin) { block.asJukebox()?.eject() }
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
            true -> {
                val beaconLevel = suspendSync(block.location, plugin) {
                    getBeaconLevel(block)
                }
                plugin.addonConfig.distance.beaconLikeDistanceList[beaconLevel]
            }
            false -> plugin.addonConfig.distance.jukeboxDistance
        }

        val actionbarMessage = McTextComponent.translatable(
            "pv.addon.discs.actionbar.playing", trackName
        )

        // todo: visualize distance to who?
        if (plugin.addonConfig.distance.visualizeDistance) {
            voicePlayer?.visualizeDistance(
                pos.toPosition(),
                distance.toInt(),
                0xf1c40f
            )
        }
        DiscsPlugin.DEBUG_LOGGER.log("Starting track job \"$trackName\" with distance $distance at ${block.location}")

        suspendSync(block.location, plugin) { block.world.getNearbyPlayers(block.location, distance.toDouble()) }
            .map { it.asVoicePlayer(plugin.voiceServer) }
            .forEach { it?.sendAnimatedActionBar(actionbarMessage) }

        val job = plugin.audioPlayerManager.startTrackJob(track, source, distance)
        try {
            var lastTick = System.currentTimeMillis()

            while (job.isActive) {
                // every 30 seconds we need to reset record state
                if (System.currentTimeMillis() - lastTick < 30_000L) {
                    delay(100L)
                    continue
                }

                suspendSync(block.location, plugin) {
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
                DiscsPlugin.DEBUG_LOGGER.log("Track \"${source.sourceInfo.name}\" on $source was ended or cancelled")
                job.cancelAndJoin()

                suspendSync(block.location, plugin) {
                    val jukebox = block.asJukebox() ?: return@suspendSync
                    jukebox.stopPlayingWithUpdate()
                    jobByBlock.remove(block)
                }
            }
        }
    }

    private fun getBeaconLevel(block: Block) =
        (1 until plugin.addonConfig.distance.beaconLikeDistanceList.size).takeWhile { level ->
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

    private inner class HopperEventListener : Listener {

        @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
        fun onHopperInsertToJukebox(event: InventoryMoveItemEvent) {
            if (event.destination.type.name != "JUKEBOX") return

            val block = event.destination.location?.block ?: return

            val item = event.item
            val identifier = item.customDiscIdentifier(plugin) ?: return

            jobByBlock.remove(block)?.cancel()
            jobByBlock[block] = playTrack(identifier, block, item)
        }
    }
}
