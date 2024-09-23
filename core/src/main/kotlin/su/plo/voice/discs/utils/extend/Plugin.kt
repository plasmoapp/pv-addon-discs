package su.plo.voice.discs.utils.extend

import kotlinx.coroutines.suspendCancellableCoroutine
import org.bukkit.Location
import org.bukkit.entity.Entity
import org.bukkit.plugin.Plugin
import su.plo.slib.spigot.util.SchedulerUtil
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

suspend fun <T> Plugin.suspendSync(entity: Entity, task: () -> T): T = suspendCancellableCoroutine { cont ->
    SchedulerUtil.runTaskFor(entity, this) {
        runCatching(task).fold({ cont.resume(it) }, cont::resumeWithException)
    }
}

suspend fun <T> Plugin.suspendSync(location: Location, task: () -> T): T = suspendCancellableCoroutine { cont ->
    SchedulerUtil.runTaskAt(location, this) {
        runCatching(task).fold({ cont.resume(it) }, cont::resumeWithException)
    }
}
