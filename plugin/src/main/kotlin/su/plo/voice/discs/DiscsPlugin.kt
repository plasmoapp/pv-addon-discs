package su.plo.voice.discs

import com.github.retrooper.packetevents.PacketEvents
import com.github.retrooper.packetevents.event.PacketListenerPriority
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.bukkit.Bukkit
import org.bukkit.plugin.java.JavaPlugin
import org.koin.dsl.koinApplication
import org.koin.dsl.module
import su.plo.slib.api.logging.McLoggerFactory
import su.plo.slib.api.permission.PermissionDefault
import su.plo.voice.api.addon.AddonLoaderScope
import su.plo.voice.api.addon.annotation.Addon
import su.plo.voice.api.addon.injectPlasmoVoice
import su.plo.voice.api.event.EventSubscribe
import su.plo.voice.api.logging.DebugLogger
import su.plo.voice.api.server.PlasmoVoiceServer
import su.plo.voice.api.server.audio.line.ServerSourceLine
import su.plo.voice.api.server.event.config.VoiceServerConfigReloadedEvent
import su.plo.voice.discs.command.CommandHandler
import su.plo.voice.discs.command.subcommand.BurnCommand
import su.plo.voice.discs.command.subcommand.CancelCommand
import su.plo.voice.discs.command.subcommand.EraseCommand
import su.plo.voice.discs.command.subcommand.SearchCommand
import su.plo.voice.discs.crafting.BurnableDiscCraft
import su.plo.voice.discs.crafting.BurnableHornCraft
import su.plo.voice.discs.event.ForbidGrindstoneListener
import su.plo.voice.discs.event.JukeboxEventListener
import su.plo.voice.discs.item.DiscHelper
import su.plo.voice.discs.item.GoatHornHelper
import su.plo.voice.discs.packet.CancelJukeboxPlayEvent
import su.plo.voice.discs.utils.KOIN_INSTANCE
import su.plo.voice.discs.utils.extend.debug
import su.plo.voice.discs.utils.extend.getMinecraftVersionInt
import su.plo.voice.discs.utils.extend.registerBukkit
import su.plo.voice.discs.v1_19_4.GoatHornListener

@Addon(
    id = "pv-addon-discs",
    scope = AddonLoaderScope.SERVER,
    version = BuildConstants.VERSION,
    authors = ["KPidS", "Apehum"]
)
class DiscsPlugin : JavaPlugin() {

    private val addonName = "discs"

    private val voiceServer: PlasmoVoiceServer by injectPlasmoVoice()
    private lateinit var sourceLine: ServerSourceLine

    private val keys = AddonKeys.of(this@DiscsPlugin)

    private val discHelper: DiscHelper by lazy {
        val mcVersion = Bukkit.getServer().getMinecraftVersionInt()

        if (mcVersion >= 12105) {
            su.plo.voice.discs.v1_21_5.DiscHelperImpl()
        } else if (mcVersion >= 12103) {
            su.plo.voice.discs.v1_21_3.DiscHelperImpl()
        } else {
            throw IllegalArgumentException("Disc helper in $mcVersion is not supported!")
        } as DiscHelper
    }

    private val goatHornHelper: GoatHornHelper by lazy {
        val mcVersion = Bukkit.getServer().getMinecraftVersionInt()

        if (mcVersion >= 12105) {
            su.plo.voice.discs.v1_21_5.GoatHornHelperImpl()
        } else if (mcVersion >= 12103) {
            su.plo.voice.discs.v1_21_3.GoatHornHelperImpl()
        } else if (mcVersion >= 12006) {
            su.plo.voice.discs.v1_20_6.GoatHornHelperImpl()
        } else if (mcVersion >= 11902) {
            su.plo.voice.discs.v1_19_4.GoatHornHelperImpl()
        } else {
            throw IllegalArgumentException("Goat horns in $mcVersion is not supported!")
        } as GoatHornHelper
    }

    private val goatHornManager by lazy {
        val mcVersion = Bukkit.getServer().getMinecraftVersionInt()

        if (mcVersion >= 11902) {
            GoatHornManager()
        } else {
            throw IllegalArgumentException("Goat horns in $mcVersion is not supported!")
        }
    }

