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
import su.plo.voice.discs.config.YoutubeClient
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
import su.plo.voice.lavaplayer.libs.org.apache.http.HttpHost
import su.plo.voice.lavaplayer.libs.org.apache.http.auth.AuthScope
import su.plo.voice.lavaplayer.libs.org.apache.http.auth.UsernamePasswordCredentials
import su.plo.voice.lavaplayer.libs.org.apache.http.impl.client.BasicCredentialsProvider
import su.plo.voice.lavaplayer.libs.org.apache.http.impl.client.HttpClientBuilder
import java.io.File
import java.net.URI
import java.util.concurrent.CompletableFuture
import java.util.function.Consumer

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

    private fun httpProxy(): Pair<HttpHost, BasicCredentialsProvider?>? {
        val proxyString = config.httpProxy ?: return null

        val regex =
            Regex("^(?:(https?)://)?(?:(\\w+):(\\w*)@)?([a-zA-Z0-9][a-zA-Z0-9-_.]{0,61}|(\\d{1,3}(?:\\.\\d{1,3}){3})):(\\d{1,5})\$")
        val match = regex.find(proxyString) ?: run {
            plugin.slF4JLogger.error("Failed to parse proxy: {}", proxyString)
            return null
        }

        val scheme = match.groupValues[1].takeIf { it.isNotBlank() }
        val username = match.groupValues[2]
        val password = match.groupValues[3]
        val hostname = match.groupValues[4]
        val port = match.groupValues[6].toInt()

        val credentials =
            if (username.isNotBlank() && password.isNotBlank()) {
                BasicCredentialsProvider().apply {
                    setCredentials(
                        AuthScope.ANY,
                        UsernamePasswordCredentials(username, password)
                    )
                }
            } else null

        val host = HttpHost(hostname, port, scheme)

        return host to credentials
    }

    private fun proxyHttpBuilder(): Consumer<HttpClientBuilder>? {
        val (host, credentials) = httpProxy() ?: return null

        return Consumer { builder ->
            builder.setProxy(host)
            credentials?.let {
                builder.setDefaultCredentialsProvider(it)
            }
        }
    }

    private fun registerSources() {
        val proxyHttpBuilder = proxyHttpBuilder()
        proxyHttpBuilder?.let { lavaPlayerManager.setHttpBuilderConfigurator(it) }

        val youtubeClients = config.youtubeSource.clients
            ?.mapNotNull {
                try {
                    // todo: config resolver don't support list of enums for some reason
                    YoutubeClient.valueOf(it)
                } catch (e: IllegalArgumentException) {
                    null
                }
            }
            ?.takeIf { it.isNotEmpty() }
            ?: listOf(
                YoutubeClient.WEB,
                YoutubeClient.ANDROID,
                YoutubeClient.TVHTML5EMBEDDED,
                YoutubeClient.MUSIC
            )
        plugin.slF4JLogger.info("YouTube clients: {}", youtubeClients)

        lavaPlayerManager.registerSourceManager(
            YoutubeAudioSourceManager(true, *youtubeClients.map { it.client }.toTypedArray())
                .also { source ->
                    proxyHttpBuilder?.let { source.httpInterfaceManager.configureBuilder(it) }

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
