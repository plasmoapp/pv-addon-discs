package su.plo.voice.discs.mcver.nms

//? if >=1.20.6 {
/*import xyz.jpenilla.reflectionremapper.proxy.annotation.ConstructorInvoker
import xyz.jpenilla.reflectionremapper.proxy.annotation.Proxies
import xyz.jpenilla.reflectionremapper.proxy.annotation.Type

@Proxies(
    className = "net.minecraft.world.item.Instrument"
)
interface InstrumentProxy {

    @ConstructorInvoker
    fun newInstance(
        @Type(className = "net.minecraft.core.Holder") holder: Any,
        //? if >=1.21.3 {
        /^duration: Float,
        ^///?} else {
        duration: Int,
        //?}
        range: Float,
        //? if >=26.3
        /^durabilityDamage: Int,^/
        //? if >=1.21.3
        /^@Type(className = "net.minecraft.network.chat.Component") component: Any^/
    ): Any
}
*///?}