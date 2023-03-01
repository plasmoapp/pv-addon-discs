package su.plo.template

import com.google.inject.Inject
import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager
import com.sedmelluq.discord.lavaplayer.player.event.AudioEventListener
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist
import com.sedmelluq.discord.lavaplayer.track.AudioTrack
import org.bukkit.Bukkit
import org.bukkit.plugin.java.JavaPlugin
import su.plo.lib.api.server.world.ServerPos3d
import su.plo.voice.api.addon.AddonManager
import su.plo.voice.api.addon.AddonScope
import su.plo.voice.api.addon.annotation.Addon
import su.plo.voice.api.event.EventSubscribe
import su.plo.voice.api.server.PlasmoVoiceServer
import su.plo.voice.api.server.audio.line.ServerSourceLine
import su.plo.voice.api.server.audio.source.ServerStaticSource
import su.plo.voice.api.server.event.config.VoiceServerConfigLoadedEvent
import su.plo.voice.proto.packets.udp.clientbound.SourceAudioPacket


@Addon(id = "audio-player", scope = AddonScope.SERVER, version = "1.0.0", authors = ["KPidS"])
class TestPlugin : JavaPlugin() {

    private val addonName = "audio_player"

    private val lavaPlayerManager: AudioPlayerManager = DefaultAudioPlayerManager()

    @Inject
    private lateinit var voiceServer: PlasmoVoiceServer

    private lateinit var sourceLine: ServerSourceLine

    override fun onLoad() {
        AddonManager.getInstance().load(this)
        AudioSourceManagers.registerRemoteSources(lavaPlayerManager)
    }

    val testIdentifier = "https://www.youtube.com/watch?v=9HBRXbU9Y5I"

    @EventSubscribe
    fun onConfigLoaded(event: VoiceServerConfigLoadedEvent) {
        sourceLine = voiceServer.sourceLineManager.register(
            this,
            addonName,
            "pv.activation.$addonName",
            "plasmovoice:textures/icons/speaker_group.png",
            10
        )

        val player = lavaPlayerManager.createPlayer()

//        player.addListener {
//            it.
//        }

//        player.

        val world = voiceServer.minecraftServer.worlds.toList()[0]

        val source = voiceServer.sourceManager.createStaticSource(
            this,
            ServerPos3d(world, 0.0, 60.0, 0.0),
            sourceLine,
            "opus",
            true
        )

        val encrypter = voiceServer.encryptionManager.default

        lavaPlayerManager.loadItem(testIdentifier, object : AudioLoadResultHandler {

            override fun trackLoaded(track: AudioTrack) {
                println("Loaded: ${track.info.title}")
                player.playTrack(track)

                var i: Long = 0

                while(true) {
                    val frame = player.provide() ?: continue
                    val packet = SourceAudioPacket(
                        i++,
                        source.state.toByte(),
                        encrypter.encrypt(frame.data),
                        source.id,
                        0
                    )
                    source.sendAudioPacket(packet, 100)
                }
            }

            override fun playlistLoaded(playlist: AudioPlaylist) {
                println("Playlist loaded")
            }

            override fun loadFailed(exception: FriendlyException) {
                println("Load failed")
            }

            override fun noMatches() {
                println("No matches")
            }

        })

    }

    override fun onEnable() {

//        sourceLine = voiceServer.sourceLineManager.register(
//            this,
//            addonName,
//            "pv.activation.$addonName",
//            "plasmovoice:textures/icons/speaker_group.png",
//            10
//        )
//
//        val player = lavaPlayerManager.createPlayer()
//
//        val world = voiceServer.minecraftServer.worlds.toList()[0]
//
//        val source = voiceServer.sourceManager.createStaticSource(
//            this,
//            ServerPos3d(world, 0.0, 60.0, 0.0),
//            sourceLine,
//            "opus",
//            true
//        )
//
//        lavaPlayerManager.loadItem(testIdentifier, object : AudioLoadResultHandler {
//
//            override fun trackLoaded(track: AudioTrack) {
//                println("Loaded: ${track.info.title}")
//                player.playTrack(track)
//
//                for (i in 0..Long.MAX_VALUE) {
//                    val frame = player.provide() ?: continue
//                    val packet = SourceAudioPacket(
//                        i,
//                        source.state.toByte(),
//                        frame.data,
//                        source.id,
//                        0
//                    )
//                    source.sendAudioPacket(packet, 100)
//                }
//            }
//
//            override fun playlistLoaded(playlist: AudioPlaylist) {
////                TODO("Not yet implemented")
//            }
//
//            override fun loadFailed(exception: FriendlyException) {
////                TODO("Not yet implemented")
//            }
//
//            override fun noMatches() {
////                TODO("Not yet implemented")
//            }
//
//        })
//
//        println("Я еблан")
    }
}
