package su.plo.voice.discs.item

import org.bukkit.inventory.ItemStack

interface GoatHornHelper {
    fun setEmptyInstrument(item: ItemStack)

    fun getInstrument(item: ItemStack): String

    fun setInstrument(item: ItemStack, instrument: String)
}