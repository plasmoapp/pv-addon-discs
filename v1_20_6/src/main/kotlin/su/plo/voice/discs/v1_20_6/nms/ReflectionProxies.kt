package su.plo.voice.discs.v1_20_6.nms

import xyz.jpenilla.reflectionremapper.ReflectionRemapper
import xyz.jpenilla.reflectionremapper.proxy.ReflectionProxyFactory

object ReflectionProxies {

    val itemStack: ItemStackProxy
    val holder: HolderProxy
    val instrument: InstrumentProxy
    val soundEvents: SoundEventsProxy
    val dataComponents: DataComponentsProxy

    init {
        val remapper = ReflectionRemapper.forReobfMappingsInPaperJar()
        val proxyFactory = ReflectionProxyFactory.create(remapper, javaClass.classLoader)

        itemStack = proxyFactory.reflectionProxy(ItemStackProxy::class.java)
        holder = proxyFactory.reflectionProxy(HolderProxy::class.java)
        instrument = proxyFactory.reflectionProxy(InstrumentProxy::class.java)
        soundEvents = proxyFactory.reflectionProxy(SoundEventsProxy::class.java)
        dataComponents = proxyFactory.reflectionProxy(DataComponentsProxy::class.java)
    }
}