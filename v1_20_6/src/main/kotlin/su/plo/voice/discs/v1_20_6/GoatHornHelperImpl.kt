package su.plo.voice.discs.v1_20_6

import org.bukkit.Material
import org.bukkit.MusicInstrument
import org.bukkit.NamespacedKey
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.MusicInstrumentMeta
import su.plo.voice.discs.item.GoatHornHelper
import su.plo.voice.discs.utils.ReflectionUtil
import su.plo.voice.discs.v1_20_6.nms.ReflectionProxies

class GoatHornHelperImpl : GoatHornHelper {

    private val emptyInstrument by lazy {
        ReflectionProxies.holder.direct(
            ReflectionProxies.instrument.newInstance(
                ReflectionProxies.holder.direct(ReflectionProxies.soundEvents.empty()),
                140,
                256f
            )
        )
    }

    override fun setEmptyInstrument(item: ItemStack) {
        if (item.type != Material.GOAT_HORN) return

        val mcItem = ReflectionUtil.getMinecraftItemStack(item)

        ReflectionProxies.itemStack.set(
            mcItem,
            ReflectionProxies.dataComponents.instrument(),
            emptyInstrument
        )
    }

    override fun getInstrument(item: ItemStack): String {
        if (item.type != Material.GOAT_HORN) throw IllegalArgumentException("item is not GOAT_HORN")

        val itemMeta = item.itemMeta as MusicInstrumentMeta
        val instrument = itemMeta.instrument ?: return ""

        return instrument.key.toString()
    }

    override fun setInstrument(item: ItemStack, instrument: String) {
        if (item.type != Material.GOAT_HORN) return

        val instrumentKey = NamespacedKey.fromString(instrument)
            ?: throw IllegalArgumentException("$instrument is now a valid key")
        val musicInstrument = MusicInstrument.getByKey(instrumentKey) ?: return

        item.editMeta(MusicInstrumentMeta::class.java) {
            it.instrument = musicInstrument
        }
    }
}