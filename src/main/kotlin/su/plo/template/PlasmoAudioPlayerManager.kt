package su.plo.template

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
import java.lang.Long.min

class PlasmoAudioPlayerManager(
    val voiceServer: PlasmoVoiceServer
) {
    private val lavaPlayerManager: AudioPlayerManager = DefaultAudioPlayerManager()

    private val scope = CoroutineScope(Dispatchers.Default)

    private val encrypter = voiceServer.defaultEncryption

    val testIdentifier = "https://www.youtube.com/watch?v=9HBRXbU9Y5I"

    init {
        AudioSourceManagers.registerRemoteSources(lavaPlayerManager)
    }

    fun playTrack(source: ServerStaticSource, identifier: String): Job {
        val track = getTrack(identifier) ?: throw Exception("Failed to load track")
        return playTrackJob(track, source)
    }

    val distance: Short = 32

    private fun playTrackJob(track: AudioTrack, source: ServerStaticSource) = scope.launch {

        val player = lavaPlayerManager.createPlayer()

        player.playTrack(track)

        var i = 0L
        var start = 0L

        try { while(isActive) {

            if (track.state == AudioTrackState.FINISHED) break

            val frame = player.provide(5L, TimeUnit.MILLISECONDS)

            if (frame == null) {
                println("frame is null")
                continue
            }

            val packet = SourceAudioPacket(
                i++,
                source.state.toByte(),
                encrypter.encrypt(frame.data),
                source.id,
                distance
            )
            source.sendAudioPacket(packet, distance)

            println("packet send")

            if (start == 0L) start = System.currentTimeMillis()

            val wait = (start + frame.timecode) - System.currentTimeMillis()

            if (wait <= 0) continue else delay(wait)

        } } finally { withContext(NonCancellable) {

            println("end")

            player.destroy()

            source.sendPacket(SourceAudioEndPacket(
                source.id,
                i++
            ), distance)

            voiceServer.sourceManager.remove(source)
        } }
    }

    private

//    @Throws(ExecutionException::class)
    fun getTrack(identifier: String): AudioTrack? {
        val future = CompletableFuture<AudioTrack>()
        lavaPlayerManager.loadItem(identifier, object : AudioLoadResultHandler {
            override fun trackLoaded(track: AudioTrack) {
                println("Loaded: ${track.info.title}")
                future.complete(track)
            }
            override fun playlistLoaded(playlist: AudioPlaylist) {
                future.complete(null)
            }
            override fun loadFailed(exception: FriendlyException) {
                future.complete(null)
            }
            override fun noMatches() {
                future.complete(null)
            }
        })
        return future.get()
    }
}

//class PlasmoPlayerHandler(
//
//) {
//
//}