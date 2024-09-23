package su.plo.voice.discs

import kotlinx.coroutines.Job
import org.bukkit.plugin.java.JavaPlugin
import org.koin.core.component.inject
import su.plo.voice.api.logging.DebugLogger
import su.plo.voice.api.server.PlasmoVoiceServer
import su.plo.voice.api.server.audio.provider.AudioFrameProvider
import su.plo.voice.api.server.audio.provider.AudioFrameResult
import su.plo.voice.api.server.audio.source.ServerProximitySource
import su.plo.voice.discs.utils.PluginKoinComponent
import su.plo.voice.lavaplayer.libs.com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler
import su.plo.voice.lavaplayer.libs.com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager
import su.plo.voice.lavaplayer.libs.com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager
import su.plo.voice.lavaplayer.libs.com.sedmelluq.discord.lavaplayer.source.bandcamp.BandcampAudioSourceManager
import su.plo.voice.lavaplayer.libs.com.sedmelluq.discord.lavaplayer.source.beam.BeamAudioSourceManager
import su.plo.voice.lavaplayer.libs.com.sedmelluq.discord.lavaplayer.source.getyarn.GetyarnAudioSourceManager
import su.plo.voice.lavaplayer.libs.com.sedmelluq.discord.lavaplayer.source.http.HttpAudioSourceManager
import su.plo.voice.lavaplayer.libs.com.sedmelluq.discord.lavaplayer.source.soundcloud.SoundCloudAudioSourceManager
import su.plo.voice.lavaplayer.libs.com.sedmelluq.discord.lavaplayer.source.twitch.TwitchStreamAudioSourceManager
import su.plo.voice.lavaplayer.libs.com.sedmelluq.discord.lavaplayer.source.vimeo.VimeoAudioSourceManager
import su.plo.voice.lavaplayer.libs.com.sedmelluq.discord.lavaplayer.tools.FriendlyException
import su.plo.voice.lavaplayer.libs.com.sedmelluq.discord.lavaplayer.track.AudioItem
import su.plo.voice.lavaplayer.libs.com.sedmelluq.discord.lavaplayer.track.AudioPlaylist
import su.plo.voice.lavaplayer.libs.com.sedmelluq.discord.lavaplayer.track.AudioReference
import su.plo.voice.lavaplayer.libs.com.sedmelluq.discord.lavaplayer.track.AudioTrack
import su.plo.voice.lavaplayer.libs.com.sedmelluq.discord.lavaplayer.track.AudioTrackState
import su.plo.voice.lavaplayer.libs.dev.lavalink.youtube.YoutubeAudioSourceManager
import java.io.File
import java.net.URI
import java.util.concurrent.CompletableFuture

class PlasmoAudioPlayerManager : PluginKoinComponent {

    private val plugin: JavaPlugin by inject()
    private val voiceServer: PlasmoVoiceServer by inject()
    private val debugLogger: DebugLogger by inject()
    private val config: AddonConfig by inject()

    private val lavaPlayerManager: AudioPlayerManager = DefaultAudioPlayerManager()
    private val encryption = voiceServer.defaultEncryption

    init {
        registerSources()
    }

    fun save() {
        lavaPlayerManager.sourceManagers
            .filterIsInstance<YoutubeAudioSourceManager>()
            .firstOrNull()
            ?.takeIf { it.oauth2RefreshToken != null }
            ?.let {
                val refreshTokenFile = File(plugin.dataFolder, ".youtube-token")
                refreshTokenFile.writeText(it.oauth2RefreshToken!!)
            }
    }

