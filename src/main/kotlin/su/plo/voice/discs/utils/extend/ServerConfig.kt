package su.plo.voice.discs.utils.extend

import su.plo.voice.api.server.config.ServerConfig

/**
 * For some reason, this field is not exposed to API in 2.0.x.
 */
fun ServerConfig.debug(): Boolean =
    try {
        val debugField = this.javaClass.getDeclaredField("debug")
        debugField.isAccessible = true
        debugField.get(this) as? Boolean ?: false
    } catch (e: Exception) {
        false
    }
