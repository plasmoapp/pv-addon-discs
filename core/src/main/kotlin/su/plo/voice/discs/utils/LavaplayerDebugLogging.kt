package su.plo.voice.discs.utils

import org.apache.logging.log4j.Level
import org.apache.logging.log4j.core.config.Configurator
import su.plo.slib.api.logging.McLoggerFactory
import su.plo.voice.lavaplayer.libs.com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager
import su.plo.voice.lavaplayer.libs.dev.lavalink.youtube.YoutubeAudioSourceManager
import su.plo.voice.lavaplayer.libs.dev.lavalink.youtube.cipher.CipherManager
import su.plo.voice.lavaplayer.libs.dev.lavalink.youtube.track.YoutubeAudioTrack

object LavaplayerDebugLogging {
    private val logger = McLoggerFactory.createLogger("LavaplayerDebugLogging")

    private val loggerNames = listOf(
        AudioPlayerManager::class.java.packageName.substringBeforeLast('.'),
        YoutubeAudioSourceManager::class.java.name,
        CipherManager::class.java.packageName,
        YoutubeAudioTrack::class.java.packageName,
    )

    private var enabled = false

    fun setEnabled(enabled: Boolean) {
        if (this.enabled == enabled) return

        try {
            val level = if (enabled) Level.DEBUG else null
            loggerNames.forEach { Configurator.setLevel(it, level) }
            this.enabled = enabled
        } catch (e: LinkageError) {
            logger.warn("Failed to change lavaplayer log level, log4j2 is not available", e)
        }
    }
}
