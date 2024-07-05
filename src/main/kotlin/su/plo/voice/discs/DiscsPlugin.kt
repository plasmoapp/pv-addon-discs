package su.plo.voice.discs

import com.comphenix.protocol.ProtocolLibrary
import com.comphenix.protocol.ProtocolManager
import com.comphenix.protocol.events.ListenerPriority
import com.google.inject.Inject
import org.bukkit.NamespacedKey
import org.bukkit.inventory.meta.ItemMeta
import org.bukkit.persistence.PersistentDataType
import org.bukkit.plugin.java.JavaPlugin
import su.plo.lib.api.server.permission.PermissionDefault
import su.plo.voice.api.addon.AddonLoaderScope
import su.plo.voice.api.addon.annotation.Addon
import su.plo.voice.api.event.EventSubscribe
import su.plo.voice.api.logging.DebugLogger
import su.plo.voice.api.server.PlasmoVoiceServer
import su.plo.voice.api.server.audio.line.ServerSourceLine
import su.plo.voice.api.server.event.config.VoiceServerConfigReloadedEvent
import su.plo.voice.discs.command.CommandHandler
import su.plo.voice.discs.command.subcommand.BurnCommand
import su.plo.voice.discs.command.subcommand.EraseCommand
import su.plo.voice.discs.command.subcommand.SearchCommand
import su.plo.voice.discs.crafting.BurnableDiscCraft
import su.plo.voice.discs.event.ForbidGrindstoneListener
import su.plo.voice.discs.event.JukeboxEventListener
import su.plo.voice.discs.packet.CancelJukeboxPlayEvent
import su.plo.voice.discs.utils.extend.debug

@Addon(
    id = "pv-addon-discs",
    scope = AddonLoaderScope.SERVER,
    version = "1.0.8",
    authors = ["KPidS", "Apehum"]
)
class DiscsPlugin : JavaPlugin() {

    private val addonName = "discs"

    @Inject
    lateinit var voiceServer: PlasmoVoiceServer

    lateinit var sourceLine: ServerSourceLine

    lateinit var audioPlayerManager: PlasmoAudioPlayerManager

    lateinit var addonConfig: AddonConfig

    // PersistentDataType.String
    val identifierKey = NamespacedKey(this, "identifier")

    // PersistentDataType.String
    val oldIdentifierKey = NamespacedKey("pv-addon-disks", "identifier")

    // PersistentDataType.BYTE 0 or 1 representing boolean
    val burnableKey = NamespacedKey(this, "burnable")

    // PersistentDataType.BYTE 0 or 1 representing boolean
    val forbidGrindstoneKey = NamespacedKey(this, "forbid_grindstone")

    override fun onLoad() {
        PlasmoVoiceServer.getAddonsLoader().load(this)
    }

    @EventSubscribe
    fun onConfigReloaded(event: VoiceServerConfigReloadedEvent) {
        loadConfig()
    }

    @EventSubscribe
    override fun onEnable() {
        loadConfig()

        server.pluginManager.registerEvents(JukeboxEventListener(this), this)
        server.pluginManager.registerEvents(ForbidGrindstoneListener(this), this)

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

        if (addonConfig.burnableTag.enableDefaultRecipe) {
            BurnableDiscCraft(this).also { it.registerRecipes() }
        }
    }

    override fun onDisable() {
        PlasmoVoiceServer.getAddonsLoader().unload(this)
    }

    private fun loadConfig() {
        addonConfig = AddonConfig.loadConfig(voiceServer)
        DEBUG_LOGGER = DebugLogger(slF4JLogger)
        DEBUG_LOGGER.enabled(voiceServer.debug())

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

    fun forbidGrindstone(itemMeta: ItemMeta) {
        itemMeta.persistentDataContainer.set(forbidGrindstoneKey, PersistentDataType.BYTE, 1)
    }

    fun allowGrindstone(itemMeta: ItemMeta) {
        itemMeta.persistentDataContainer.remove(forbidGrindstoneKey)
    }

    companion object {

        @JvmStatic
        lateinit var DEBUG_LOGGER: DebugLogger
    }
}
