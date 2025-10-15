package su.plo.voice.discs.item

import org.bukkit.inventory.ItemStack

interface DiscHelper {
    fun showSongTooltip(item: ItemStack, show: Boolean)

    fun isRecord(item: ItemStack) : Boolean
}
