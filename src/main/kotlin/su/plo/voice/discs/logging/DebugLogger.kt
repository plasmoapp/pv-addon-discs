package su.plo.voice.discs.logging

import org.slf4j.Logger

class DebugLogger(private val logger: Logger) {

    var enabled = false

    fun log(message: String?, vararg params: Any?) {
        if (enabled) {
            logger.info(message, *params)
        }
    }

    fun warn(message: String?, vararg params: Any?) {
        if (enabled) {
            logger.warn(message, *params)
        }
    }
}
