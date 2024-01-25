package su.plo.voice.discs.utils.extend

import su.plo.voice.api.server.PlasmoVoiceServer

fun PlasmoVoiceServer.debug(): Boolean =
    this.config.debug() || System.getProperty("plasmovoice.debug") == "true"
