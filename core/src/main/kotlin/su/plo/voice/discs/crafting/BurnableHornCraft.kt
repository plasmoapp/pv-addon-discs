package su.plo.voice.discs.crafting

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextDecoration
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.enchantments.Enchantment
import org.bukkit.inventory.ItemFlag
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.ShapelessRecipe
import org.bukkit.persistence.PersistentDataType
import org.bukkit.plugin.java.JavaPlugin
import org.koin.core.component.inject
import su.plo.voice.discs.AddonConfig
import su.plo.voice.discs.AddonKeys
import su.plo.voice.discs.item.GoatHornHelper
import su.plo.voice.discs.utils.PluginKoinComponent
import su.plo.voice.discs.utils.extend.forbidGrindstone
import su.plo.voice.discs.utils.extend.getValue
import su.plo.voice.discs.utils.extend.getter

class BurnableHornCraft : PluginKoinComponent {

    private val plugin: JavaPlugin by inject()
    private val config: AddonConfig by getter()
    private val goatHornHelper: GoatHornHelper by inject()

    fun registerRecipes() {
        goatHornHelper.getAllHorns().filter { (goatHorn, instrumentName) ->
            Bukkit.addRecipe(createRecipe(goatHorn, instrumentName))
        }
    }

    private val cost = config.burnableTag.defaultRecipeCost.also {
        if (it > 8 || it <= 0) throw Exception("Cost should be greater than 0 and less than 9")
    }

    private val material = config.burnableTag.defaultRecipeItem.let {
        Material.matchMaterial(it) ?: throw Exception("Material '$it' not found")
    }

    private fun createRecipe(goatHorn: ItemStack, instrumentName: String): ShapelessRecipe {
        val recipeKey = NamespacedKey(
            plugin,
            "burnable_goat_horn_craft.${instrumentName.lowercase()}"
        )

        return ShapelessRecipe(recipeKey, createCustomGoatHorn(goatHorn)).also { recipe ->
            recipe.addIngredient(goatHorn)
            recipe.addIngredient(cost, material)
        }
    }

    private fun createCustomGoatHorn(goatHorn: ItemStack): ItemStack {
        val itemStack = goatHorn.clone()

        itemStack.editMeta {
            if (config.addGlintToCustomDiscs) {
                it.forbidGrindstone()
                it.addEnchant(Enchantment.MENDING, 1, true)
            }

            it.addItemFlags(*ItemFlag.values())
            it.persistentDataContainer.set(
                AddonKeys.burnableKey,
                PersistentDataType.BYTE,
                1
            )
            val loreName = Component.text(config.burnableTag.defaultRecipeLore, NamedTextColor.BLUE)
                .decoration(TextDecoration.ITALIC, false)
            it.lore(listOf(loreName))
        }

        return itemStack
    }
}
