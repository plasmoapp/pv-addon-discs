package su.plo.voice.discs.v1_20_6.nms

import xyz.jpenilla.reflectionremapper.proxy.annotation.Proxies
import xyz.jpenilla.reflectionremapper.proxy.annotation.Static

@Proxies(
    className = "net.minecraft.core.Holder"
)
interface HolderProxy {

    @Static
    fun direct(value: Any): Any
}