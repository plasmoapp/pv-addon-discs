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
import su.plo.voice.discs.utils.MaterialUtil
import su.plo.voice.discs.utils.PluginKoinComponent
import su.plo.voice.discs.utils.extend.forbidGrindstone

class BurnableDiscCraft : PluginKoinComponent {

    private val plugin: JavaPlugin by inject()
    private val config: AddonConfig by inject()
    private val keys: AddonKeys by inject()

    private val groupKey = NamespacedKey(plugin, "burnable_record_craft")

    fun registerRecipes() = MaterialUtil.itemMusicDiscs.forEach { record ->
        createRecipe(record).let(Bukkit::addRecipe)
    }

    private val cost = config.burnableTag.defaultRecipeCost.also {
        if (it > 8 || it <= 0) throw Exception("Cost should be greater than 0 and less than 9")
    }

    private val material = config.burnableTag.defaultRecipeItem.let {
        Material.matchMaterial(it) ?: throw Exception("Material '$it' not found")
    }

    private fun createRecipe(record: Material): ShapelessRecipe {
        val recipeKey = NamespacedKey(
            plugin,
            "burnable_record_craft.${record.key.value().lowercase()}"
        )
        return ShapelessRecipe(recipeKey, createCustomRecord(record)).also { recipe ->
            recipe.addIngredient(ItemStack(record))
            recipe.addIngredient(cost, material)
            recipe.group = groupKey.key
        }
    }


    private fun createCustomRecord(record: Material): ItemStack = with(keys) {
        val itemStack = ItemStack(record)

        itemStack.editMeta {
            if (config.addGlintToCustomDiscs) {
                it.forbidGrindstone()
                it.addEnchant(Enchantment.MENDING, 1, true)
            }

            it.addItemFlags(*ItemFlag.values())
            it.persistentDataContainer.set(
                keys.burnableKey,
                PersistentDataType.BYTE,
                1
            )
            val loreName = Component.text()
                .content(config.burnableTag.defaultRecipeLore)
                .decoration(TextDecoration.ITALIC, false)
                .color(NamedTextColor.BLUE)
                .build()
            it.lore(listOf(loreName))
        }
        return itemStack
    }
}
