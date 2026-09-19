package su.plo.voice.discs.mcver

import org.bukkit.Material
import org.bukkit.MusicInstrument
import org.bukkit.NamespacedKey
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.MusicInstrumentMeta
import su.plo.voice.discs.item.GoatHornHelper
import su.plo.voice.discs.mcver.nms.ReflectionProxies
import su.plo.voice.discs.utils.ReflectionUtil

class GoatHornHelperImpl : GoatHornHelper {

    //? if >=1.21.5 {
    /*private val emptyInstrument by lazy {
        ReflectionProxies.instrumentComponent.newInstance(
            ReflectionProxies.holder.direct(
                ReflectionProxies.instrument.newInstance(
                    ReflectionProxies.holder.direct(ReflectionProxies.soundEvents.empty()),
                    140.0f,
                    256f,
                    //? if >=26.3
                    /^durabilityDamage = 0,^/
                    ReflectionProxies.component.empty()
                )
            )
        )
    }
    *///?} else if >=1.20.6 {
    /*private val emptyInstrument by lazy {
        ReflectionProxies.holder.direct(
            ReflectionProxies.instrument.newInstance(
                ReflectionProxies.holder.direct(ReflectionProxies.soundEvents.empty()),
                //? if >=1.21.3 {
                140.0f,
                //?} else {
                /^140,
                ^///?}
                256f,
                //? if >=1.21.3
                ReflectionProxies.component.empty()
            )
        )
    }
    *///?}

    override fun setEmptyInstrument(item: ItemStack) {
        if (item.type != Material.GOAT_HORN) return

        val mcItem = ReflectionUtil.getMinecraftItemStack(item)

        //? if >=1.20.6 {
        /*ReflectionProxies.itemStack.set(
            mcItem,
            ReflectionProxies.dataComponents.instrument(),
            emptyInstrument
        )
        *///?} else {
        val compoundTag = ReflectionProxies.itemStack.getOrCreateTag(mcItem)

        ReflectionProxies.compoundTag.putString(compoundTag, "instrument", "empty")
        //?}
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

    override fun getAllHorns(): List<Pair<ItemStack, String>> =
        MusicInstrument.values().map { instrument ->
            ItemStack(Material.GOAT_HORN).also { item ->
                item.editMeta(MusicInstrumentMeta::class.java) {
                    it.instrument = instrument
                }
            } to instrument.key().value()
        }
}
