package su.plo.voice.discs

import kotlinx.coroutines.*
import su.plo.voice.api.encryption.Encryption
import su.plo.voice.api.server.audio.source.ServerStaticSource
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
import su.plo.voice.lavaplayer.libs.com.sedmelluq.discord.lavaplayer.track.*
import su.plo.voice.lavaplayer.libs.dev.lavalink.youtube.YoutubeAudioSourceManager
import su.plo.voice.lavaplayer.libs.dev.lavalink.youtube.http.YoutubeOauth2Handler
import su.plo.voice.proto.packets.tcp.clientbound.SourceAudioEndPacket
import su.plo.voice.proto.packets.udp.clientbound.SourceAudioPacket
import java.io.File
import java.net.URI
import java.util.concurrent.CompletableFuture
import java.util.concurrent.TimeUnit

class PlasmoAudioPlayerManager(
    private val plugin: DiscsPlugin
) {
    private val lavaPlayerManager: AudioPlayerManager = DefaultAudioPlayerManager()
    private val scope = CoroutineScope(Dispatchers.Default)
    private val encrypt: Encryption
        get() = plugin.voiceServer.defaultEncryption

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

    fun startTrackJob(track: AudioTrack, source: ServerStaticSource, distance: Short) = scope.launch {

        val player = lavaPlayerManager.createPlayer()
        player.playTrack(track)

        DiscsPlugin.DEBUG_LOGGER.log("Starting track \"${source.sourceInfo.name}\" on $source")

        var i = 0L
        var start = 0L

        try { while(isActive) {

            if (track.state == AudioTrackState.FINISHED) break

            val frame = player.provide(5L, TimeUnit.MILLISECONDS) ?: continue

            val packet = SourceAudioPacket(
                i++,
                source.state.toByte(),
                encrypt.encrypt(frame.data),
                source.id,
                distance
            )

            source.sendAudioPacket(packet, distance)

            if (start == 0L) start = System.currentTimeMillis()

            val wait = (start + frame.timecode) - System.currentTimeMillis()

            if (wait <= 0) continue else delay(wait)

        }} finally { withContext(NonCancellable) {

            DiscsPlugin.DEBUG_LOGGER.log("Track \"${source.sourceInfo.name}\" on $source was ended or cancelled")

            player.destroy()

            source.sendPacket(SourceAudioEndPacket(
                source.id,
                i++
            ), distance)

            plugin.sourceLine.removeSource(source)
        }}
    }

    val noMatchesException = FriendlyException(
        "No matches",
        FriendlyException.Severity.COMMON,
        Exception("No matches")
    )

    fun getTrack(identifier: String): CompletableFuture<AudioTrack> {

        val future = CompletableFuture<AudioTrack>()

        lavaPlayerManager.loadItem(identifier, object: AudioLoadResultHandler {
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
                    if (plugin.addonConfig.youtubeSource.useOauth2) {
                        val refreshToken = File(plugin.dataFolder, ".youtube-token")
                            .takeIf { it.isFile && it.exists() }
                            ?.readText()
                            ?.trim()
                        source.useOauth2(refreshToken, false)

                        if (refreshToken != null) {
                            // todo: token validation is not implemented yet,
                            //  so for now I just forcibly set enabled to true in YoutubeOauth2Handler

                            val oauth2HandlerField = YoutubeAudioSourceManager::class.java.getDeclaredField("oauth2Handler")
                            oauth2HandlerField.isAccessible = true
                            val oauth2Handler = oauth2HandlerField.get(source) as YoutubeOauth2Handler

                            val enabledField = YoutubeOauth2Handler::class.java.getDeclaredField("enabled")
                            enabledField.isAccessible = true
                            enabledField.set(oauth2Handler, true)
                        }
                    }
                }
        )
        lavaPlayerManager.registerSourceManager(SoundCloudAudioSourceManager.createDefault())
        lavaPlayerManager.registerSourceManager(BandcampAudioSourceManager())
        lavaPlayerManager.registerSourceManager(VimeoAudioSourceManager())
        lavaPlayerManager.registerSourceManager(TwitchStreamAudioSourceManager())
        lavaPlayerManager.registerSourceManager(BeamAudioSourceManager())
        lavaPlayerManager.registerSourceManager(GetyarnAudioSourceManager())
        lavaPlayerManager.registerSourceManager(CustomHttpAudioSourceManager(plugin))
    }

    class CustomHttpAudioSourceManager(private val plugin: DiscsPlugin) : HttpAudioSourceManager() {
        override fun loadItem(manager: AudioPlayerManager?, reference: AudioReference?): AudioItem? {
            if (reference != null && plugin.addonConfig.httpSource.whitelistEnabled) {
                val identifier = reference.identifier
                val host = runCatching { URI(identifier) }.getOrNull()?.host ?: return null
                val hostSplit = host.split(".")
                if (!plugin.addonConfig.httpSource.whitelist.any {
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
