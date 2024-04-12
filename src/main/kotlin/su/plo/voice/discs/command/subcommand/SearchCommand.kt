package su.plo.voice.discs.command.subcommand

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.future.await
import kotlinx.coroutines.launch
import org.bukkit.command.CommandSender
import su.plo.slib.api.chat.component.McTextComponent
import su.plo.slib.api.chat.style.McTextClickEvent
import su.plo.slib.api.chat.style.McTextHoverEvent
import su.plo.slib.api.permission.PermissionDefault
import su.plo.voice.discs.DiscsPlugin
import su.plo.voice.discs.command.CommandHandler
import su.plo.voice.discs.command.SubCommand
import su.plo.voice.discs.utils.extend.asPlayer
import su.plo.voice.discs.utils.extend.asVoicePlayer
import su.plo.voice.discs.utils.extend.sendTranslatable

class SearchCommand(handler: CommandHandler) : SubCommand(handler) {

    override val name = "search"

    override val permissions = listOf(
        "search" to PermissionDefault.OP
    )

    private val scope = CoroutineScope(Dispatchers.Default)

    override fun suggest(source: CommandSender, arguments: Array<out String>): List<String> {

        val voicePlayer = source.asPlayer()?.asVoicePlayer(handler.plugin.voiceServer) ?: return listOf()

        if (!voicePlayer.instance.hasPermission("pv.addon.discs.search")) return listOf()

       return listOf(
            handler.getTranslationStringByKey("pv.addon.discs.arg.search", voicePlayer.instance)
        )
    }

    override fun execute(sender: CommandSender, arguments: Array<out String>) { scope.launch {

        val voicePlayer = sender.asPlayer()?.asVoicePlayer(handler.plugin.voiceServer) ?: return@launch

        if (!voicePlayer.instance.hasPermission("pv.addon.discs.search")) {
            voicePlayer.instance.sendTranslatable("pv.addon.discs.error.no_permission")
            return@launch
        }

        val query = arguments.drop(1).joinToString(" ")

        val tracks = try {
            handler.plugin.audioPlayerManager.getPlaylist("ytsearch:$query").await().tracks
        } catch (e: Exception) {
            voicePlayer.instance.sendTranslatable("pv.addon.discs.error.search_fail", e.message ?: "Unexpected error")
            DiscsPlugin.DEBUG_LOGGER.log("Search failed", e)
            return@launch
        }

        if (tracks.isEmpty()) {
            voicePlayer.instance.sendTranslatable("pv.addon.discs.error.search_fail", "No results")
            return@launch
        }

        tracks.take(5).forEach {
            val command = "/disc burn ${it.identifier}"
            val component = McTextComponent.translatable("pv.addon.discs.format.search_entry", it.info.title, it.info.author)
                .hoverEvent(McTextHoverEvent.showText(McTextComponent.literal(command)))
                .clickEvent(McTextClickEvent.suggestCommand(command))
            voicePlayer.instance.sendMessage(component)
        }
    }}

    override fun checkCanExecute(sender: CommandSender): Boolean = sender.hasPermission("pv.addon.discs.search")
}
