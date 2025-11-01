package su.plo.voice.discs.event

import com.destroystokyo.paper.event.inventory.PrepareResultEvent
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.inventory.GrindstoneInventory
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType
import su.plo.voice.discs.AddonKeys
import su.plo.voice.discs.utils.PluginKoinComponent

class ForbidGrindstoneListener: Listener, PluginKoinComponent {

    private fun ItemStack.isForbidden() =
        itemMeta.persistentDataContainer
            .get(AddonKeys.forbidGrindstoneKey, PersistentDataType.BYTE)
            ?.let { it.toInt() == 1 }
            ?: false

    @EventHandler
    fun onPrepareGrindstoneEvent(event: PrepareResultEvent) {
        val inventory = event.inventory as? GrindstoneInventory ?: return

        if (
            inventory.lowerItem?.isForbidden() == true ||
            inventory.upperItem?.isForbidden() == true
        ) event.result = null
    }
}
