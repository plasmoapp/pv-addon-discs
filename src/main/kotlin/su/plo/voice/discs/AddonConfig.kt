package su.plo.voice.discs

import org.bukkit.Bukkit
import su.plo.config.Config
import su.plo.config.ConfigField
import su.plo.config.provider.ConfigurationProvider
import su.plo.config.provider.toml.TomlConfiguration
import su.plo.voice.api.server.PlasmoVoiceServer
import java.io.File
import java.io.IOException
import java.io.InputStream

@Config
class AddonConfig {
    @ConfigField
    var sourceLineWeight = 10

    @ConfigField
    var defaultSourceLineVolume = 0.2

    @ConfigField
    var jukeboxDistance: Short = 65

    @ConfigField
    var addGlintToCustomDiscs = false

    @ConfigField(path = "enable_beacon_like_distance_amplification",)
    var enableBeaconLikeDistance = false

    @ConfigField(path = "beacon_like_distances")
    var beaconLikeDistanceList: List<Short> = listOf(12, 24, 32, 48, 64)

    companion object {
        fun loadConfig(server: PlasmoVoiceServer): AddonConfig {

            val addonFolder = getAddonFolder()

            server.languages.register(
                "plasmo-voice-addons",
                "server/discs.toml",
                { resourcePath: String -> getLanguageResource(resourcePath)
                    ?: throw Exception("Can't load language resource") },
                File(addonFolder, "languages")
            )

            val configFile = File(addonFolder, "discs.toml")

            return toml.load<AddonConfig>(AddonConfig::class.java, configFile, false)
                .also { toml.save(AddonConfig::class.java, it, configFile) }
        }

        @Throws(IOException::class)
        private fun getLanguageResource(resourcePath: String): InputStream? {
            return javaClass.classLoader.getResourceAsStream(String.format("discs/%s", resourcePath))
        }

        private val toml = ConfigurationProvider.getProvider<ConfigurationProvider>(
            TomlConfiguration::class.java
        )

        private fun getAddonFolder(): File =
            File(Bukkit.getPluginsFolder(), "pv-addon-discs")
    }
}
