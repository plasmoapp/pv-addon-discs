package su.plo.voice.discs.crafting

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextDecoration
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.enchantments.Enchantment
import org.bukkit.inventory.*
import org.bukkit.persistence.PersistentDataType
import su.plo.voice.discs.DiscsPlugin
import su.plo.voice.discs.utils.MaterialUtil

class BurnableDiscCraft(val plugin: DiscsPlugin) {

    private val groupKey = NamespacedKey(plugin, "burnable_record_craft")

    fun registerRecipes() = MaterialUtil.itemMusicDiscs.forEach { record ->
        createRecipe(record).let(Bukkit::addRecipe)
    }

    private val cost = plugin.addonConfig.burnableTag.defaultRecipeCost.also {
        if (it > 8 || it <= 0) throw Exception("Cost should be greater than 0 and less than 9")
    }

    private val material = plugin.addonConfig.burnableTag.defaultRecipeItem.let {
        Material.matchMaterial(it) ?: throw Exception("Material '$it' not found")
    }

    private fun createRecipe(record: Material): ShapelessRecipe {
        val recipeKey = NamespacedKey(
            plugin,
            "burnable_record_craft.${record.key().value().lowercase()}"
        )
        return ShapelessRecipe(recipeKey, createCustomRecord(record)).also { recipe ->
            recipe.addIngredient(ItemStack(record))
            recipe.addIngredient(cost, material)
            recipe.group = groupKey.key
        }
    }


    private fun createCustomRecord(record: Material): ItemStack {
        val itemStack = ItemStack(record)

        itemStack.editMeta {
            if (plugin.addonConfig.addGlintToCustomDiscs) {
                plugin.forbidGrindstone(it)
            }

            if (plugin.addonConfig.addGlintToCustomDiscs) {
                it.addEnchant(Enchantment.MENDING, 1, true)
            }
            it.addItemFlags(*ItemFlag.values())
            it.persistentDataContainer.set(
                plugin.burnableKey,
                PersistentDataType.BYTE,
                1
            )
            val loreName = Component.text()
                .content(plugin.addonConfig.burnableTag.defaultRecipeLore)
                .decoration(TextDecoration.ITALIC, false)
                .color(NamedTextColor.BLUE)
                .build()
            it.lore(listOf(loreName))
        }
        return itemStack
    }
}
