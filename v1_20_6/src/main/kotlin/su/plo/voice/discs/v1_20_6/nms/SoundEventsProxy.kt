package su.plo.voice.discs.v1_20_6.nms

import xyz.jpenilla.reflectionremapper.proxy.annotation.FieldGetter
import xyz.jpenilla.reflectionremapper.proxy.annotation.Proxies
import xyz.jpenilla.reflectionremapper.proxy.annotation.Static

@Proxies(
    className = "net.minecraft.sounds.SoundEvents"
)
interface SoundEventsProxy {

    @Static
    @FieldGetter("EMPTY")
    fun empty(): Any
}