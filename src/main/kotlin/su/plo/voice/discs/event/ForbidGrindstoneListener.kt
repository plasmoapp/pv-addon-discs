package su.plo.voice.discs.event

import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.inventory.PrepareGrindstoneEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType
import su.plo.voice.discs.DiscsPlugin

class ForbidGrindstoneListener(val plugin: DiscsPlugin): Listener {

    private fun ItemStack.isForbidden() =
        itemMeta.persistentDataContainer
            .get(plugin.forbidGrindstoneKey, PersistentDataType.BYTE)
            ?.let { it.toInt() == 1 }
            ?: false

    @EventHandler
    fun onPrepareGrindstoneEvent(event: PrepareGrindstoneEvent) {
        if (
            event.inventory.lowerItem?.isForbidden() == true ||
            event.inventory.upperItem?.isForbidden() == true
        ) event.result = null
    }
}