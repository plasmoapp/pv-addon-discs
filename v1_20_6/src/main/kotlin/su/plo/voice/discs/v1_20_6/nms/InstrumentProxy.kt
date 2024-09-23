package su.plo.voice.discs.v1_20_6.nms

import xyz.jpenilla.reflectionremapper.proxy.annotation.ConstructorInvoker
import xyz.jpenilla.reflectionremapper.proxy.annotation.Proxies
import xyz.jpenilla.reflectionremapper.proxy.annotation.Type

@Proxies(
    className = "net.minecraft.world.item.Instrument"
)
interface InstrumentProxy {

    @ConstructorInvoker
    fun newInstance(
        @Type(className = "net.minecraft.core.Holder") holder: Any,
        duration: Int,
        range: Float
    ): Any
}