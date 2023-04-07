package su.plo.voice.discs

import com.comphenix.protocol.ProtocolLibrary
import com.comphenix.protocol.ProtocolManager
import com.comphenix.protocol.events.ListenerPriority
import com.google.inject.Inject
import org.bukkit.NamespacedKey
import org.bukkit.plugin.java.JavaPlugin
import su.plo.lib.api.server.permission.PermissionDefault
import su.plo.voice.api.addon.AddonLoaderScope
import su.plo.voice.api.addon.annotation.Addon
import su.plo.voice.api.event.EventSubscribe
import su.plo.voice.api.server.PlasmoVoiceServer
import su.plo.voice.api.server.audio.line.ServerSourceLine
import su.plo.voice.api.server.event.config.VoiceServerConfigReloadedEvent
import su.plo.voice.discs.command.CommandHandler
import su.plo.voice.discs.command.subcommand.BurnCommand
import su.plo.voice.discs.command.subcommand.EraseCommand
import su.plo.voice.discs.command.subcommand.SearchCommand
import su.plo.voice.discs.event.JukeboxEventListener
import su.plo.voice.discs.packet.CancelJukeboxPlayEvent

@Addon(
    id = "pv-addon-discs",
    scope = AddonLoaderScope.SERVER,
    version = "1.0.1",
    authors = ["KPidS"]
)
class DiscsPlugin : JavaPlugin() {

    private val addonName = "discs"

    @Inject
    lateinit var voiceServer: PlasmoVoiceServer

    lateinit var sourceLine: ServerSourceLine

    lateinit var audioPlayerManager: PlasmoAudioPlayerManager

    lateinit var addonConfig: AddonConfig

    val identifierKey = NamespacedKey(this, "identifier")

    val oldIdentifierKey = NamespacedKey("pv-addon-disks", "identifier")

    override fun onLoad() {
        PlasmoVoiceServer.getAddonsLoader().load(this)
    }

    @EventSubscribe
    fun onConfigReloaded(event: VoiceServerConfigReloadedEvent) {
        loadConfig()
    }

    override fun onEnable() {
        loadConfig()

        server.pluginManager.registerEvents(JukeboxEventListener(this), this)

        val handler = CommandHandler(this)
            .addSubCommand(::BurnCommand)
            .addSubCommand(::EraseCommand)
            .addSubCommand(::SearchCommand)

        val command = getCommand("disc") ?: throw Exception("Command not found")

        command.setExecutor(handler)
        command.tabCompleter = handler

        val permissions = voiceServer.minecraftServer.permissionsManager

        permissions.register("pv.addon.discs.play", PermissionDefault.TRUE)

        val protocolManager: ProtocolManager = ProtocolLibrary.getProtocolManager()

        protocolManager.addPacketListener(
            CancelJukeboxPlayEvent(this, ListenerPriority.NORMAL)
        )
    }

    override fun onDisable() {
        PlasmoVoiceServer.getAddonsLoader().unload(this)
    }

    private fun loadConfig() {
        addonConfig = AddonConfig.loadConfig(voiceServer)

        sourceLine = voiceServer.sourceLineManager.createBuilder(
            this,
            addonName,
            "pv.activation.$addonName",
            "plasmovoice:textures/icons/speaker_disc.png",
            addonConfig.sourceLineWeight
        ).apply {
            setDefaultVolume(addonConfig.defaultSourceLineVolume)
        }.build()

        audioPlayerManager = PlasmoAudioPlayerManager(this)
    }
}