    fun startTrackJob(track: AudioTrack, source: ServerProximitySource<*>, distance: Short): Job {
        val player = lavaPlayerManager.createPlayer()
        player.playTrack(track)

        debugLogger.log("Starting track \"${source.sourceInfo.name}\" on $source")

        val frameProvider = object : AudioFrameProvider {
            override fun provide20ms(): AudioFrameResult =
                if (track.state == AudioTrackState.FINISHED) {
                    AudioFrameResult.Finished
                } else {
                    val frame = player.provide()?.data?.let {
                        encryption.encrypt(it)
                    }

                    AudioFrameResult.Provided(frame)
                }
        }

        val sender = source.createAudioSender(frameProvider, distance)
            .also { it.start() }
        val job = sender.job ?: throw IllegalStateException("AudioSender job is not started")
        sender.onStop {
            player.destroy()
            source.remove()
        }

        return job
    }

    val noMatchesException = FriendlyException(
        "No matches",
        FriendlyException.Severity.COMMON,
        Exception("No matches")
    )

    fun getTrack(identifier: String): CompletableFuture<AudioTrack> {

        val future = CompletableFuture<AudioTrack>()

        lavaPlayerManager.loadItem(identifier, object : AudioLoadResultHandler {
            override fun trackLoaded(track: AudioTrack) {
                future.complete(track)
            }

            override fun playlistLoaded(playlist: AudioPlaylist) {
                if (playlist.selectedTrack == null) {
                    future.completeExceptionally(noMatchesException)
                    return
                }

                future.complete(playlist.selectedTrack)
            }

            override fun loadFailed(exception: FriendlyException) {
                future.completeExceptionally(exception)
            }

            override fun noMatches() {
                future.completeExceptionally(noMatchesException)
            }
        })
        return future
    }

    fun getPlaylist(identifier: String): CompletableFuture<AudioPlaylist> {

        val future = CompletableFuture<AudioPlaylist>()

        lavaPlayerManager.loadItem(identifier, object : AudioLoadResultHandler {
            override fun trackLoaded(track: AudioTrack) {
                future.completeExceptionally(noMatchesException)
            }

            override fun playlistLoaded(playlist: AudioPlaylist) {
                future.complete(playlist)
            }

            override fun loadFailed(exception: FriendlyException) {
                future.completeExceptionally(exception)
            }

            override fun noMatches() {
                future.completeExceptionally(noMatchesException)
            }
        })

        return future
    }

    private fun registerSources() {
        lavaPlayerManager.registerSourceManager(
            YoutubeAudioSourceManager(true)
                .also { source ->
                    if (config.youtubeSource.useOauth2) {
                        val refreshToken = File(plugin.dataFolder, ".youtube-token")
                            .takeIf { it.isFile && it.exists() }
                            ?.readText()
                            ?.trim()
                        source.useOauth2(refreshToken, false)
                    }
                }
        )
        lavaPlayerManager.registerSourceManager(SoundCloudAudioSourceManager.createDefault())
        lavaPlayerManager.registerSourceManager(BandcampAudioSourceManager())
        lavaPlayerManager.registerSourceManager(VimeoAudioSourceManager())
        lavaPlayerManager.registerSourceManager(TwitchStreamAudioSourceManager())
        lavaPlayerManager.registerSourceManager(BeamAudioSourceManager())
        lavaPlayerManager.registerSourceManager(GetyarnAudioSourceManager())
        lavaPlayerManager.registerSourceManager(CustomHttpAudioSourceManager())
    }

    inner class CustomHttpAudioSourceManager : HttpAudioSourceManager() {
        override fun loadItem(manager: AudioPlayerManager?, reference: AudioReference?): AudioItem? {
            if (reference != null && config.httpSource.whitelistEnabled) {
                val identifier = reference.identifier
                val host = runCatching { URI(identifier) }.getOrNull()?.host ?: return null
                val hostSplit = host.split(".")
                if (!config.httpSource.whitelist.any {
                        val itemSplitLength = it.split(".").size

                        val hostToCompare = hostSplit
                            .subList((hostSplit.size - itemSplitLength).coerceAtLeast(0), hostSplit.size)
                            .joinToString(".")

                        hostToCompare == it
                    }) return null
            }
            return super.loadItem(manager, reference)
        }
    }
}
