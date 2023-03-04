package su.plo.voice.discs

import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist
import com.sedmelluq.discord.lavaplayer.track.AudioTrack
import com.sedmelluq.discord.lavaplayer.track.AudioTrackState
import kotlinx.coroutines.*
import su.plo.voice.api.server.PlasmoVoiceServer
import su.plo.voice.api.server.audio.source.ServerStaticSource
import su.plo.voice.proto.packets.tcp.clientbound.SourceAudioEndPacket
import su.plo.voice.proto.packets.udp.clientbound.SourceAudioPacket
import java.util.concurrent.CompletableFuture
import java.util.concurrent.TimeUnit

class PlasmoAudioPlayerManager(
    private val voiceServer: PlasmoVoiceServer,
    addonConfig: AddonConfig
) {
    private val lavaPlayerManager: AudioPlayerManager = DefaultAudioPlayerManager()
    private val scope = CoroutineScope(Dispatchers.Default)
    private val encrypt = voiceServer.defaultEncryption

    init {
        AudioSourceManagers.registerRemoteSources(lavaPlayerManager)
    }

    private val distance: Short = addonConfig.jukeboxDistance.toShort()

    fun startTrackJob(track: AudioTrack, source: ServerStaticSource) = scope.launch {

        val player = lavaPlayerManager.createPlayer()

        player.playTrack(track)

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

            player.destroy()

            source.sendPacket(SourceAudioEndPacket(
                source.id,
                i++
            ), distance)

            voiceServer.sourceManager.remove(source)
        }}
    }

    fun getTrack(identifier: String): AudioTrack {

        val future = CompletableFuture<AudioTrack>()

        lavaPlayerManager.loadItem(identifier, object : AudioLoadResultHandler {

            override fun trackLoaded(track: AudioTrack) {
                future.complete(track)
            }

            override fun playlistLoaded(playlist: AudioPlaylist) {
                future.complete(playlist.tracks.getOrNull(0))
            }

            override fun loadFailed(exception: FriendlyException) {
                future.completeExceptionally(exception)
            }

            override fun noMatches() {
                future.completeExceptionally(
                    FriendlyException(
                        "No matches",
                        FriendlyException.Severity.COMMON,
                        Exception("No matches")
                    )
                )
            }
        })
        return future.get()
    }
}