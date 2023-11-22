package su.plo.voice.discs.utils

import kotlinx.coroutines.suspendCancellableCoroutine
import org.bukkit.Location
import org.bukkit.entity.Entity
import org.bukkit.plugin.Plugin
import su.plo.slib.spigot.util.SchedulerUtil
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

suspend fun <T> suspendSync(entity: Entity, plugin: Plugin, task: () -> T): T = suspendCancellableCoroutine { cont ->
    SchedulerUtil.runTaskFor(entity, plugin) {
        runCatching(task).fold({ cont.resume(it) }, cont::resumeWithException)
    }
}

suspend fun <T> suspendSync(location: Location, plugin: Plugin, task: () -> T): T = suspendCancellableCoroutine { cont ->
    SchedulerUtil.runTaskAt(location, plugin) {
        runCatching(task).fold({ cont.resume(it) }, cont::resumeWithException)
    }
}
