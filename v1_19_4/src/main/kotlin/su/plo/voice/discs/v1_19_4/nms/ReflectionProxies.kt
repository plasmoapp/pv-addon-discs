package su.plo.voice.discs.v1_19_4.nms

import xyz.jpenilla.reflectionremapper.ReflectionRemapper
import xyz.jpenilla.reflectionremapper.proxy.ReflectionProxyFactory

object ReflectionProxies {

    val itemStack: ItemStackProxy
    val compoundTag: CompoundTagProxy

    init {
        val remapper = ReflectionRemapper.forReobfMappingsInPaperJar()
        val proxyFactory = ReflectionProxyFactory.create(remapper, javaClass.classLoader)

        itemStack = proxyFactory.reflectionProxy(ItemStackProxy::class.java)
        compoundTag = proxyFactory.reflectionProxy(CompoundTagProxy::class.java)
    }
}