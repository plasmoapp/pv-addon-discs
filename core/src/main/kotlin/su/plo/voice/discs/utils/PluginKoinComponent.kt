package su.plo.voice.discs.utils

import org.koin.core.Koin
import org.koin.core.component.KoinComponent

lateinit var KOIN_INSTANCE: Koin

interface PluginKoinComponent : KoinComponent {
    override fun getKoin(): Koin = KOIN_INSTANCE
}
