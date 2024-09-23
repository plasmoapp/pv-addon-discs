package su.plo.voice.discs.v1_19_4.nms

import xyz.jpenilla.reflectionremapper.proxy.annotation.Proxies
import xyz.jpenilla.reflectionremapper.proxy.annotation.Type

@Proxies(
    className = "net.minecraft.nbt.CompoundTag"
)
interface CompoundTagProxy {
    fun putString(
        @Type(className = "net.minecraft.nbt.CompoundTag") instance: Any,
        key: String,
        value: String
    )
}