package su.plo.voice.discs.v1_20_6.nms

import xyz.jpenilla.reflectionremapper.proxy.annotation.Proxies
import xyz.jpenilla.reflectionremapper.proxy.annotation.Type

@Proxies(
    className = "net.minecraft.world.item.ItemStack"
)
interface ItemStackProxy {
    fun set(
        @Type(className = "net.minecraft.world.item.ItemStack") instance: Any,
        @Type(className = "net.minecraft.core.component.DataComponentType") dataType: Any,
        dataValue: Any
    )
}