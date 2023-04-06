package su.plo.voice.discs.command

import org.bukkit.command.CommandSender
import su.plo.lib.api.server.permission.PermissionDefault

abstract class SubCommand(val handler: CommandHandler) {

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
