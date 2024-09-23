package su.plo.voice.discs

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.future.await
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import net.kyori.adventure.text.TextComponent
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.plugin.java.JavaPlugin
import org.koin.core.component.inject
import su.plo.slib.api.chat.component.McTextComponent
import su.plo.slib.api.chat.style.McTextStyle
import su.plo.voice.api.logging.DebugLogger
import su.plo.voice.api.server.PlasmoVoiceServer
import su.plo.voice.api.server.audio.line.ServerSourceLine
import su.plo.voice.discs.utils.PluginKoinComponent
import su.plo.voice.discs.utils.extend.asVoicePlayer
import su.plo.voice.discs.utils.extend.identifier
import su.plo.voice.discs.utils.extend.sendTranslatable
import su.plo.voice.discs.utils.extend.suspendSync
import su.plo.voice.lavaplayer.libs.com.sedmelluq.discord.lavaplayer.track.AudioTrack
import java.util.concurrent.ConcurrentHashMap

class GoatHornManager : PluginKoinComponent {

    private val plugin: JavaPlugin by inject()
    private val keys: AddonKeys by inject()
    private val config: AddonConfig by inject()
    private val voiceServer: PlasmoVoiceServer by inject()
    private val audioPlayerManager: PlasmoAudioPlayerManager by inject()
    private val debugLogger: DebugLogger by inject()
    private val sourceLine: ServerSourceLine by inject()

    private val jobByPlayer: MutableMap<Player, Job> = ConcurrentHashMap()

    fun canPlay(track: AudioTrack): Boolean {
        val maxDurationSeconds = config.goatHorn.maxDurationSeconds
        return maxDurationSeconds <= 0 || ((track.info.length / 1000) <= maxDurationSeconds) && !track.info.isStream
    }

    fun playTrack(
        player: Player,
        item: ItemStack
    ) = with(keys) {
        val identifier = item.identifier() ?: return

        CoroutineScope(Dispatchers.Default).launch {
            cancelTrack(player)?.join()
            jobByPlayer[player] = startJob(player, identifier, item)
        }
        Unit
    }

    fun cancelTrack(player: Player): Job? =
        jobByPlayer.remove(player)
            ?.also { it.cancel() }

    private fun startJob(
        player: Player,
        identifier: String,
        item: ItemStack
    ): Job = CoroutineScope(Dispatchers.Default).launch {
        val voicePlayer = player.asVoicePlayer(voiceServer) ?: return@launch

        voicePlayer.instance.sendActionBar(
            McTextComponent.translatable("pv.addon.discs.actionbar.loading")
                .withStyle(McTextStyle.YELLOW)
        )

        val track = try {
            audioPlayerManager.getTrack(identifier).await()
        } catch (e: Exception) {
            voicePlayer.instance.sendActionBar(
                McTextComponent.translatable(
                    "pv.addon.discs.actionbar.track_not_found",
                    e.message ?: "Unexpected error"
                )
                    .withStyle(McTextStyle.RED)
            )

            debugLogger.log("Failed to load track", e)
            return@launch
        }

        if (!canPlay(track)) {
            voicePlayer.instance.sendTranslatable(
                "pv.addon.discs.error.horn_too_long", config.goatHorn.maxDurationSeconds
            )
            return@launch
        }

        val trackName = item.itemMeta
            ?.lore()
            ?.getOrNull(0)
            ?.let { it as? TextComponent }
            ?.content()
            ?: track.info.title

        val source = sourceLine.createPlayerSource(voicePlayer, true)
            .apply {
                setName(trackName)
                removeFilter(filters.first()) // the first filter is always the "self" filter
                println(filters)
            }

        val distance = config.goatHorn.distance

        val actionbarMessage = McTextComponent.translatable(
            "pv.addon.discs.actionbar.playing", trackName
        )

        if (config.goatHorn.visualizeDistance) {
            voicePlayer.visualizeDistance(
                voicePlayer.instance.getPosition(),
                distance.toInt(),
                0xf1c40f
            )
        }

        plugin.suspendSync(player) { player.location.getNearbyPlayers(distance.toDouble()) }
            .map { it.asVoicePlayer(voiceServer) }
            .forEach { it?.sendAnimatedActionBar(actionbarMessage) }

        debugLogger.log("Starting track job \"$trackName\" with distance $distance on $player")

        val job = audioPlayerManager.startTrackJob(track, source, distance)
        try {
            job.join()
        } finally {
            withContext(NonCancellable) {
                debugLogger.log("Track \"$trackName\" on $source was ended or cancelled")

                job.cancelAndJoin()
                source.remove()

                jobByPlayer.remove(player)
            }
        }
    }
}