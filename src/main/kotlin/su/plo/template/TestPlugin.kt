package su.plo.template

import com.google.inject.Inject
import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist
import com.sedmelluq.discord.lavaplayer.track.AudioTrack
import com.sedmelluq.discord.lavaplayer.track.AudioTrackState
import org.bukkit.plugin.java.JavaPlugin
import su.plo.lib.api.server.world.ServerPos3d
import su.plo.template.event.JukeboxEventLister
import su.plo.voice.api.addon.AddonManager
import su.plo.voice.api.addon.AddonScope
import su.plo.voice.api.addon.annotation.Addon
import su.plo.voice.api.event.EventSubscribe
import su.plo.voice.api.server.PlasmoVoiceServer
import su.plo.voice.api.server.audio.line.ServerSourceLine
import su.plo.voice.api.server.event.config.VoiceServerConfigLoadedEvent
import su.plo.voice.api.server.event.connection.UdpClientConnectedEvent
import su.plo.voice.proto.packets.tcp.clientbound.SourceAudioEndPacket
import su.plo.voice.proto.packets.udp.clientbound.SourceAudioPacket
import java.lang.Long.min
import java.util.concurrent.TimeUnit


@Addon(id = "audio-player", scope = AddonScope.SERVER, version = "1.0.0", authors = ["KPidS"])
class TestPlugin : JavaPlugin() {

    private val addonName = "audio_player"

//    private val lavaPlayerManager: AudioPlayerManager = DefaultAudioPlayerManager()

    @Inject
    lateinit var voiceServer: PlasmoVoiceServer

    lateinit var sourceLine: ServerSourceLine

    lateinit var audioPlayerManager: PlasmoAudioPlayerManager

    override fun onLoad() {
        AddonManager.getInstance().load(this)
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
        audioPlayerManager = PlasmoAudioPlayerManager(voiceServer)
    }

//    @EventSubscribe
//    fun pepega(event: UdpClientConnectedEvent) {
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
//    }

    override fun onEnable() {
        server.pluginManager.registerEvents(JukeboxEventLister(this), this)
    }
}