    private lateinit var audioPlayerManager: PlasmoAudioPlayerManager

    private lateinit var addonConfig: AddonConfig
    private lateinit var debugLogger: DebugLogger

    override fun onLoad() {
        PlasmoVoiceServer.getAddonsLoader().load(this)

        KOIN_INSTANCE =
            koinApplication {
                modules(
                    module {
                        single<PlasmoVoiceServer> { voiceServer }
                        factory<ServerSourceLine> { sourceLine }
                        factory<AddonConfig> { addonConfig }
                        factory<PlasmoAudioPlayerManager> { audioPlayerManager }
                        single<GoatHornHelper> { goatHornHelper }
                        single<GoatHornManager> { goatHornManager }
                        single<JavaPlugin> { this@DiscsPlugin }
                        single<AddonKeys> { keys }
                        factory<DebugLogger> { debugLogger }
                        single<DiscHelper> { discHelper }
                    }
                )
            }.koin
    }

    @EventSubscribe
    fun onConfigReloaded(event: VoiceServerConfigReloadedEvent) {
        loadConfig()
    }

    override fun onEnable() {
        loadConfig()

        val jukeboxEventListener = JukeboxEventListener()
        server.pluginManager.registerEvents(jukeboxEventListener, this)

        if (Bukkit.getServer().getMinecraftVersionInt() >= 11904) {
            Bukkit.getServer().pluginManager.registerEvents(jukeboxEventListener.HopperEventListener(), this)
        }

        server.pluginManager.registerEvents(ForbidGrindstoneListener(), this)

        val hornsSupported = Bukkit.getServer().getMinecraftVersionInt() >= 11902
        if (hornsSupported) {
            server.pluginManager.registerEvents(GoatHornListener(), this)
        }

        val handler = CommandHandler()
            .addSubCommand(::BurnCommand)
            .addSubCommand(::EraseCommand)
            .addSubCommand(::SearchCommand)
        if (hornsSupported) {
            handler.addSubCommand(::CancelCommand)
        }

        val command = getCommand("disc") ?: throw Exception("Command not found")

        command.setExecutor(handler)
        command.tabCompleter = handler

        val permissions = voiceServer.minecraftServer.permissionManager
        permissions.registerBukkit("pv.addon.discs.play", PermissionDefault.TRUE)

        PacketEvents.getAPI().eventManager.registerListener(
            CancelJukeboxPlayEvent(keys, jukeboxEventListener),
            PacketListenerPriority.NORMAL
        )

        if (addonConfig.burnableTag.enableDefaultRecipe) {
            BurnableDiscCraft().also { it.registerRecipes() }

            val mcVersion = Bukkit.getServer().getMinecraftVersionInt()

            if (mcVersion >= 11902) {
                BurnableHornCraft().registerRecipes()
            }
        }
    }

    override fun onDisable() {
        PlasmoVoiceServer.getAddonsLoader().unload(this)

        if (::audioPlayerManager.isInitialized) {
            audioPlayerManager.shutdown()
        }
    }

    private fun loadConfig() {
        addonConfig = AddonConfig.loadConfig(voiceServer)
        debugLogger = DebugLogger(McLoggerFactory.createLogger(slF4JLogger.name))
        debugLogger.enabled(addonConfig.debug || voiceServer.debug())

        voiceServer.sourceLineManager.unregister(addonName)
        sourceLine = voiceServer.sourceLineManager.createBuilder(
            this,
            addonName,
            "pv.activation.$addonName",
            "plasmovoice:textures/icons/speaker_disc.png",
            addonConfig.sourceLineWeight
        ).apply {
            setDefaultVolume(addonConfig.defaultSourceLineVolume)
        }.build()

        if (::audioPlayerManager.isInitialized) {
            audioPlayerManager.shutdown()
        }

        audioPlayerManager = PlasmoAudioPlayerManager()
        CoroutineScope(Dispatchers.Default).launch {
            audioPlayerManager.registerSources()
        }
    }
}
