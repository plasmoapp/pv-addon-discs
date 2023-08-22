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

    @ConfigField(
        comment = """
            The default volume. Volume is configured on the client side
            and can be adjusted via the mod settings.
        """
    )
    var defaultSourceLineVolume = 0.5

    @ConfigField(
        comment = """
            Add enchantment glint to custom discs.
        """
    )
    var addGlintToCustomDiscs = false

    @Config class DistanceConfig {

        @ConfigField(
            comment = """
                Distance if 'enable_beacon_like_distance_amplification' is set
                to false.
            """
        )
        var jukeboxDistance: Short = 65

        @ConfigField(
            path = "enable_beacon_like_distance_amplification",
            comment = """
                With this option enabled you can place diamond block in 2
                block radius near jukebox to change the distance of the sound.
            """
        )
        var increaseDistance = false

        @ConfigField(
            path = "beacon_like_distances",
            comment = """
                The first element is the distance without any diamond blocks.
                You can add as much diamond blocks as you want. 
                At least one layer is required.  
            """
        )
        var increasedDistanceList: List<Short> = listOf(12, 24, 32, 48, 64)
    }

    @ConfigField
    val distance = DistanceConfig()

    @Config
    class YouTubeConfig {
        @ConfigField(
            comment = """
                You can specify an email and a password to a YouTube account.
                This will help with age restricted content and rate limits.
                
                Authorization via password can fail. You need to check the
                console and see if you've been prompted to visit a specific
                link and enter a unique code to complete the process.
            """
        )
        val email: String = ""
        @ConfigField
        val password: String = ""
    }

    @ConfigField
    val youtube = YouTubeConfig()

    @Config
    class HttpSourceConfig {
        @ConfigField(
            comment = """
                Only allow links from trusted sources. You can disable this if
                the server IP is public and leaking it is not a problem.
            """
        )
        val whitelistEnabled = true
        @ConfigField
        val whitelist = listOf(
            "dropbox.com",
            "dropboxusercontent.com"
        )
    }

    @ConfigField
    val httpSource = HttpSourceConfig()

    @Config
    class BurnableTag {
        @ConfigField(
            comment = """
                With this option you can only burn discs that have a special NBT
                tag. You can use this to add a custom way of getting burnable
                discs, like buying for in-game currency, or crafting.
            """
        )
        var requireBurnableTag = false
        @ConfigField(
            comment = """
                Enable a recipe for burnable discs. It's a shapeless craft.
                By default you need a disc + 4 diamonds to get a burnable disc.
                You can configure recipe item and cost. 
            """
        )
        var enableDefaultRecipe = false
        @ConfigField
        var defaultRecipeItem = "minecraft:diamond"
        @ConfigField
        var defaultRecipeCost = 4
        @ConfigField(
            comment = """
                A lore that will be added to a burnable disc crafted with the
                default recipe.
            """
        )
        var defaultRecipeLore = "Burnable"
    }

    @ConfigField
    val burnableTag = BurnableTag()

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
