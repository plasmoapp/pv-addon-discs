package su.plo.voice.discs.v1_19_4.nms

import xyz.jpenilla.reflectionremapper.proxy.annotation.Proxies
import xyz.jpenilla.reflectionremapper.proxy.annotation.Type

@Proxies(
    className = "net.minecraft.world.item.ItemStack"
)
interface ItemStackProxy {
    fun getOrCreateTag(
        @Type(className = "net.minecraft.world.item.ItemStack") instance: Any
    ): Any
}