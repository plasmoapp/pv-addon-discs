package su.plo.voice.discs.utils

import kotlinx.coroutines.suspendCancellableCoroutine
import org.bukkit.Bukkit
import org.bukkit.plugin.Plugin
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

suspend fun <T> suspendSync(plugin: Plugin, task: () -> T): T = suspendCancellableCoroutine { cont ->
    Bukkit.getScheduler().runTask(plugin) { _ ->
        runCatching(task).fold({ cont.resume(it) }, cont::resumeWithException)
    }
}