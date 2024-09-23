package su.plo.voice.discs.command

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import org.bukkit.command.CommandSender
import org.koin.core.component.inject
import su.plo.slib.api.permission.PermissionDefault
import su.plo.voice.api.logging.DebugLogger
import su.plo.voice.api.server.PlasmoVoiceServer
import su.plo.voice.discs.AddonConfig
import su.plo.voice.discs.AddonKeys
import su.plo.voice.discs.PlasmoAudioPlayerManager
import su.plo.voice.discs.utils.PluginKoinComponent

abstract class SubCommand : PluginKoinComponent {

    protected val voiceServer: PlasmoVoiceServer by inject()
    protected val keys: AddonKeys by inject()
    protected val config: AddonConfig by inject()
    protected val audioPlayerManager: PlasmoAudioPlayerManager by inject()
    protected val debugLogger: DebugLogger by inject()

    protected val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    abstract val name: String

    abstract fun execute(
        sender: CommandSender,
        arguments: Array<out String>,
    )

    open val permissions: List<Pair<String, PermissionDefault>> = listOf()

    open fun suggest(
        source: CommandSender,
        arguments: Array<out String>,
    ): List<String> = listOf()

    open fun checkCanExecute(sender: CommandSender): Boolean = true
}
