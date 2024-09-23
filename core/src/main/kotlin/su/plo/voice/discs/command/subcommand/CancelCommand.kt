package su.plo.voice.discs.command.subcommand

import org.bukkit.Bukkit
import org.bukkit.command.CommandSender
import org.koin.core.component.inject
import su.plo.slib.api.chat.component.McTextComponent
import su.plo.slib.api.permission.PermissionDefault
import su.plo.voice.discs.GoatHornManager
import su.plo.voice.discs.command.SubCommand
import su.plo.voice.discs.utils.extend.asPlayer
import su.plo.voice.discs.utils.extend.asVoicePlayer
import su.plo.voice.discs.utils.extend.launchAndForget
import su.plo.voice.discs.utils.extend.render
import su.plo.voice.discs.utils.extend.sendTranslatable
import su.plo.voice.discs.utils.extend.toPlainText

class CancelCommand : SubCommand() {

    private val hornManager: GoatHornManager by inject()

    override val name: String = "cancel"

    override val permissions = listOf(
        "cancel" to PermissionDefault.TRUE,
        "cancel.other" to PermissionDefault.OP,
    )

    override fun execute(sender: CommandSender, arguments: Array<out String>) = scope.launchAndForget {
        val player = sender.asPlayer() ?: return@launchAndForget
        val voicePlayer = player.asVoicePlayer(voiceServer) ?: return@launchAndForget

        if (!voicePlayer.instance.hasPermission("pv.addon.discs.cancel")) {
            voicePlayer.instance.sendTranslatable("pv.addon.discs.error.no_permission")
            return@launchAndForget
        }

        val target = arguments.getOrNull(1)
            ?.takeIf { sender.hasPermission("pv.addon.discs.cancel.other") }
            ?.let {
                Bukkit.getPlayer(it) ?: run {
                    voicePlayer.instance.sendTranslatable("pv.addon.discs.error.player_not_found")
                    return@launchAndForget
                }
            }
            ?: player

        if (hornManager.cancelTrack(target) != null) {
            voicePlayer.instance.sendTranslatable("pv.addon.discs.success.canceled")
        } else {
            voicePlayer.instance.sendTranslatable("pv.addon.discs.error.nothing_to_cancel")
        }
    }

    override fun suggest(source: CommandSender, arguments: Array<out String>): List<String> {
        val voicePlayer = source.asVoicePlayer(voiceServer) ?: return emptyList()
        val otherPermission = source.hasPermission("pv.addon.discs.cancel.other")

        val nick = arguments.getOrNull(1) ?: run {
            return if (otherPermission) {
                listOf(
                    McTextComponent.translatable("pv.addon.discs.arg.nick")
                        .render(voicePlayer, voiceServer)
                        .toPlainText()
                )
            } else {
                emptyList()
            }
        }
        if (!otherPermission) return emptyList()

        return Bukkit.getOnlinePlayers()
            .map { it.name }
            .filter { it.startsWith(nick, ignoreCase = true) }
    }

    override fun checkCanExecute(sender: CommandSender): Boolean =
        sender.hasPermission("pv.addon.discs.cancel")
}