package su.plo.voice.discs.utils.extend

import kotlinx.coroutines.future.await
import org.bukkit.Location
import org.bukkit.entity.Entity
import su.plo.slib.api.server.McServerLib
import su.plo.slib.api.server.position.ServerPos3d

suspend fun <T> McServerLib.suspendSync(entity: Entity, task: () -> T): T =
    scheduler.runTaskFor(getEntityByInstance(entity), task).await()

suspend fun <T> McServerLib.suspendSync(location: Location, task: () -> T): T =
    scheduler.runTaskAt(location.toServerPos3d(this), task).await()

private fun Location.toServerPos3d(minecraftServer: McServerLib) =
    ServerPos3d(
        minecraftServer.getWorld(world),
        x,
        y,
        z,
        yaw,
        pitch,
    )
