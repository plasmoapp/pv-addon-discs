package su.plo.voice.disks

import com.comphenix.protocol.ProtocolLibrary
import com.comphenix.protocol.ProtocolManager
import com.comphenix.protocol.events.ListenerPriority
import com.google.inject.Inject
import org.bukkit.NamespacedKey
import org.bukkit.plugin.java.JavaPlugin
import su.plo.lib.api.server.permission.PermissionDefault
import su.plo.voice.disks.event.JukeboxEventListener
import su.plo.voice.api.addon.AddonManager
import su.plo.voice.api.addon.AddonScope
import su.plo.voice.api.addon.annotation.Addon
import su.plo.voice.api.event.EventSubscribe
import su.plo.voice.api.server.PlasmoVoiceServer
import su.plo.voice.api.server.audio.line.ServerSourceLine
import su.plo.voice.api.server.event.config.VoiceServerConfigLoadedEvent
import su.plo.voice.disks.command.subcommand.BurnCommand
import su.plo.voice.disks.command.CommandHandler
import su.plo.voice.disks.command.subcommand.EraseCommand
import su.plo.voice.disks.packet.CancelJukeboxPlayEvent


@Addon(id = "disks", scope = AddonScope.SERVER, version = "1.0.0", authors = ["KPidS"])
class DisksPlugin : JavaPlugin() {

    private val addonName = "disks"

    @Inject
    lateinit var voiceServer: PlasmoVoiceServer

    lateinit var sourceLine: ServerSourceLine

    lateinit var audioPlayerManager: PlasmoAudioPlayerManager

    lateinit var addonConfig: AddonConfig

    val identifierKey = NamespacedKey(this, "identifier")

    override fun onLoad() {
        AddonManager.getInstance().load(this)
    }

    @EventSubscribe
    fun onConfigLoaded(event: VoiceServerConfigLoadedEvent) {

        addonConfig = AddonConfig.loadConfig(voiceServer)

        sourceLine = voiceServer.sourceLineManager.register(
            this,
            addonName,
            "pv.activation.$addonName",
            "plasmovoice:textures/icons/speaker_disc.png",
            addonConfig.sourceLineWeight
        )

        audioPlayerManager = PlasmoAudioPlayerManager(voiceServer, addonConfig)
    }

    override fun onEnable() {
        server.pluginManager.registerEvents(JukeboxEventListener(this), this)

        val handler = CommandHandler(this)
            .addSubCommand(::BurnCommand)
            .addSubCommand(::EraseCommand)

        val command = getCommand("disc") ?: throw Exception("Command not found")

        command.setExecutor(handler)
        command.tabCompleter = handler

        voiceServer.minecraftServer.permissionsManager
            .register("pv.addon.disks.play", PermissionDefault.TRUE)

        val protocolManager: ProtocolManager = ProtocolLibrary.getProtocolManager()

        protocolManager.addPacketListener(
            CancelJukeboxPlayEvent(this, ListenerPriority.NORMAL)
        )
    }
}
