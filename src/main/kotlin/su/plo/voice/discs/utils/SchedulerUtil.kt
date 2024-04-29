package su.plo.voice.discs.utils

import kotlinx.coroutines.suspendCancellableCoroutine
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.entity.Entity
import org.bukkit.plugin.Plugin
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException


object SchedulerUtil {

    suspend fun <T> suspendSync(entity: Entity, plugin: Plugin, task: () -> T): T = suspendCancellableCoroutine { cont ->
        runTaskFor(entity, plugin) {
            runCatching(task).fold({ cont.resume(it) }, cont::resumeWithException)
        }
    }

    suspend fun <T> suspendSync(location: Location, plugin: Plugin, task: () -> T): T = suspendCancellableCoroutine { cont ->
        runTaskAt(location, plugin) {
            runCatching(task).fold({ cont.resume(it) }, cont::resumeWithException)
        }
    }

    /*
     * Schedules a task to run for a given entity.
     *
     * For non-Folia servers, runs on Bukkit scheduler.
     * For Folia servers, runs on the entity's scheduler.
     */
    @Suppress("deprecation")
    fun runTaskFor(entity: Entity, plugin: Plugin, task: Runnable) {
        try {
            entity.scheduler.run(plugin, { task.run() }, null)
        } catch (e: NoSuchMethodError) {
            Bukkit.getScheduler().runTask(plugin, task)
        }
    }

    /*
   * Schedules a task to run for a given location.
   *
   * For non-Folia servers, runs on Bukkit scheduler.
   * For Folia servers, runs on the region's scheduler.
   */
    @Suppress("deprecation")
    fun runTaskAt(location: Location, plugin: Plugin, task: Runnable) {
        try {
            Bukkit.getRegionScheduler().run(plugin, location) { task.run() }
        } catch (e: NoSuchMethodError) {
            Bukkit.getScheduler().runTask(plugin, task)
        }
    }
}
